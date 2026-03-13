package com.akmeczo.votersystem.server

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.Instant

internal object ApiJson {
    @PublishedApi
    internal val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .create()

    fun encode(value: Any?): String = gson.toJson(value)

    internal inline fun <reified T> decode(json: String): T =
        gson.fromJson(json, object : TypeToken<T>() {}.type)

    private class InstantTypeAdapter : TypeAdapter<Instant>() {
        override fun write(out: JsonWriter, value: Instant?) {
            if (value == null) {
                out.nullValue()
                return
            }

            out.value(value.toString())
        }

        override fun read(reader: JsonReader): Instant? {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                return null
            }

            return Instant.parse(reader.nextString())
        }
    }
}
