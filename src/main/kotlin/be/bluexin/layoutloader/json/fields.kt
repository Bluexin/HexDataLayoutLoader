package be.bluexin.layoutloader.json

import kotlin.math.pow

fun List<Field>.expandRepeats() = asSequence().flatMap {
    when (it) {
        is RepeatedLookupField -> (1..it.repeat).map { i ->
            Lookup(it.name(i), it.description, it.offset + it.repeat * it.size, it.lookup).apply {
                lookupRef = it.lookupRef
            }
        }

        is RepeatedSizeField -> (1..it.repeat).map { i ->
            Size(it.name(i), it.description, it.offset + it.repeat * it.size, it.size)
        }

        is RepeatedStructureField -> (1..it.repeat).map { i ->
            Structure(it.name(i), it.description, it.offset + it.repeat * it.size, it.structure).apply {
                structureRef = it.structureRef
            }
        }

        is Repeated -> error("Unknown repeated field $it")
        else -> listOf(it)
    }
}

val Size.characterOffset get() = offset * 2

val Size.maxValue get() = (2.0.pow(size * 4) - 1).toULong()
val Size.characterSize get() = size * 2
val Size.maxCharacter get() = characterOffset + characterSize
fun Size.write(value: UInt, text: String): String =
    if (value <= maxValue && maxCharacter <= text.length) {
        text.replaceRange(
            offset, maxCharacter,
            value.toString(16).uppercase().padStart(size, '0').chunked(2).reversed()
                .joinToString(separator = "")
        )
    } else text

fun String.substring(field: Size) =
    if (field.maxCharacter <= length) substring(field.characterOffset, field.maxCharacter)
    else ""

fun String.readBE() = ifEmpty { "0" }
    .chunked(2).reversed()
    .joinToString(separator = "").toULong(16)
    .toString()
