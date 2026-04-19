package dev.synapse.data.repository

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.repository.ResonanceRepository
import editor.ResonanceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import java.util.*

class ResonanceRepositoryImpl(
    private val database: SynapseDatabase
) : ResonanceRepository {
    private val queries = database.synapseDatabaseQueries
    private val env = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var tokenizer: HuggingFaceTokenizer? = null

    init {
        try {
            // Load model and tokenizer from resources
            val modelStream = this::class.java.classLoader.getResourceAsStream("model.onnx")
            val tokenizerStream = this::class.java.classLoader.getResourceAsStream("tokenizer.json")
            
            if (modelStream != null && tokenizerStream != null) {
                session = env.createSession(modelStream.readAllBytes())
                
                // Save tokenizer to temp file as DJL expects a path
                val tempFile = java.io.File.createTempFile("tokenizer", ".json")
                tempFile.deleteOnExit()
                tempFile.outputStream().use { tokenizerStream.copyTo(it) }
                tokenizer = HuggingFaceTokenizer.newInstance(tempFile.toPath())
            }
        } catch (e: Exception) {
            println("Failed to initialize ResonanceRepository: ${e.message}")
        }
    }

    override suspend fun getResonance(text: String): List<ResonanceItem> = withContext(Dispatchers.IO) {
        val queryEmbedding = generateEmbedding(text) ?: return@withContext emptyList()
        
        val allNotes = queries.getAllNotes().executeAsList()
        val results = allNotes.mapNotNull { noteEntity ->
            val embeddingBlob = queries.getEmbedding(noteEntity.id).executeAsOneOrNull()
            val embedding = embeddingBlob?.let { fromBlob(it) } ?: return@mapNotNull null
            val similarity = cosineSimilarity(queryEmbedding, embedding)
            if (similarity > 0.6) {
                ResonanceItem(
                    id = noteEntity.id,
                    title = noteEntity.title,
                    snippet = noteEntity.content_raw.take(100),
                    tags = emptyList() // Could extract from attributes
                ) to similarity
            } else null
        }.sortedByDescending { it.second }
        .take(5)
        .map { it.first }
        
        results
    }

    override suspend fun updateEmbedding(noteId: String, content: String) = withContext(Dispatchers.IO) {
        val embedding = generateEmbedding(content) ?: return@withContext
        queries.insertEmbedding(noteId, toBlob(embedding))
    }

    private fun generateEmbedding(text: String): FloatArray? {
        val tok = tokenizer ?: return null
        val sess = session ?: return null
        
        try {
            val encoding = tok.encode(text)
            val ids = encoding.ids
            val mask = encoding.attentionMask
            
            val idsTensor = OnnxTensor.createTensor(env, arrayOf(ids))
            val maskTensor = OnnxTensor.createTensor(env, arrayOf(mask))
            
            val inputs = mapOf(
                "input_ids" to idsTensor,
                "attention_mask" to maskTensor
            )
            
            val result = sess.run(inputs)
            val output = result.get(0).value as Array<Array<FloatArray>> // Typical BERT output shape [batch, seq, dim]
            
            // Mean pooling
            val dim = output[0][0].size
            val pooled = FloatArray(dim)
            for (i in output[0].indices) {
                for (j in 0 until dim) {
                    pooled[j] += output[0][i][j]
                }
            }
            for (j in 0 until dim) {
                pooled[j] /= output[0].size.toFloat()
            }
            
            return normalize(pooled)
        } catch (e: Exception) {
            println("Embedding generation failed: ${e.message}")
            return null
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dotProduct = 0f
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
        }
        return dotProduct // Assuming normalized vectors
    }

    private fun normalize(v: FloatArray): FloatArray {
        var norm = 0f
        for (x in v) norm += x * x
        norm = kotlin.math.sqrt(norm)
        for (i in v.indices) v[i] /= norm
        return v
    }

    private fun toBlob(array: FloatArray): ByteArray {
        val buffer = java.nio.ByteBuffer.allocate(array.size * 4)
        array.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    private fun fromBlob(bytes: ByteArray): FloatArray {
        val buffer = java.nio.ByteBuffer.wrap(bytes)
        val array = FloatArray(bytes.size / 4)
        for (i in array.indices) {
            array[i] = buffer.getFloat()
        }
        return array
    }
}
