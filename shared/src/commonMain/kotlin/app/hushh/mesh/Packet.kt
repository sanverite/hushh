package app.hushh.mesh

/**
 * One bitchat wire packet. [encode] and [Packet.decode] follow bitchat iOS
 * `BinaryProtocol.swift` (public domain) byte for byte. All numbers are big-endian.
 *
 * Header: version(1) type(1) ttl(1) timestamp ms(8) flags(1) payloadLength(2 in v1, 4 in v2).
 * Body: senderId(8) [recipientId(8)] [v2 only: route count(1) + hops(8 each)]
 * [original size(2|4) if compressed] payload [signature(64)].
 */
data class Packet(
    val type: Int,
    val senderId: ByteArray,
    val timestamp: Long,
    val payload: ByteArray,
    val ttl: Int = TTL,
    val version: Int = 1,
    val recipientId: ByteArray? = null,
    val signature: ByteArray? = null,
    val route: List<ByteArray> = emptyList(),
    /** bitchat's "RSR" flag (0x10), which relays may change; it is kept but not signed. */
    val isRsr: Boolean = false,
    /**
     * The payload's compressed bytes exactly as received, reused when re-encoding. Signatures
     * cover the compressed bytes, and another phone's zlib may not compress identically to ours.
     */
    val compressedPayload: ByteArray? = null,
) {
    /**
     * The wire bytes, or null if the packet can't be represented. Only Noise packets
     * are padded on the wire (later tickets); signing always uses the padded form.
     */
    fun encode(padding: Boolean = false): ByteArray? {
        if (version != 1 && version != 2) return null
        val lengthBytes = if (version == 2) 4 else 2
        val hops = if (version == 2) route else emptyList()
        if (hops.size > 255 || hops.any { it.size != ID_SIZE }) return null

        var body = payload
        var originalSize = -1
        if (compressedPayload != null) {
            originalSize = payload.size
            body = compressedPayload
        } else if (shouldCompress(payload)) {
            val compressed = deflateRaw(payload)
            if (compressed.isNotEmpty() && compressed.size < payload.size) {
                originalSize = payload.size
                body = compressed
            }
        }
        val isCompressed = originalSize >= 0
        val payloadLength = body.size + if (isCompressed) lengthBytes else 0
        if (version == 1 && payloadLength > 0xFFFF) return null

        val out = Writer()
        out.byte(version)
        out.byte(type)
        out.byte(ttl)
        out.number(timestamp, 8)
        var flags = 0
        if (recipientId != null) flags = flags or FLAG_RECIPIENT
        if (signature != null) flags = flags or FLAG_SIGNATURE
        if (isCompressed) flags = flags or FLAG_COMPRESSED
        if (hops.isNotEmpty()) flags = flags or FLAG_ROUTE
        if (isRsr) flags = flags or FLAG_RSR
        out.byte(flags)
        out.number(payloadLength.toLong(), lengthBytes)
        out.id(senderId)
        recipientId?.let { out.id(it) }
        if (hops.isNotEmpty()) {
            out.byte(hops.size)
            hops.forEach { out.bytes(it) }
        }
        if (isCompressed) out.number(originalSize.toLong(), lengthBytes)
        out.bytes(body)
        signature?.let { out.bytes(it.copyOf(SIGNATURE_SIZE)) }

        val data = out.toByteArray()
        return if (padding) pad(data) else data
    }

    /** The bytes that are signed: TTL 0, no signature, RSR cleared, padded. TTL changes when relayed. */
    fun bytesForSigning(): ByteArray? = copy(ttl = 0, signature = null, isRsr = false).encode(padding = true)

    companion object {
        const val TYPE_ANNOUNCE = 0x01
        const val TYPE_MESSAGE = 0x02

        /** Hops a packet may travel from its origin. */
        const val TTL = 7

        const val ID_SIZE = 8
        const val SIGNATURE_SIZE = 64

        private const val FLAG_RECIPIENT = 0x01
        private const val FLAG_SIGNATURE = 0x02
        private const val FLAG_COMPRESSED = 0x04
        private const val FLAG_ROUTE = 0x08
        private const val FLAG_RSR = 0x10

        private const val COMPRESSION_THRESHOLD = 100
        private const val MAX_DEFLATE_RATIO = 1032

        /** A 1 MiB file plus its metadata and framing: bitchat's `FileTransferLimits.maxFramedFileBytes`. */
        private const val MAX_FRAMED_FILE_BYTES = 1_179_760L

        /** Decodes wire bytes, with or without padding. Returns null for anything malformed. */
        fun decode(data: ByteArray): Packet? {
            decodeUnpadded(data)?.let { return it }
            val unpadded = unpad(data) ?: return null
            return decodeUnpadded(unpadded)
        }

        private fun decodeUnpadded(data: ByteArray): Packet? {
            val r = Reader(data)
            val version = r.byte() ?: return null
            if (version != 1 && version != 2) return null
            val lengthBytes = if (version == 2) 4 else 2
            val type = r.byte() ?: return null
            val ttl = r.byte() ?: return null
            val timestamp = r.number(8) ?: return null
            val flags = r.byte() ?: return null
            val payloadLength = r.number(lengthBytes) ?: return null
            if (payloadLength > MAX_FRAMED_FILE_BYTES) return null
            val senderId = r.bytes(ID_SIZE) ?: return null
            val recipientId = if (flags and FLAG_RECIPIENT != 0) r.bytes(ID_SIZE) ?: return null else null

            val route = mutableListOf<ByteArray>()
            if (version == 2 && flags and FLAG_ROUTE != 0) {
                val count = r.byte() ?: return null
                repeat(count) { route += r.bytes(ID_SIZE) ?: return null }
            }

            val payload: ByteArray
            var compressedPayload: ByteArray? = null
            if (flags and FLAG_COMPRESSED != 0) {
                if (payloadLength < lengthBytes) return null
                val originalSize = r.number(lengthBytes)?.toInt() ?: return null
                val compressedSize = payloadLength.toInt() - lengthBytes
                if (originalSize <= 0 || originalSize > maxPayloadBytes(type)) return null
                if (compressedSize <= 0 || originalSize.toLong() > compressedSize.toLong() * MAX_DEFLATE_RATIO) return null
                compressedPayload = r.bytes(compressedSize) ?: return null
                payload = inflateRaw(compressedPayload, originalSize) ?: return null
            } else {
                if (payloadLength > maxPayloadBytes(type)) return null
                payload = r.bytes(payloadLength.toInt()) ?: return null
            }

            val signature = if (flags and FLAG_SIGNATURE != 0) r.bytes(SIGNATURE_SIZE) ?: return null else null
            // Trailing bytes are ignored, as bitchat does.
            return Packet(
                type = type,
                senderId = senderId,
                timestamp = timestamp,
                payload = payload,
                ttl = ttl,
                version = version,
                recipientId = recipientId,
                signature = signature,
                route = route,
                isRsr = flags and FLAG_RSR != 0,
                compressedPayload = compressedPayload,
            )
        }

        /** Largest payload accepted per packet type, as in bitchat's `PacketPayloadLimits`. */
        private fun maxPayloadBytes(type: Int): Long = when (type) {
            0x22, 0x11 -> MAX_FRAMED_FILE_BYTES // fileTransfer, noiseEncrypted
            0x02, 0x25 -> 128L * 1024 // message, groupMessage
            0x04, 0x28 -> 64L * 1024 // courierEnvelope, nostrCarrier
            0x21 -> 8L * 1024 // requestSync
            0x20 -> 1_280 // fragment
            0x01, 0x2C, 0x03, 0x23, 0x24, 0x29, 0x10, 0x26, 0x27 -> 4L * 1024 // control packets
            else -> 128L * 1024
        }

        /** bitchat compresses payloads of 100+ bytes whose bytes aren't already varied (likely compressed). */
        private fun shouldCompress(payload: ByteArray): Boolean {
            if (payload.size < COMPRESSION_THRESHOLD) return false
            val unique = payload.toSet().size
            return unique.toDouble() / minOf(payload.size, 256) < 0.9
        }

        private val BLOCK_SIZES = intArrayOf(256, 512, 1024, 2048)

        /** PKCS#7-style padding up to the smallest block that fits (plus 16 bytes headroom), as bitchat does. */
        private fun pad(data: ByteArray): ByteArray {
            val target = BLOCK_SIZES.firstOrNull { data.size + 16 <= it } ?: return data
            val needed = target - data.size
            if (needed > 255) return data
            return data + ByteArray(needed) { needed.toByte() }
        }

        private fun unpad(data: ByteArray): ByteArray? {
            if (data.isEmpty()) return null
            val n = data.last().toInt() and 0xFF
            if (n == 0 || n > data.size) return null
            for (i in data.size - n until data.size) if (data[i] != data.last()) return null
            return data.copyOf(data.size - n)
        }
    }
}

