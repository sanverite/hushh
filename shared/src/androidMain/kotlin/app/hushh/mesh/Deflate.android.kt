package app.hushh.mesh

import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

internal actual fun deflateRaw(data: ByteArray): ByteArray {
    val deflater = Deflater(5, true)
    try {
        deflater.setInput(data)
        deflater.finish()
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) out.write(buffer, 0, deflater.deflate(buffer))
        return out.toByteArray()
    } finally {
        deflater.end()
    }
}

internal actual fun inflateRaw(data: ByteArray, originalSize: Int): ByteArray? {
    val inflater = Inflater(true)
    try {
        inflater.setInput(data)
        val out = ByteArray(originalSize)
        var size = 0
        while (size < originalSize && !inflater.finished()) {
            val n = inflater.inflate(out, size, originalSize - size)
            if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) return null
            size += n
        }
        return if (size == originalSize) out else null
    } catch (e: DataFormatException) {
        return null
    } finally {
        inflater.end()
    }
}
