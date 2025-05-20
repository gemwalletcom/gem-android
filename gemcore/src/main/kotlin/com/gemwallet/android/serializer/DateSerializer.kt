package com.gemwallet.android.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Long::class)
object DateSerializer {

    override fun deserialize(decoder: Decoder): Long {
        return ZonedDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_ZONED_DATE_TIME)
            .toInstant()
            .toEpochMilli()

    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeString(value.toString())
    }
}