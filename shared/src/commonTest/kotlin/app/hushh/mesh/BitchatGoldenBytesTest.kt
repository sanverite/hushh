package app.hushh.mesh

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Bytes produced by bitchat iOS's own encoder (BinaryProtocol.swift and AnnouncementPacket at
 * permissionlesstech/bitchat 04995f6), compiled and run with the fixed inputs below.
 * Regenerate with tools/bitchat-golden/generate.sh. Signatures come from CryptoKit, which
 * randomises Ed25519 signatures, so we verify them rather than reproduce them.
 */
class BitchatGoldenBytesTest {
    private val timestamp = 1_763_700_000_000L
    private val peerId = hex("a0289810032eb417")

    @Test
    fun identityKeysAndPeerIdMatchBitchat() = withSodium {
        val identity = goldenIdentity()
        assertEquals("358072d6365880d1aeea329adf9121383851ed21a28e3b75e965d0d2cd166254", identity.noisePublicKey.toHex())
        assertEquals("03a107bff3ce10be1d70dd18e74bc09967e4d6309ba50d5f1ddc8664125531b8", identity.signingPublicKey.toHex())
        assertEquals(PeerId("a0289810032eb417"), identity.peerId)
    }

    @Test
    fun announceMatchesBitchat() = withSodium {
        val identity = goldenIdentity()
        val payload = Announcement("quiet-otter-42", identity.noisePublicKey, identity.signingPublicKey).encode()!!
        assertEquals(
            "010e71756965742d6f747465722d34320220358072d6365880d1aeea329adf9121383851ed21a28e3b75e965d0d2cd166254" +
                "032003a107bff3ce10be1d70dd18e74bc09967e4d6309ba50d5f1ddc8664125531b8050100",
            payload.toHex(),
        )
        val wire = "0101070000019aa4b64500020057a0289810032eb417010e71756965742d6f747465722d34320220358072d6365880d1aeea329adf9121383851ed21a28e3b75e965d0d2cd166254032003a107bff3ce10be1d70dd18e74bc09967e4d6309ba50d5f1ddc8664125531b805010083fe332fbe80a1065448ad4c3dccb8d578e45aa0f4d7a80b24a5de17f34d8eeab1fc05f97d27befc185bec84493aaf846cf3e2df822e9fc9445d107f8e9dd50c"
        assertWireMatches(
            Packet(Packet.TYPE_ANNOUNCE, peerId, timestamp, payload),
            signature = "83fe332fbe80a1065448ad4c3dccb8d578e45aa0f4d7a80b24a5de17f34d8eeab1fc05f97d27befc185bec84493aaf846cf3e2df822e9fc9445d107f8e9dd50c",
            wire = wire,
        )
        val decoded = assertNotNull(Announcement.decode(Packet.decode(hex(wire))!!.payload))
        assertEquals("quiet-otter-42", decoded.nickname)
        assertContentEquals(identity.noisePublicKey, decoded.noisePublicKey)
    }

    @Test
    fun nearbyMessageMatchesBitchat() = withSodium {
        assertWireMatches(
            Packet(Packet.TYPE_MESSAGE, peerId, timestamp + 1, "hello nearby 👋".encodeToByteArray()),
            signature = "41df2a373ded20de858eea3bc62fb16c4ae6d22072c8812368aec350d9863e16dd40b6e0b101f7aa4ce54ab4faaed78e74a36fc1bc17c23468ca498ca99f3f0a",
            wire = "0102070000019aa4b64501020011a0289810032eb41768656c6c6f206e656172627920f09f918b41df2a373ded20de858eea3bc62fb16c4ae6d22072c8812368aec350d9863e16dd40b6e0b101f7aa4ce54ab4faaed78e74a36fc1bc17c23468ca498ca99f3f0a",
        )
    }

    @Test
    fun compressedMessageMatchesBitchat() = withSodium {
        val text = "Nearby is public. ".repeat(20)
        val wire = "0102070000019aa4b6450206001ba0289810032eb4170168f34b4d2c4aaa54c82c5628284dcac94cd653f01b15a1810800c937ee6fb4ee68997a3ef4de6561625e39ae3f70a9a736600f39357b0e9dc3331dcde3d95fae8a1cf5b041a0b8f351790c0bd7c7aa575e3c1e38ed65168a7902"
        assertEquals(text, Packet.decode(hex(wire))!!.payload.decodeToString())
        assertWireMatches(
            Packet(Packet.TYPE_MESSAGE, peerId, timestamp + 2, text.encodeToByteArray()),
            signature = "c937ee6fb4ee68997a3ef4de6561625e39ae3f70a9a736600f39357b0e9dc3331dcde3d95fae8a1cf5b041a0b8f351790c0bd7c7aa575e3c1e38ed65168a7902",
            wire = wire,
        )
    }

    @Test
    fun v2PacketWithRecipientAndRouteMatchesBitchat() = withSodium {
        val wire = "0202030000019aa4b645030b00000005a0289810032eb41701020304050607080109090909090909097632206869ac759ab3e6b7b47aa1afc2e3b9d603c914c6ae878c6c3d5cea5f766e61ecc1a7d273bc0ace3300da595c9a5a163ba194e4d97fde09d6d57a3f9564308368c007"
        val decoded = Packet.decode(hex(wire))!!
        assertEquals(2, decoded.version)
        assertEquals(3, decoded.ttl)
        assertEquals("0102030405060708", decoded.recipientId!!.toHex())
        assertEquals(listOf("0909090909090909"), decoded.route.map { it.toHex() })
        assertWireMatches(
            Packet(
                Packet.TYPE_MESSAGE, peerId, timestamp + 3, "v2 hi".encodeToByteArray(), ttl = 3, version = 2,
                recipientId = hex("0102030405060708"), route = listOf(hex("0909090909090909")),
            ),
            signature = "ac759ab3e6b7b47aa1afc2e3b9d603c914c6ae878c6c3d5cea5f766e61ecc1a7d273bc0ace3300da595c9a5a163ba194e4d97fde09d6d57a3f9564308368c007",
            wire = wire,
        )
    }

    @Test
    fun paddedPacketMatchesBitchat() = withSodium {
        val signature = "317e95b9bb161d84c267ab96fea9cc6a60de858da72c1401dd74f3481f9285747b604e528752e03a61eea012fa1d08efc95eeb256240c9b530a213e8672a9d0a"
        val wire = "0102070000019aa4b64504020006a0289810032eb417706164206d65" + signature + "a4".repeat(0xa4)
        assertWireMatches(
            Packet(Packet.TYPE_MESSAGE, peerId, timestamp + 4, "pad me".encodeToByteArray()),
            signature = signature,
            wire = wire,
            padding = true,
        )
    }

    /** Our encoding with bitchat's signature equals bitchat's bytes, the signature verifies, and decoding round-trips. */
    private fun assertWireMatches(packet: Packet, signature: String, wire: String, padding: Boolean = false) {
        val signed = packet.copy(signature = hex(signature))
        assertEquals(wire, signed.encode(padding)!!.toHex())
        assertTrue(Identity.verify(hex(signature), packet.bytesForSigning()!!, goldenIdentity().signingPublicKey))
        val decoded = assertNotNull(Packet.decode(hex(wire)))
        assertContentEquals(packet.payload, decoded.payload)
        assertEquals(wire, decoded.encode(padding)!!.toHex())
    }
}
