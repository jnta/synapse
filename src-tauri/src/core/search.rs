use fastembed::TextEmbedding;
use rusqlite::Connection;
use std::sync::Arc;
use tokio::sync::Mutex;

use crate::core::error::AppError;

pub async fn search_notes(
    query: String,
    limit: usize,
    db: Arc<Mutex<Connection>>,
    model: Arc<TextEmbedding>,
) -> Result<Vec<(String, f32)>, AppError> {
    let query_embedding = tokio::task::spawn_blocking(move || {
        model.embed(vec![query], None)
    })
    .await
    .map_err(|e| AppError::EmbeddingError(format!("Task spawn failed: {}", e)))?
    .map_err(|e| AppError::EmbeddingError(format!("Embedding generation failed: {}", e)))?
    .pop()
    .ok_or_else(|| AppError::EmbeddingError("No embedding returned".to_string()))?;

    let db_lock = db.lock().await;
    let results = crate::core::db::search_similar(&db_lock, &query_embedding, limit)?;
    Ok(results)
}
