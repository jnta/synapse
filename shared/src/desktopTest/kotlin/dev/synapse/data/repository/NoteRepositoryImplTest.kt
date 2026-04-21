package dev.synapse.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.model.Attribute
import dev.synapse.domain.model.Edge
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteCollection
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class NoteRepositoryImplTest {
    private lateinit var database: SynapseDatabase
    private lateinit var repository: NoteRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SynapseDatabase.Schema.create(driver)
        database = SynapseDatabase(driver)
        repository = NoteRepositoryImpl(database)
    }

    @Test
    fun `saveNote and getNoteById should persist all fields correctly`() = runTest {
        val note = Note(
            id = "test-1",
            title = "Test Note",
            content = "Content with [[Link]]",
            attributes = listOf(Attribute("a1", "key", "value")),
            connections = listOf(Edge("e1", "test-1", "target", "label")),
            createdAt = 1000L,
            updatedAt = 2000L
        )

        repository.saveNote(note)
        val retrieved = repository.getNoteById("test-1")

        retrieved shouldNotBe null
        val n = retrieved!!
        n.title shouldBe "Test Note"
        n.content shouldBe "Content with [[Link]]"
        n.attributes shouldHaveSize 1
        n.attributes.first().key shouldBe "key"
        n.connections shouldHaveSize 1
        n.connections.first().targetId shouldBe "target"
    }

    @Test
    fun `saveNote should update existing note and replace attributes and edges`() = runTest {
        val initialNote = Note(
            id = "test-1",
            title = "Initial",
            content = "Old",
            attributes = listOf(Attribute("a1", "old-key", "old-value")),
            connections = listOf(Edge("e1", "test-1", "old-target", "old-label")),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.saveNote(initialNote)

        val updatedNote = initialNote.copy(
            title = "Updated",
            attributes = listOf(Attribute("a2", "new-key", "new-value")),
            connections = listOf(Edge("e2", "test-1", "new-target", "new-label"))
        )
        repository.saveNote(updatedNote)

        val retrieved = repository.getNoteById("test-1")
        retrieved shouldNotBe null
        val n = retrieved!!
        n.title shouldBe "Updated"
        n.attributes shouldHaveSize 1
        n.attributes.first().key shouldBe "new-key"
        n.connections shouldHaveSize 1
        n.connections.first().targetId shouldBe "new-target"
    }

    @Test
    fun `deleteNote should remove note and its related data`() = runTest {
        val note = Note(
            id = "test-1",
            title = "To Delete",
            content = "Delete me",
            attributes = listOf(Attribute("a1", "k", "v")),
            connections = listOf(Edge("e1", "test-1", "t", "l")),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.saveNote(note)
        
        repository.deleteNote("test-1")

        repository.getNoteById("test-1") shouldBe null
        
        val queries = database.synapseDatabaseQueries
        queries.getAttributesForNote("test-1").executeAsList() shouldHaveSize 0
        queries.getEdgesForNote("test-1", "test-1").executeAsList() shouldHaveSize 0
    }

    @Test
    fun `collection operations should work correctly`() = runTest {
        val collection = NoteCollection("c1", "Inbox", "#FFFFFF")
        repository.saveCollection(collection)

        val collections = repository.getCollections().first()
        collections shouldHaveSize 1
        collections.first().name shouldBe "Inbox"

        repository.isCollectionEmpty("c1") shouldBe true

        val note = Note(
            id = "n1", 
            title = "N1", 
            content = "",
            collectionId = "c1",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.saveNote(note)

        repository.isCollectionEmpty("c1") shouldBe false

        repository.deleteCollection("c1")
        repository.getCollections().first() shouldHaveSize 0
    }
}
