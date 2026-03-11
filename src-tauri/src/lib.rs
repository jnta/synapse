//! Application library entry point where Tauri builder is configured.

use tauri::{Manager, State};
use std::path::PathBuf;
use std::sync::Arc;
use tokio::sync::Mutex;
use rusqlite::Connection;
use fastembed::TextEmbedding;
use serde::Serialize;

pub mod core;

struct AppState {
    db: Arc<Mutex<Connection>>,
    model: Arc<TextEmbedding>,
    vault_path: PathBuf,
}

#[derive(Serialize)]
struct VaultNotes {
    daily: Vec<core::vault::NoteEntry>,
    resources: Vec<core::vault::NoteEntry>,
    projects: Vec<core::vault::NoteEntry>,
}

#[tauri::command]
async fn list_vault_notes(state: State<'_, AppState>) -> Result<VaultNotes, String> {
    let vp = &state.vault_path;
    Ok(VaultNotes {
        daily: core::vault::list_notes("daily-notes", vp),
        resources: core::vault::list_notes("resource-notes", vp),
        projects: core::vault::list_notes("project-notes", vp),
    })
}

#[tauri::command]
async fn read_note(folder: String, name: String, state: State<'_, AppState>) -> Result<String, String> {
    core::vault::read_note(&folder, &name, &state.vault_path).map_err(|e| e.to_string())
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

            core::vault::ensure_vault_dirs(&vault_path);

            let db = core::db::init_db(&vault_path).expect("Failed to init DB");
            let db_arc = Arc::new(Mutex::new(db));

            let handle = app.handle().clone();
            let vault_path_clone = vault_path.clone();

            tauri::async_runtime::spawn(async move {
                let model = crate::core::embedding::init_model().await;

                handle.manage(AppState {
                    db: Arc::clone(&db_arc),
                    model: Arc::clone(&model),
                    vault_path: vault_path_clone,
                });

                crate::core::watcher::start_watching(vault_path, model, db_arc).await;
            });

            Ok(())
        })
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![search_notes, list_vault_notes, read_note])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}

