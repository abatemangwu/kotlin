/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("ConsoleKt")

package kotlin.io

import java.io.InputStream
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public actual inline fun print(message: Any?) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Int) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Long) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Byte) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Short) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Char) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Boolean) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Float) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: Double) {
    System.out.print(message)
}

/** Prints the given [message] to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun print(message: CharArray) {
    System.out.print(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public actual inline fun println(message: Any?) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Int) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Long) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Byte) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Short) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Char) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Boolean) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Float) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: Double) {
    System.out.println(message)
}

/** Prints the given [message] and the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public inline fun println(message: CharArray) {
    System.out.println(message)
}

/** Prints the line separator to the standard output stream. */
@kotlin.internal.InlineOnly
public actual inline fun println() {
    System.out.println()
}

private const val BUFFER_SIZE: Int = 32
private const val LINE_SEPARATOR_MAX_LENGTH: Int = 2

private val decoder: CharsetDecoder by lazy { Charset.defaultCharset().newDecoder() }

/**
 * Reads a line of input from the standard input stream.
 *
 * @return the line read or `null` if the input stream is redirected to a file and the end of file has been reached.
 */
fun readLine(): String? = readLine(System.`in`, decoder)

internal fun readLine(inputStream: InputStream, decoder: CharsetDecoder): String? {
    require(decoder.maxCharsPerByte() <= 1) { "Encodings with multiple chars per byte are not supported" }

    val byteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
    val charBuffer = CharBuffer.allocate(LINE_SEPARATOR_MAX_LENGTH * 2) // twice for surrogate pairs
    val stringBuilder = StringBuilder()

    var read = inputStream.read()
    if (read == -1) return null
    do {
        byteBuffer.put(read.toByte())
        if (decoder.tryDecode(byteBuffer, charBuffer, false)) {
            if (charBuffer.endsWithLineSeparator()) {
                break
            }
            if (charBuffer.remaining() < 2) {
                charBuffer.offloadPrefixTo(stringBuilder)
            }
        }
        read = inputStream.read()
    } while (read != -1)

    with(decoder) {
        tryDecode(byteBuffer, charBuffer, true) // throws exception if undecoded bytes are left
        reset()
    }

    with(charBuffer) {
        var length = position()
        if (length > 0 && get(length - 1) == '\n') {
            length--
            if (length > 0 && get(length - 1) == '\r') {
                length--
            }
        }
        flip()
        repeat(length) {
            stringBuilder.append(get())
        }
    }

    return stringBuilder.toString()
}

private fun CharsetDecoder.tryDecode(byteBuffer: ByteBuffer, charBuffer: CharBuffer, isEndOfStream: Boolean): Boolean {
    val positionBefore = charBuffer.position()
    byteBuffer.flip()
    with(decode(byteBuffer, charBuffer, isEndOfStream)) {
        if (isError) throwException()
    }
    return (charBuffer.position() > positionBefore).also { isDecoded ->
        if (isDecoded) byteBuffer.clear() else byteBuffer.flipBack()
    }
}

private fun CharBuffer.endsWithLineSeparator(): Boolean {
    val p = position()
    return p > 0 && get(p - 1) == '\n'
}

private fun Buffer.flipBack() {
    position(limit())
    limit(capacity())
}

/** Extracts everything except the last char into [builder]. */
private fun CharBuffer.offloadPrefixTo(builder: StringBuilder) {
    flip()
    repeat(limit() - 1) {
        builder.append(get())
    }
    compact()
}
