package be.bluexin.layoutloader.json

import com.fasterxml.jackson.annotation.JsonIgnore

data class Settings(
    val metadata: String,
    val clientFiles: String?,
    @JsonIgnore
    val nanos: Long = System.nanoTime()
)