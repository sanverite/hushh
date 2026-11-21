package app.hushh.mesh

/**
 * The payload of an announce packet: who a Peer is. TLV-encoded (type, length, value)
 * like bitchat iOS `AnnouncementPacket`; unknown TLVs are skipped.
 */
class Announcement(
    val nickname: String,
    val noisePublicKey: ByteArray,
    val signingPublicKey: ByteArray,
) {
    fun encode(): ByteArray? {
        val name = nickname.encodeToByteArray()
        if (name.size > 255) return null
        // Capabilities: none yet. Later tickets set bits as features are implemented.
        return tlv(TLV_NICKNAME, name) + tlv(TLV_NOISE_KEY, noisePublicKey) +
            tlv(TLV_SIGNING_KEY, signingPublicKey) + tlv(TLV_CAPABILITIES, byteArrayOf(0))
    }

    companion object {
        private const val TLV_NICKNAME = 0x01
        private const val TLV_NOISE_KEY = 0x02
        private const val TLV_SIGNING_KEY = 0x03
        private const val TLV_CAPABILITIES = 0x05

        private fun tlv(type: Int, value: ByteArray) = byteArrayOf(type.toByte(), value.size.toByte()) + value

        fun decode(data: ByteArray): Announcement? {
            var nickname: String? = null
            var noiseKey: ByteArray? = null
            var signingKey: ByteArray? = null
            var offset = 0
            while (offset + 2 <= data.size) {
                val type = data[offset].toInt() and 0xFF
                val length = data[offset + 1].toInt() and 0xFF
                offset += 2
                if (offset + length > data.size) return null
                val value = data.copyOfRange(offset, offset + length)
                offset += length
                when (type) {
                    TLV_NICKNAME -> nickname = value.decodeUtf8OrNull()
                    TLV_NOISE_KEY -> noiseKey = value
                    TLV_SIGNING_KEY -> signingKey = value
                }
            }
            return Announcement(nickname ?: return null, noiseKey ?: return null, signingKey ?: return null)
        }
    }
}
