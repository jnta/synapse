use fastembed::{TextEmbedding, InitOptions, EmbeddingModel};
use notify::{Watcher, RecursiveMode, EventKind};
use std::path::PathBuf;
use std::sync::Arc;
use tokio::sync::mpsc;
use tokio::fs;

// Learn more about Tauri commands at https://tauri.app/develop/calling-rust/
#[tauri::command]
fn greet(name: &str) -> String {
    format!("Hello, {}! You've been greeted from Rust!", name)
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .setup(|_app| {
            // Find our local vault directory
            let vault_path = std::env::current_dir().unwrap().join("vault");
            
            // Ensure the vault directory exists
            if !vault_path.exists() {
                std::fs::create_dir_all(&vault_path).unwrap();
            }

            // Spawn background task to initialize the model and watch files independently of UI
            tauri::async_runtime::spawn(async move {
                println!("🧠 Initializing fastembed LLM model (all-MiniLM-L6-v2)...");

                // Initialize the model strictly inside a blocking task (it can block the thread doing HTTP reqs/disk reads)
                let model = tokio::task::spawn_blocking(|| {
                    TextEmbedding::try_new(InitOptions {
                        model_name: EmbeddingModel::AllMiniLML6V2,
                        show_download_progress: true,
                        ..Default::default()
                    }).expect("Failed to initialize model")
                }).await.unwrap();

                let model = Arc::new(model);
                println!("✅ fastembed model initialized successfully!");

                let (tx, mut rx) = mpsc::channel(100);

                // Setup the directory watcher
                let mut watcher = notify::recommended_watcher(move |res| {
                    if let Ok(event) = res {
                        // Forward events to our async loop
                        let _ = tx.blocking_send(event);
                    }
                }).unwrap();

                watcher.watch(&vault_path, RecursiveMode::Recursive).unwrap();
                println!("👀 Watching for changes in {:?}", vault_path);

                // Main event listener loop
                while let Some(event) = rx.recv().await {
                    // Look specifically for modification events
                    if let EventKind::Modify(_) = event.kind {
                        for path in event.paths {
                            if path.extension().and_then(|s| s.to_str()) == Some("md") {
                                println!("📄 Detected change in: {:?}", path.file_name().unwrap());
                                
                                let model_clone = Arc::clone(&model);
                                let path_clone = path.clone();
                                
                                // Spawn another task to handle the actual file I/O and embedding silently
                                tokio::spawn(async move {
                                    let start = std::time::Instant::now();
                                    
                                    // Non-blocking read
                                    if let Ok(contents) = fs::read_to_string(&path_clone).await {
                                        // Push compute intensive process to a blocking thread worker
                                        let embeddings = tokio::task::spawn_blocking(move || {
                                            model_clone.embed(vec![contents], None)
                                        }).await.unwrap();
                                        
                                        match embeddings {
                                            Ok(e) => {
                                                let elapsed = start.elapsed();
                                                println!(
                                                    "⚡ Successfully generated embedding for {:?} in {:?} | Size: {} | (UI thread 100% unblocked!)", 
                                                    path_clone.file_name().unwrap(), 
                                                    elapsed, 
                                                    e[0].len()
                                                );
                                            },
                                            Err(e) => println!("Warning: Error during embedding generation: {:?}", e),
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                
                // Ensure watcher stays alive (while loop blocks dropping)
                drop(watcher);
            });

            Ok(())
        })
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![greet])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
