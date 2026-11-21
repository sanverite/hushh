package app.hushh.mesh

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Two Mesh cores joined by direct function calls, standing in for one Bluetooth link. */
class MeshCoreTest {
    private class TwoCores {
        val a: MeshCore = MeshCore(Identity.generate(), "asha", send = { _, bytes -> b.bytesReceived("link-a", bytes) })
        val b: MeshCore = MeshCore(Identity.generate(), "rahul", send = { _, bytes -> a.bytesReceived("link-b", bytes) })

        fun connect() {
            a.linkConnected("link-b")
            b.linkConnected("link-a")
        }
    }

    @Test
    fun connectedCoresSeeEachOtherAsPeers() = withSodium {
        val p = TwoCores()
        p.connect()
        assertEquals(listOf("rahul"), p.a.state.value.peers.map { it.nickname })
        assertEquals(listOf("asha"), p.b.state.value.peers.map { it.nickname })
    }

    @Test
    fun nearbyMessagesReachTheOtherCore() = withSodium {
        val p = TwoCores()
        p.connect()
        p.a.postToNearby("Water at the east gate")
        p.b.postToNearby("On my way 👍")

        val seenByB = p.b.state.value.nearby
        assertEquals(listOf("Water at the east gate" to false, "On my way 👍" to true), seenByB.map { it.text to it.isMine })
        assertEquals("asha", seenByB.first().nickname)
        assertEquals(listOf("Water at the east gate", "On my way 👍"), p.a.state.value.nearby.map { it.text })
    }

    @Test
    fun longMessagesAreCompressedAndStillArrive() = withSodium {
        val p = TwoCores()
        p.connect()
        val text = "Police moving towards north road, avoid it. ".repeat(10)
        p.a.postToNearby(text)
        assertEquals(text, p.b.state.value.nearby.single().text)
    }

    @Test
    fun peerDisappearsWhenItsLinkDisconnects() = withSodium {
        val p = TwoCores()
        p.connect()
        p.a.linkDisconnected("link-b")
        assertTrue(p.a.state.value.peers.isEmpty())
    }

    @Test
    fun messagesBeforeAnAnnounceAreDropped() = withSodium {
        val p = TwoCores()
        p.a.postToNearby("too early")
        assertTrue(p.b.state.value.nearby.isEmpty())
    }
}
