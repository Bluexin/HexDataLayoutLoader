package be.bluexin.layoutloader.json

import kotlin.math.pow

private fun Structure.expand(): List<Size> = structureRef.fields.map {  f ->
    Size(
        "${name}-${f.name}",
        listOfNotNull(description, f.description).joinToString("\n"),
        offset + f.offset, f.size, export
    )
}

fun List<Field>.expandRepeats(): Sequence<Size> = asSequence().flatMap {
    when (it) {
        is RepeatedLookupField -> (1..it.repeat).map { i ->
            Lookup(it.name(i), it.description, it.offset + (i - 1) * it.size, it.lookup, it.export).apply {
                lookupRef = it.lookupRef
            }
        }

        is RepeatedSizeField -> (1..it.repeat).map { i ->
            Size(it.name(i), it.description, it.offset + (i - 1) * it.size, it.size, it.export)
        }

        is RepeatedStructureField -> (1..it.repeat).flatMap { i ->
            Structure(it.name(i), it.description, it.offset + (i - 1) * it.size, it.structure).apply {
                structureRef = it.structureRef
            }.expand()
        }

        is Repeated -> error("Unknown repeated field $it")
        is Structure -> if (it.structure == "stringRef") listOf(it) else it.expand()
        else -> listOf(it as Size)
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

fun Size.read(from: String) = from.substring(this).readBE()

fun Size.write(value: ULong) = value.toString(16).padStart(characterSize, '0').uppercase()
    .chunked(2).reversed()
    .joinToString(separator = "")

fun Size.write(value: ULong, to: String) = to.replaceRange(characterOffset, maxCharacter, write(value))
