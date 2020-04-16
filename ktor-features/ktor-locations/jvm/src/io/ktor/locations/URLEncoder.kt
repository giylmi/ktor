/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.locations

import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.*
import kotlinx.serialization.Encoder
import kotlinx.serialization.modules.*

@KtorExperimentalLocationsAPI
internal class URLEncoder(
    override val context: SerialModule,
    private val conversionService: ConversionService = DefaultConversionService
) : Encoder, CompositeEncoder {
    private val pathParameters = ParametersBuilder()
    private val queryParameters = ParametersBuilder()

    private var pattern: LocationPattern? = null
    private var currentElementName: String? = null

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder {
        if (desc.kind == StructureKind.CLASS && this.pattern == null) {
            this.pattern = buildLocationPattern(desc)
        }

        return this
    }

    override fun encodeBoolean(value: Boolean) {
        encodeString(value.toString())
    }

    override fun encodeByte(value: Byte) {
        encodeString(value.toString())
    }

    override fun encodeChar(value: Char) {
        encodeString(value.toString())
    }

    override fun encodeDouble(value: Double) {
        encodeString(value.toString())
    }

    override fun encodeEnum(enumDescription: SerialDescriptor, ordinal: Int) {
        TODO("Not yet implemented")
    }

    override fun encodeFloat(value: Float) {
        encodeString(value.toString())
    }

    override fun encodeInt(value: Int) {
        encodeString(value.toString())
    }

    override fun encodeLong(value: Long) {
        encodeString(value.toString())
    }

    override fun encodeNotNullMark() {
        error("Encoding a primitive to URL is not supported")
    }

    override fun encodeNull() {
        error("Encoding a primitive to URL is not supported")
    }

    override fun encodeShort(value: Short) {
        encodeString(value.toString())
    }

    override fun encodeString(value: String) {
        val name = currentElementName!!
        val pattern = pattern!!

        if (name in pattern.pathParameterNames) {
            pathParameters.append(name, value)
        } else {
            queryParameters.append(name, value)
        }
    }

    override fun encodeUnit() {
        encodeString("Unit")
    }

    override fun encodeBooleanElement(desc: SerialDescriptor, index: Int, value: Boolean) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeByteElement(desc: SerialDescriptor, index: Int, value: Byte) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeCharElement(desc: SerialDescriptor, index: Int, value: Char) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeDoubleElement(desc: SerialDescriptor, index: Int, value: Double) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeFloatElement(desc: SerialDescriptor, index: Int, value: Float) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeIntElement(desc: SerialDescriptor, index: Int, value: Int) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeLongElement(desc: SerialDescriptor, index: Int, value: Long) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeNonSerializableElement(desc: SerialDescriptor, index: Int, value: Any) {
        TODO("Not yet implemented")
    }

    override fun <T : Any> encodeNullableSerializableElement(
        desc: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value != null) {
            encodeSerializableElement(desc, index, serializer, value)
        }
    }

    override fun <T> encodeSerializableElement(
        desc: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val before = currentElementName
        val name = when (desc.kind) {
            StructureKind.CLASS -> desc.getElementName(index)
            else -> before
        }

        currentElementName = name
        try {
            val elementDescriptor = desc.getElementDescriptor(index)
            if (name != null && elementDescriptor.location == null && elementDescriptor.kind == StructureKind.CLASS) {
                try {
                    conversionService.toValues(value).forEach { valueComponent ->
                        encodeElement(name, valueComponent)
                    }

                    return
                } catch (_: DataConversionException) {
                }
            }

            serializer.serialize(this, value)
        } finally {
            currentElementName = before
        }
    }

    override fun encodeShortElement(desc: SerialDescriptor, index: Int, value: Short) {
        encodeElement(desc, index, value.toString())
    }

    override fun encodeStringElement(desc: SerialDescriptor, index: Int, value: String) {
        encodeElement(desc, index, value)
    }

    override fun encodeUnitElement(desc: SerialDescriptor, index: Int) {
        encodeElement(desc, index, "")
    }

    private fun encodeElement(desc: SerialDescriptor, index: Int, stringified: String) {
        val name = desc.getElementName(index)
        encodeElement(name, stringified)
    }

    private fun encodeElement(name: String, stringified: String) {
        val pattern = pattern ?: error("No @Location annotation found")

        if (name in pattern.pathParameterNames) {
            pathParameters[name] = stringified
        } else {
            queryParameters.append(name, stringified)
        }
    }

    fun build(): Url {
        val pattern = pattern ?: error("No @Location annotation found.")

        val builder = URLBuilder(
            parameters = queryParameters,
            encodedPath = pattern.format(pathParameters.build())
        )

        return builder.build()
    }
}

