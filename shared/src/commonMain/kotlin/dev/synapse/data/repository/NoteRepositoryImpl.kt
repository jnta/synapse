package dev.synapse.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.Attribute
import dev.synapse.domain.model.Edge
import dev.synapse.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val database: SynapseDatabase
) : NoteRepository {
    private val queries = database.synapseDatabaseQueries

    override fun getAllNotes(): Flow<List<Note>> {
        return queries.getAllNotes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes ->
                notes.map { noteEntity ->
                    val attributes = queries.getAttributesForNote(noteEntity.id).executeAsList().map {
                        Attribute(it.id, it.attr_key, it.attr_value)
                    }
                    val edges = queries.getEdgesForNote(noteEntity.id, noteEntity.id).executeAsList().map {
                        Edge(it.id, it.source_id, it.target_id, it.label)
                    }
                    Note(
                        noteEntity.id,
                        noteEntity.title,
                        noteEntity.content_raw,
                        attributes,
                        edges,
                        noteEntity.created_at,
                        noteEntity.updated_at,
                        noteEntity.view_count.toInt()
                    )
                }
            }
    }

    override suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        val noteEntity = queries.getNoteById(id).executeAsOneOrNull() ?: return@withContext null
        val attributes = queries.getAttributesForNote(id).executeAsList().map {
            Attribute(it.id, it.attr_key, it.attr_value)
        }
        val edges = queries.getEdgesForNote(id, id).executeAsList().map {
            Edge(it.id, it.source_id, it.target_id, it.label)
        }
        Note(
            noteEntity.id,
            noteEntity.title,
            noteEntity.content_raw,
            attributes,
            edges,
            noteEntity.created_at,
            noteEntity.updated_at,
            noteEntity.view_count.toInt()
        )
    }

    override suspend fun saveNote(note: Note) = withContext(Dispatchers.IO) {
        queries.transaction {
            // Use insert or replace logic? SQLDelight insertNote uses INSERT. 
            // Better to use INSERT OR REPLACE in .sq file if possible.
            // For now I'll just delete and re-insert or assume it's an update if exists.
            // Actually, I'll update the .sq file to use INSERT OR REPLACE.
            
            queries.insertNote(
                id = note.id,
                title = note.title,
                content_raw = note.content,
                created_at = note.createdAt,
                updated_at = note.updatedAt,
                view_count = note.viewCount.toLong()
            )
            
            queries.deleteAttributesForNote(note.id)
            note.attributes.forEach {
                queries.insertAttribute(it.id, note.id, it.key, it.value)
            }
            
            queries.deleteEdgesForNote(note.id, note.id)
            note.connections.forEach {
                queries.insertEdge(it.id, it.sourceId, it.targetId, it.label)
            }
        }
    }

    override suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.deleteAttributesForNote(id)
            queries.deleteEdgesForNote(id, id)
            queries.deleteNote(id)
        }
    }
}
