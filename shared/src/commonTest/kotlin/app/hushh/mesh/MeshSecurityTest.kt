package app.hushh.mesh

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Packets that must be dropped, and inputs that must never crash the core. */
class MeshSecurityTest {
    /** A receiving core, and a real Peer ("asha") already announced to it on link "l". */
    private class Setup {
        val asha = Identity.generate()
        val receiver = MeshCore(Identity.generate(), "me", send = { _, _ -> })
        init {
            receiver.linkConnected("l")
            receiver.bytesReceived("l", signed(asha, announce(asha, "asha")).encode()!!)
        }
    }

    private companion object {
        fun announce(identity: Identity, nickname: String) = Packet(
            Packet.TYPE_ANNOUNCE, identity.peerId.toBytes(), 1,
            Announcement(nickname, identity.noisePublicKey, identity.signingPublicKey).encode()!!,
        )

        fun message(from: Identity, text: String) =
            Packet(Packet.TYPE_MESSAGE, from.peerId.toBytes(), 2, text.encodeToByteArray())

        fun signed(by: Identity, packet: Packet) = packet.copy(signature = by.sign(packet.bytesForSigning()!!))
    }

    @Test
    fun validMessageFromAnnouncedPeerIsShown() = withSodium {
        val s = Setup()
        s.receiver.bytesReceived("l", signed(s.asha, message(s.asha, "hi")).encode()!!)
        assertEquals(listOf("hi"), s.receiver.state.value.nearby.map { it.text })
    }

    @Test
    fun unsignedMessageIsDropped() = withSodium {
        val s = Setup()
        s.receiver.bytesReceived("l", message(s.asha, "unsigned").encode()!!)
        assertTrue(s.receiver.state.value.nearby.isEmpty())
    }

    @Test
    fun messageSignedByAnotherKeyIsDropped() = withSodium {
        val s = Setup()
        val impostor = Identity.generate()
        s.receiver.bytesReceived("l", signed(impostor, message(s.asha, "I am asha")).encode()!!)
        assertTrue(s.receiver.state.value.nearby.isEmpty())
    }

    @Test
    fun tamperedMessageIsDropped() = withSodium {
        val s = Setup()
        val good = signed(s.asha, message(s.asha, "meet at gate 1"))
        s.receiver.bytesReceived("l", good.copy(payload = "meet at gate 9".encodeToByteArray()).encode()!!)
        assertTrue(s.receiver.state.value.nearby.isEmpty())
    }

    @Test
    fun relayedTtlChangeKeepsSignatureValid() = withSodium {
        val s = Setup()
        s.receiver.bytesReceived("l", signed(s.asha, message(s.asha, "relayed")).copy(ttl = 3).encode()!!)
        assertEquals(1, s.receiver.state.value.nearby.size)
    }

    @Test
    fun unsignedOrForgedAnnouncesAreDropped() = withSodium {
        val receiver = MeshCore(Identity.generate(), "me", send = { _, _ -> })
        receiver.linkConnected("l")
        val mallory = Identity.generate()
        val victim = Identity.generate()

        receiver.bytesReceived("l", announce(mallory, "unsigned").encode()!!)
        // Signed by a different key than the one it carries.
        receiver.bytesReceived("l", signed(victim, announce(mallory, "wrong key")).encode()!!)
        // Claims the victim's Peer ID with mallory's keys.
        val spoofed = announce(mallory, "spoofed").copy(senderId = victim.peerId.toBytes())
        receiver.bytesReceived("l", signed(mallory, spoofed).encode()!!)

        assertTrue(receiver.state.value.peers.isEmpty())
    }

    @Test
    fun reannouncingAPeersNoiseKeyWithAnotherSigningKeyIsDropped() = withSodium {
        val s = Setup()
        val mallory = Identity.generate()
        // Copies asha's public Noise key (so the Peer ID matches) but signs with mallory's key.
        val hijack = Packet(
            Packet.TYPE_ANNOUNCE, s.asha.peerId.toBytes(), 3,
            Announcement("asha", s.asha.noisePublicKey, mallory.signingPublicKey).encode()!!,
        )
        s.receiver.bytesReceived("l", signed(mallory, hijack).encode()!!)
        s.receiver.bytesReceived("l", signed(mallory, message(s.asha, "follow me")).encode()!!)
        s.receiver.bytesReceived("l", signed(s.asha, message(s.asha, "still me")).encode()!!)

        assertEquals(listOf("still me"), s.receiver.state.value.nearby.map { it.text })
    }

    @Test
    fun malformedBytesNeverCrash() = withSodium {
        val s = Setup()
        val valid = signed(s.asha, message(s.asha, "Water + first aid at the east gate tent. ".repeat(5))).encode()!!
        val random = Random(42)
        // Every truncation of a valid packet.
        for (n in 0 until valid.size) s.receiver.bytesReceived("l", valid.copyOf(n))
        // Random single-byte corruptions, including the header, flags and lengths.
        repeat(2_000) {
            val bytes = valid.copyOf()
            bytes[random.nextInt(bytes.size)] = random.nextInt(256).toByte()
            s.receiver.bytesReceived("l", bytes)
        }
        // Pure noise, including huge declared lengths and bogus compression headers.
        repeat(2_000) { s.receiver.bytesReceived("l", random.nextBytes(random.nextInt(0, 300))) }
        s.receiver.bytesReceived("l", hex("01020700000000000000000400ff") + ByteArray(8) + hex("ffff") + ByteArray(10))

        assertTrue(s.receiver.state.value.nearby.all { it.sender == s.asha.peerId })
    }
}
