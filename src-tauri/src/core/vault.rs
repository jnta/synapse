use std::path::Path;
use std::time::SystemTime;
use serde::Serialize;

#[derive(Serialize, Clone)]
pub struct NoteEntry {
    pub name: String,
    pub folder: String,
    pub modified: u64,
}

pub fn ensure_vault_dirs(vault_path: &Path) {
    for folder in ["daily-notes", "resource-notes", "project-notes"] {
        let dir = vault_path.join(folder);
        if !dir.exists() {
            std::fs::create_dir_all(&dir).ok();
        }
    }
}

pub fn list_notes(folder: &str, vault_path: &Path) -> Vec<NoteEntry> {
    let dir = vault_path.join(folder);
    let mut entries: Vec<NoteEntry> = std::fs::read_dir(&dir)
        .into_iter()
        .flatten()
        .flatten()
        .filter(|e| {
            e.path().extension().and_then(|s| s.to_str()) == Some("md")
        })
        .filter_map(|e| {
            let path = e.path();
            let name = path.file_stem()?.to_string_lossy().to_string();
            let modified = e.metadata().ok()?.modified().ok()
                .and_then(|t| t.duration_since(SystemTime::UNIX_EPOCH).ok())
                .map(|d| d.as_secs())
                .unwrap_or(0);
            Some(NoteEntry { name, folder: folder.to_string(), modified })
        })
        .collect();

    entries.sort_by(|a, b| b.modified.cmp(&a.modified));
    entries
}

pub fn read_note(folder: &str, name: &str, vault_path: &Path) -> std::io::Result<String> {
    let path = vault_path.join(folder).join(format!("{}.md", name));
    std::fs::read_to_string(path)
}
