/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.http

import io.ktor.util.*

/**
 * Construct [Url] from [urlString].
 */
@Suppress("FunctionName")
fun Url(urlString: String): Url = URLBuilder(urlString).build()

/**
 * Construct [Url] from [builder] without building origin.
 */
@Suppress("FunctionName")
fun Url(builder: URLBuilder): Url = URLBuilder().takeFrom(builder).build()

/**
 * Construct [URLBuilder] from [urlString].
 */
@Suppress("FunctionName")
fun URLBuilder(urlString: String): URLBuilder = URLBuilder().takeFrom(urlString)

/**
 * Construct [URLBuilder] from [url].
 */
@Suppress("FunctionName")
fun URLBuilder(url: Url): URLBuilder = URLBuilder().takeFrom(url)

/**
 * Construct [URLBuilder] from [builder].
 */
@Suppress("FunctionName")
fun URLBuilder(builder: URLBuilder): URLBuilder = URLBuilder().takeFrom(builder)

/**
 * Take components from another [url] builder
 */
fun URLBuilder.takeFrom(url: URLBuilder): URLBuilder {
    protocol = url.protocol
    host = url.host
    port = url.port
    encodedPath = url.encodedPath
    user = url.user
    password = url.password
    parameters.appendAll(url.parameters)
    fragment = url.fragment
    trailingQuery = url.trailingQuery

    return this
}

/**
 * Take components from another [url]
 */
fun URLBuilder.takeFrom(url: Url): URLBuilder {
    protocol = url.protocol
    host = url.host
    port = url.specifiedPort
    encodedPath = url.encodedPath
    user = url.user
    password = url.password
    parameters.appendAll(url.parameters)
    fragment = url.fragment
    trailingQuery = url.trailingQuery

    return this
}

/**
 * Full encoded path with query string but without domain, port and schema
 */
val Url.fullPath: String
    get() = buildString { appendUrlFullPath(encodedPath, parameters, trailingQuery) }

/**
 * Host:port pair, not normalized so port is always specified even if the port is schema's default
 */
val Url.hostWithPort: String get() = "$host:$port"

/**
 * Append URL full path with query parameters. No host/port/auth/scheme will be appended.
 */
internal fun Appendable.appendUrlFullPath(
    encodedPath: String,
    queryParameters: Parameters,
    trailingQuery: Boolean
) {
    if (encodedPath.isNotBlank() && !encodedPath.startsWith("/")) {
        append('/')
    }

    append(encodedPath)

    if (!queryParameters.isEmpty() || trailingQuery) {
        append("?")
    }

    queryParameters.formUrlEncodeTo(this)
}
