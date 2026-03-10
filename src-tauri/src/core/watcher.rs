//! Background file watcher for the vault directory.

use fastembed::TextEmbedding;
use notify::{Event, EventKind, RecursiveMode, Watcher};
use rusqlite::Connection;
use std::path::PathBuf;
use std::sync::Arc;
use tokio::fs;
use tokio::sync::{mpsc, Mutex};
use crate::core::error::AppError;

pub async fn process_file(
    path: PathBuf,
    model: Arc<TextEmbedding>,
    db: Arc<Mutex<Connection>>,
) -> Result<(), AppError> {
    let start = std::time::Instant::now();

    let contents = fs::read_to_string(&path).await?;

    let embeddings = tokio::task::spawn_blocking(move || model.embed(vec![contents], None))
        .await
        .map_err(|e| AppError::EmbeddingError(format!("Task spawn failed: {}", e)))?;

    let embedding = match embeddings {
        Ok(mut e) => e.pop().unwrap(),
        Err(err) => return Err(AppError::EmbeddingError(format!("Error during embedding generation: {:?}", err))),
    };

    let db_lock = db.lock().await;
    crate::core::db::upsert_embedding(&db_lock, &path, &embedding)?;

    let elapsed = start.elapsed();
    println!(
        "Generated and saved embedding for {:?} in {:?}",
        path.file_name().unwrap_or_default(),
        elapsed
    );

    Ok(())
}

/// Starts a background directory watcher that generates embeddings when markdown files change.
pub async fn start_watching(
    vault_path: PathBuf,
    model: Arc<TextEmbedding>,
    db: Arc<Mutex<Connection>>,
) {
    let (tx, mut rx) = mpsc::channel(100);

    let mut watcher = notify::recommended_watcher(move |res: Result<Event, notify::Error>| {
        if let Ok(event) = res {
            let _ = tx.blocking_send(event);
        }
    })
    .unwrap();

    watcher.watch(&vault_path, RecursiveMode::Recursive).unwrap();

    while let Some(event) = rx.recv().await {
        // We only care about file modification events
        if let EventKind::Modify(_) = event.kind {
            for path in event.paths {
                if path.extension().and_then(|s| s.to_str()) == Some("md") {
                    let model_clone = Arc::clone(&model);
                    let db_clone = Arc::clone(&db);
                    let path_clone = path.clone();

                    tokio::spawn(async move {
                        if let Err(e) = process_file(path_clone, model_clone, db_clone).await {
                            eprintln!("Error processing file: {}", e);
                        }
                    });
                }
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Write;
    use tempfile::NamedTempFile;

    #[tokio::test]
    async fn test_process_file_generates_different_embeddings_for_different_files() {
        // 1. Initialize the embedding model
        let model = crate::core::embedding::init_model().await;
        
        // 2. Init in-memory db to pass into the process_file function
        let temp_dir = tempfile::tempdir().unwrap();
        let db = crate::core::db::init_db(temp_dir.path()).unwrap();
        let db_arc = Arc::new(Mutex::new(db));

        // 3. Create two temporary files with different textual contents
        let mut file1 = NamedTempFile::new().unwrap();
        writeln!(file1, "The core philosophy is Friction as a Feature.").unwrap();
        let path1 = file1.path().to_path_buf();

        let mut file2 = NamedTempFile::new().unwrap();
        writeln!(file2, "Synaptic Pruning handles automatic archival over 90 days.").unwrap();
        let path2 = file2.path().to_path_buf();

        // 4. Process both files simulating the "on file save" scenario
        process_file(path1, Arc::clone(&model), Arc::clone(&db_arc))
            .await
            .expect("Failed to process file 1");

        process_file(path2, Arc::clone(&model), Arc::clone(&db_arc))
            .await
            .expect("Failed to process file 2");

        // Validate that both entered the DB
        let db_lock = db_arc.lock().await;
        let count: i32 = db_lock.query_row("SELECT COUNT(*) FROM docs", rusqlite::params![], |row| row.get(0)).unwrap();
        assert_eq!(count, 2);
    }
}
