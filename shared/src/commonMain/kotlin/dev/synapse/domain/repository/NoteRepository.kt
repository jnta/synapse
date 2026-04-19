package dev.synapse.domain.repository

import dev.synapse.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun getNoteByTitle(title: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
    suspend fun getForwardLinks(id: String): List<Note>
    suspend fun getBackLinks(id: String): List<Note>
}
