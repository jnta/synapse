//! Embedding model initialization and execution logic.

use fastembed::{EmbeddingModel, InitOptions, TextEmbedding};
use std::sync::Arc;

/// Initializes the text embedding model using fastembed.
pub async fn init_model() -> Arc<TextEmbedding> {
    let model = tokio::task::spawn_blocking(|| {
        TextEmbedding::try_new(
            InitOptions::new(EmbeddingModel::AllMiniLML6V2).with_show_download_progress(true),
        )
        .expect("Failed to initialize model")
    })
    .await
    .unwrap();

    Arc::new(model)
}
