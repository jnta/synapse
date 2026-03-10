//! Application library entry point where Tauri builder is configured.

use tauri::{Manager, State};
use std::sync::Arc;
use tokio::sync::Mutex;
use rusqlite::Connection;
use fastembed::TextEmbedding;

pub mod core;

struct AppState {
    db: Arc<Mutex<Connection>>,
    model: Arc<TextEmbedding>,
}

#[tauri::command]
async fn search_notes(
    query: String,
    limit: usize,
    state: State<'_, AppState>,
) -> Result<Vec<(String, f32)>, core::error::AppError> {
    core::search::search_notes(query, limit, Arc::clone(&state.db), Arc::clone(&state.model)).await
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .setup(|app| {
            let vault_path = std::env::current_dir().unwrap().join("vault");

            if !vault_path.exists() {
                std::fs::create_dir_all(&vault_path).unwrap();
            }

            let db = core::db::init_db(&vault_path).expect("Failed to init DB");
            let db_arc = Arc::new(Mutex::new(db));
            
            let handle = app.handle().clone();

            tauri::async_runtime::spawn(async move {
                let model = crate::core::embedding::init_model().await;
                
                handle.manage(AppState {
                    db: Arc::clone(&db_arc),
                    model: Arc::clone(&model),
                });
                
                crate::core::watcher::start_watching(vault_path, model, db_arc).await;
            });

            Ok(())
        })
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![search_notes])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
