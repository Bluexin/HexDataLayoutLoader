package be.bluexin.layoutloader.json

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.function.Predicate

class ServerFileLayout(
    serverFiles: String,
) : Named {
    @JsonAnyGetter
    @JsonAnySetter
    val fields: MutableMap<String, AttributeType> = mutableMapOf()

    @JsonIgnore
    override lateinit var name: String

    val serverFiles = serverFiles.toRegex()
}

enum class AttributeType(private val matcher: Predicate<String>, val map: (String) -> Any) {
    BOOLEAN({ it == "y" || it == "n" }, { it == "y" }),
    NUMBER({ it.toLongOrNull() != null }, String::toLong),
    STRING({ true }, { it });

    fun widenIfNecessary(value: String): AttributeType =
        if (matcher.test(value)) this else findFirstMatching(value)

    fun check(value: String) = matcher.test(value)

    companion object {
        private val values = values()

        fun findFirstMatching(value: String) = values.first { it.matcher.test(value) }
    }
}
