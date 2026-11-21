package app.hushh.mesh

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PacketTest {
    private val sender = ByteArray(8) { it.toByte() }

    private fun roundTrip(packet: Packet, padding: Boolean = false): Packet = assertNotNull(Packet.decode(packet.encode(padding)!!))

    @Test
    fun v1HeaderIs14BytesWithTwoByteLength() {
        val bytes = Packet(Packet.TYPE_MESSAGE, sender, 1, "hi".encodeToByteArray()).encode()!!
        assertEquals(14 + 8 + 2, bytes.size)
        assertEquals(1, bytes[0].toInt())
        assertEquals(0, bytes[11].toInt()) // no flags
        assertEquals(listOf<Byte>(0, 2), bytes.slice(12..13))
    }

    @Test
    fun v2HeaderIs16BytesWithFourByteLength() {
        val bytes = Packet(Packet.TYPE_MESSAGE, sender, 1, "hi".encodeToByteArray(), version = 2).encode()!!
        assertEquals(16 + 8 + 2, bytes.size)
        assertEquals(listOf<Byte>(0, 0, 0, 2), bytes.slice(12..15))
    }

    @Test
    fun optionalRecipientAndSignatureRoundTrip() {
        val packet = Packet(
            Packet.TYPE_MESSAGE, sender, 1_763_700_000_123, "hello".encodeToByteArray(), ttl = 5,
            recipientId = ByteArray(8) { 9 }, signature = ByteArray(64) { 7 },
        )
        val bytes = packet.encode()!!
        assertEquals(0x03, bytes[11].toInt()) // hasRecipient | hasSignature
        val decoded = roundTrip(packet)
        assertEquals(1_763_700_000_123, decoded.timestamp)
        assertEquals(5, decoded.ttl)
        assertContentEquals(packet.recipientId, decoded.recipientId)
        assertContentEquals(packet.signature, decoded.signature)
        assertContentEquals(packet.payload, decoded.payload)
    }

    @Test
    fun routeIsOnlyWrittenForV2() {
        val hop = ByteArray(8) { 5 }
        assertEquals(1, roundTrip(Packet(Packet.TYPE_MESSAGE, sender, 1, ByteArray(3), version = 2, route = listOf(hop))).route.size)
        assertEquals(0, roundTrip(Packet(Packet.TYPE_MESSAGE, sender, 1, ByteArray(3), version = 1, route = listOf(hop))).route.size)
    }

    @Test
    fun compressesFrom100BytesOnlyWhenItShrinks() {
        val flagsOf = { payload: ByteArray -> Packet(Packet.TYPE_MESSAGE, sender, 1, payload).encode()!![11].toInt() }
        assertEquals(0, flagsOf(ByteArray(99) { 'a'.code.toByte() }))
        assertEquals(0x04, flagsOf(ByteArray(100) { 'a'.code.toByte() }))
        assertEquals(0, flagsOf(Random(1).nextBytes(400))) // varied bytes: not compressed
        val text = "Water at the east gate. ".repeat(30).encodeToByteArray()
        assertContentEquals(text, roundTrip(Packet(Packet.TYPE_MESSAGE, sender, 1, text)).payload)
        assertContentEquals(text, roundTrip(Packet(Packet.TYPE_MESSAGE, sender, 1, text, version = 2)).payload)
    }

    @Test
    fun receivedCompressedBytesAreKeptSoSignaturesStillVerify() = withSodium {
        val identity = goldenIdentity()
        val text = "Nearby is public. ".repeat(20).encodeToByteArray()
        // The same text deflated at level 1: valid, but not the bytes our level-5 zlib produces.
        val otherZlib = hex("f34b4d2c4aaa54c82c5628284dcac94cd653f01b15a141680000")
        val sent = Packet(Packet.TYPE_MESSAGE, sender, 1, text, compressedPayload = otherZlib)
        val signed = sent.copy(signature = identity.sign(sent.bytesForSigning()!!))

        val received = Packet.decode(signed.encode()!!)!!
        assertContentEquals(text, received.payload)
        assertTrue(Identity.verify(received.signature!!, received.bytesForSigning()!!, identity.signingPublicKey))
    }

    @Test
    fun paddedPacketsDecode() {
        val packet = Packet(Packet.TYPE_MESSAGE, sender, 1, "pad".encodeToByteArray())
        assertEquals(256, packet.encode(padding = true)!!.size)
        assertContentEquals(packet.payload, roundTrip(packet, padding = true).payload)
    }

    @Test
    fun unknownVersionIsRejected() {
        val bytes = Packet(Packet.TYPE_MESSAGE, sender, 1, "x".encodeToByteArray()).encode()!!
        bytes[0] = 3
        assertNull(Packet.decode(bytes))
        assertNull(Packet(Packet.TYPE_MESSAGE, sender, 1, ByteArray(1), version = 3).encode())
    }

    @Test
    fun peerIdIsFirst8BytesOfSha256OfNoiseKey() = withSodium {
        // SHA-256(00 01 .. 1f) = 630dcd2966c4336691125448bbb25b4ff412a49c732db2c8abc1b8581bd710dd
        assertEquals(PeerId("630dcd2966c43366"), Identity.peerIdOf(ByteArray(32) { it.toByte() }))
    }
}
