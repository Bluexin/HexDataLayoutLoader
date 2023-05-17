package be.bluexin.layoutloader.json

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.oshai.KotlinLogging

class DataLayout(
    val fields: List<Field>,
    val filter: Map<String, String> = emptyMap(),
    val clientFile: String
) : Named {
    @JsonAnyGetter
    @JsonAnySetter
    val extra: MutableMap<String, String> = mutableMapOf()

    var loadingTimeStamp = 0L
        private set

    @JsonIgnore
    override lateinit var name: String

    fun load(metadata: Metadata, structures: Map<String, LayoutStructure>, lookups: Map<String, LayoutLookup>) {
        logger.info { "Resolving $name" }
        fields.forEach {
            try {
                when (it) {
                    is Structure -> it.structureRef = structures[it.structure]
                        ?: error("Missing referenced structure ${it.structure}")
                    is Lookup -> it.lookupRef = lookups[it.lookup]
                        ?: error("Missing referenced lookup ${it.lookup}")

                    else -> Unit
                }
            } catch (e: Exception) {
                logger.info { "\t${e.message}" }
            }
        }
        loadingTimeStamp = System.nanoTime()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataLayout) return false

        if (fields != other.fields) return false
        if (extra != other.extra) return false
        if (loadingTimeStamp != other.loadingTimeStamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fields.hashCode()
        result = 31 * result + extra.hashCode()
        result = 31 * result + loadingTimeStamp.hashCode()
        return result
    }
}

private val logger = KotlinLogging.logger { }