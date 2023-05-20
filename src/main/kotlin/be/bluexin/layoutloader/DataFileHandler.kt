package be.bluexin.layoutloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createTempFile
import kotlin.io.path.moveTo

object DataFileHandler {

    suspend fun update(file: File, transform: (String) -> String) {
        withContext(Dispatchers.IO) {
            val writeFile = createTempFile(suffix = ".${file.extension}")
            file.bufferedReader().use {  reader ->
                writeFile.bufferedWriter().use { writer ->
                    var line = reader.readLine()
                    while (line != null && line.isNotEmpty()) {
                        if (line.endsWith(DATA_END)) {
                            val startIndex = line.indexOf(DATA_START) + DATA_START.length
                            val endIndex = line.length - DATA_END.length
                            writer.append(line.substring(0, startIndex))
                            writer.append(transform(line.substring(startIndex, endIndex)))
                            writer.append(line.substring(endIndex))
                        } else writer.append(line)
                        line = reader.readLine()
                        if (line != null) writer.appendLine()
                    }
                }
            }
            writeFile.moveTo(file.toPath(), overwrite = true)
        }
    }

    private const val DATA_START = "<data>"
    private const val DATA_END = "</data>"
}
