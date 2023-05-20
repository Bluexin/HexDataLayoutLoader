package be.bluexin.layoutloader

import be.bluexin.layoutloader.json.Named
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import io.github.oshai.KotlinLogging
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import kotlin.io.path.pathString

object LayoutLoader {
    private val objectMapper = jacksonObjectMapper().apply {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    }
    private val logger = KotlinLogging.logger {}

    private fun <T : Named> File.read(typeRef: TypeReference<T>, init: (T) -> Unit): Pair<String, T>? = try {
        val read = objectMapper.readValue(this, typeRef)
        read.name = this.nameWithoutExtension
        init(read)
        logger.info { "Loaded ${read.name} (${this})" }
        read.name to read
    } catch (e: Exception) {
        logger.warn(e) { "Failed to load ${this.name}" }
        null
    }

    @OptIn(FlowPreview::class)
    suspend fun <T : Named> load(
        root: File,
        directory: String?,
        typeRef: TypeReference<T>,
        init: (T) -> Unit
    ): Flow<Map<String, T>> {
        if (directory == null) return emptyFlow()
        logger.info { "Loading ${typeRef.type.typeName}" }

        val realDir = File(root, directory)
        return if (realDir.isDirectory) withContext(Dispatchers.IO) {
            val initial = realDir.walk().filter { !it.isDirectory }.mapNotNull { it.read(typeRef, init) }.toMap()

            flow {
                val path = realDir.toPath()
                path.watchEvents(ENTRY_CREATE, ENTRY_MODIFY) { event ->
                    val eventPath = event.context() as? Path
                    if (eventPath != null) {
                        logger.debug { "Event: ${event.kind()} x${event.count()} for $eventPath" }
                        if (eventPath.pathString.endsWith(".json")) emit(path.resolve(eventPath).toFile())
                    }
                }
            }.flowOn(Dispatchers.IO)
                .debounce(3_000L)
                .mapNotNull { it.read(typeRef, init) }
                .runningFold(initial) { acc, it -> acc + it }
        } else emptyFlow()
    }

    suspend inline fun <reified T : Named> load(
        root: File,
        directory: String?,
        noinline init: (T) -> Unit = {}
    ): Flow<Map<String, T>> = load(root, directory, jacksonTypeRef(), init)
}