private class Writer {
    private val out = ArrayList<Byte>()
    fun byte(value: Int) { out += value.toByte() }
    fun bytes(value: ByteArray) { value.forEach { out += it } }
    fun number(value: Long, size: Int) { for (i in size - 1 downTo 0) byte((value shr (8 * i)).toInt()) }
    /** An 8-byte ID, truncated or zero-padded like bitchat. */
    fun id(value: ByteArray) = bytes(value.copyOf(Packet.ID_SIZE))
    fun toByteArray() = out.toByteArray()
}

private class Reader(private val data: ByteArray) {
    private var offset = 0
    fun byte(): Int? = if (offset < data.size) data[offset++].toInt() and 0xFF else null
    fun bytes(n: Int): ByteArray? {
        if (n < 0 || n > data.size - offset) return null
        return data.copyOfRange(offset, offset + n).also { offset += n }
    }
    fun number(size: Int): Long? {
        var value = 0L
        repeat(size) { value = (value shl 8) or (byte() ?: return null).toLong() }
        return value
    }
}

/** Strict UTF-8 decoding: null for invalid bytes rather than replacement characters. */
internal fun ByteArray.decodeUtf8OrNull(): String? = try {
    decodeToString(throwOnInvalidSequence = true)
} catch (e: CharacterCodingException) {
    null
}

/** Raw DEFLATE (no zlib header) at level 5, matching Apple's `COMPRESSION_ZLIB` used by bitchat. */
internal expect fun deflateRaw(data: ByteArray): ByteArray

/** Inflates raw DEFLATE data; null unless it is valid and exactly [originalSize] bytes. */
internal expect fun inflateRaw(data: ByteArray, originalSize: Int): ByteArray?
