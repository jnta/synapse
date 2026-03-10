//! Application library entry point where Tauri builder is configured.

pub mod core;

/// Sets up the Tauri application builder and runs the app.
#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .setup(|_app| {
            let vault_path = std::env::current_dir().unwrap().join("vault");

            if !vault_path.exists() {
                std::fs::create_dir_all(&vault_path).unwrap();
            }

            tauri::async_runtime::spawn(async move {
                let model = crate::core::embedding::init_model().await;
                crate::core::watcher::start_watching(vault_path, model).await;
            });

            Ok(())
        })
        .plugin(tauri_plugin_opener::init())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
