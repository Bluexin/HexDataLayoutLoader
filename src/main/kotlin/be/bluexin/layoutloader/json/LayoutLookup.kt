package be.bluexin.layoutloader.json

import com.fasterxml.jackson.annotation.JsonIgnore

data class LayoutLookup(
    override var name: String,
    var size: Int,
    val values: MutableList<String>
) : Named {
    @JsonIgnore
    val indexedValues = values.mapIndexed { index, s -> s to index }.toMap()
}