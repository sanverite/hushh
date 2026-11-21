package app.hushh.mesh

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import platform.zlib.Z_DEFAULT_STRATEGY
import platform.zlib.Z_DEFLATED
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.deflate
import platform.zlib.deflateBound
import platform.zlib.deflateEnd
import platform.zlib.deflateInit2_
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream
import platform.zlib.zlibVersion

// windowBits -15 means raw DEFLATE with no zlib header, which is what Apple's COMPRESSION_ZLIB produces.

@OptIn(ExperimentalForeignApi::class)
internal actual fun deflateRaw(data: ByteArray): ByteArray = memScoped {
    if (data.isEmpty()) return ByteArray(0)
    val stream = alloc<z_stream>()
    check(deflateInit2_(stream.ptr, 5, Z_DEFLATED, -15, 8, Z_DEFAULT_STRATEGY, zlibVersion(), sizeOf<z_stream>().toInt()) == Z_OK)
    try {
        val out = ByteArray(deflateBound(stream.ptr, data.size.convert()).toInt())
        data.usePinned { input ->
            out.usePinned { output ->
                stream.next_in = input.addressOf(0).reinterpret()
                stream.avail_in = data.size.convert()
                stream.next_out = output.addressOf(0).reinterpret()
                stream.avail_out = out.size.convert()
                check(deflate(stream.ptr, Z_FINISH) == Z_STREAM_END)
            }
        }
        out.copyOf(stream.total_out.toInt())
    } finally {
        deflateEnd(stream.ptr)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun inflateRaw(data: ByteArray, originalSize: Int): ByteArray? = memScoped {
    if (data.isEmpty() || originalSize <= 0) return null
    val stream = alloc<z_stream>()
    if (inflateInit2_(stream.ptr, -15, zlibVersion(), sizeOf<z_stream>().toInt()) != Z_OK) return null
    try {
        val out = ByteArray(originalSize)
        val result = data.usePinned { input ->
            out.usePinned { output ->
                stream.next_in = input.addressOf(0).reinterpret()
                stream.avail_in = data.size.convert()
                stream.next_out = output.addressOf(0).reinterpret()
                stream.avail_out = out.size.convert()
                inflate(stream.ptr, Z_FINISH)
            }
        }
        if (result == Z_STREAM_END && stream.total_out.toInt() == originalSize) out else null
    } finally {
        inflateEnd(stream.ptr)
    }
}
