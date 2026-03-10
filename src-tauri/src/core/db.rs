use rusqlite::{ffi::sqlite3_auto_extension, params, Connection, Result as SqliteResult};
use sqlite_vec::sqlite3_vec_init;
use std::path::Path;
use zerocopy::IntoBytes;


pub fn init_db(vault_path: &Path) -> SqliteResult<Connection> {
    unsafe {
        sqlite3_auto_extension(Some(std::mem::transmute(sqlite3_vec_init as *const ())));
    }
    let db_path = vault_path.join(".cerebro.db");
    let db = Connection::open(&db_path)?;
    db.execute_batch("PRAGMA journal_mode = WAL;")?;
    db.execute_batch(
        "
        BEGIN;
        CREATE TABLE IF NOT EXISTS docs(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            path TEXT UNIQUE
        );
        CREATE VIRTUAL TABLE IF NOT EXISTS vec_docs USING vec0(
            embedding float[384]
        );
        COMMIT;
        "
    )?;
    Ok(db)
}

pub fn upsert_embedding(conn: &Connection, path: &Path, embedding: &[f32]) -> SqliteResult<()> {
    let path_str = path.to_string_lossy().to_string();
    conn.execute(
        "INSERT OR IGNORE INTO docs (path) VALUES (?1)",
        params![&path_str],
    )?;
    let id: i64 = conn.query_row(
        "SELECT id FROM docs WHERE path = ?1",
        params![&path_str],
        |row| row.get(0),
    )?;
    conn.execute(
        "INSERT OR REPLACE INTO vec_docs (rowid, embedding) VALUES (?1, ?2)",
        params![id, embedding.as_bytes()],
    )?;
    Ok(())
}

pub fn search_similar(conn: &Connection, query_embedding: &[f32], limit: usize) -> SqliteResult<Vec<(String, f32)>> {
    let mut stmt = conn.prepare(
        "
        SELECT docs.path, vec_distance_cosine(vec_docs.embedding, ?1) as distance
        FROM vec_docs
        JOIN docs ON docs.id = vec_docs.rowid
        WHERE vec_docs.embedding MATCH ?1 AND k = ?2
        ORDER BY distance
        "
    )?;
    let results = stmt.query_map(
        params![query_embedding.as_bytes(), limit as i64],
        |row| {
            let path: String = row.get(0)?;
            let distance: f32 = row.get(1)?;
            Ok((path, distance))
        }
    )?;
    let mut matches = Vec::new();
    for result in results {
        matches.push(result?);
    }
    Ok(matches)
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::path::PathBuf;

    #[test]
    fn test_vec_insertion_and_search() {
        unsafe { 
            sqlite3_auto_extension(Some(std::mem::transmute(sqlite3_vec_init as *const ())));
        }
        let conn = Connection::open_in_memory().unwrap();
        
        conn.execute_batch(
            "
            CREATE TABLE docs(id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE);
            CREATE VIRTUAL TABLE vec_docs USING vec0(embedding float[3]);
            "
        ).unwrap();

        let path1 = PathBuf::from("doc1.md");
        let path2 = PathBuf::from("doc2.md");
        let vec1 = vec![1.0, 0.0, 0.0];
        let vec2 = vec![0.0, 1.0, 0.0];

        upsert_embedding(&conn, &path1, &vec1).unwrap();
        upsert_embedding(&conn, &path2, &vec2).unwrap();

        let query = vec![1.0, 0.1, 0.0];
        let results = search_similar(&conn, &query, 1).unwrap();

        assert_eq!(results.len(), 1);
        assert_eq!(results[0].0, "doc1.md");
    }
}
