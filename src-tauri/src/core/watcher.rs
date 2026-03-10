//! Background file watcher for the vault directory.

use fastembed::TextEmbedding;
use notify::{Event, EventKind, RecursiveMode, Watcher};
use std::path::PathBuf;
use std::sync::Arc;
use tokio::fs;
use tokio::sync::mpsc;

/// Processes a single file to generate embeddings.
pub async fn process_file(path: PathBuf, model: Arc<TextEmbedding>) -> Result<Vec<f32>, String> {
    let start = std::time::Instant::now();

    let contents = fs::read_to_string(&path)
        .await
        .map_err(|e| format!("Failed to read file: {}", e))?;

    let embeddings = tokio::task::spawn_blocking(move || model.embed(vec![contents], None))
        .await
        .map_err(|e| format!("Task spawn failed: {}", e))?;

    match embeddings {
        Ok(mut e) => {
            let elapsed = start.elapsed();
            let embedding = e.pop().unwrap();
            println!(
                "Generated embedding for {:?} in {:?} | Size: {}",
                path.file_name().unwrap_or_default(),
                elapsed,
                embedding.len()
            );
            Ok(embedding)
        }
        Err(err) => Err(format!("Error during embedding generation: {:?}", err)),
    }
}

/// Starts a background directory watcher that generates embeddings when markdown files change.
pub async fn start_watching(vault_path: PathBuf, model: Arc<TextEmbedding>) {
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
                    let path_clone = path.clone();

                    tokio::spawn(async move {
                        if let Err(e) = process_file(path_clone, model_clone).await {
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

        // 2. Create two temporary files with different textual contents
        let mut file1 = NamedTempFile::new().unwrap();
        writeln!(file1, "The core philosophy is Friction as a Feature.").unwrap();
        let path1 = file1.path().to_path_buf();

        let mut file2 = NamedTempFile::new().unwrap();
        writeln!(file2, "Synaptic Pruning handles automatic archival over 90 days.").unwrap();
        let path2 = file2.path().to_path_buf();

        // 3. Process both files simulating the "on file save" scenario
        let embedding1 = process_file(path1, Arc::clone(&model))
            .await
            .expect("Failed to process file 1");

        let embedding2 = process_file(path2, Arc::clone(&model))
            .await
            .expect("Failed to process file 2");

        // 4. Validate they exist and have exactly the same dimension length (all-MiniLM-L6-v2 uses 384 dimensions)
        assert_eq!(embedding1.len(), 384);
        assert_eq!(embedding2.len(), 384);

        // 5. Validation: Two different files should result in two different embeddings
        assert_ne!(
            embedding1, embedding2,
            "Two different files must not have the identical embeddings"
        );
    }
}
