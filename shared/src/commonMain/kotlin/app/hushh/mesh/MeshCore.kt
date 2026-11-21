package app.hushh.mesh

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

/**
 * The Mesh core: the single entry point to the shared protocol code, used by the
 * app and the tests alike.
 *
 * In: bytes received on a link, links connecting and disconnecting, and actions on this phone.
 * Out: bytes to send through [send] (a link ID, or null for every link) and the UI [state].
 *
 * Not thread-safe: call it from one thread at a time.
 */
class MeshCore(
    private val identity: Identity,
    nickname: String,
    private val send: (linkId: String?, bytes: ByteArray) -> Unit,
) {
    private val _state = MutableStateFlow(MeshState(nickname))
    val state: StateFlow<MeshState> = _state.asStateFlow()

    /** Peers that announced on each link. */
    private val links = mutableMapOf<String, MutableSet<PeerId>>()
    private val nicknames = mutableMapOf<PeerId, String>()

    /** Each Peer's Ed25519 signing key, pinned at its first valid announce. */
    private val signingKeys = mutableMapOf<PeerId, ByteArray>()

    fun linkConnected(linkId: String) {
        links.getOrPut(linkId) { mutableSetOf() }
        announcePacket()?.let { send(linkId, it) }
    }

    fun linkDisconnected(linkId: String) {
        links.remove(linkId) ?: return
        updatePeers()
    }

    fun bytesReceived(linkId: String, bytes: ByteArray) {
        val packet = Packet.decode(bytes) ?: return
        if (PeerId(packet.senderId) == identity.peerId) return
        when (packet.type) {
            Packet.TYPE_ANNOUNCE -> receiveAnnounce(linkId, packet)
            Packet.TYPE_MESSAGE -> receiveNearbyMessage(packet)
        }
    }

    /** Posts [text] to Nearby: signed, sent on every link and shown in our own feed. */
    fun postToNearby(text: String) {
        if (text.isBlank() || text.length > MAX_MESSAGE_LENGTH) return
        val packet = signed(Packet(Packet.TYPE_MESSAGE, identity.peerId.toBytes(), now(), text.encodeToByteArray()))
        val bytes = packet?.encode() ?: return
        send(null, bytes)
        addNearbyMessage(NearbyMessage(identity.peerId, _state.value.nickname, text, packet.timestamp, isMine = true))
    }

    private fun now() = Clock.System.now().toEpochMilliseconds()

    private fun announcePacket(): ByteArray? {
        val payload = Announcement(_state.value.nickname, identity.noisePublicKey, identity.signingPublicKey).encode()
            ?: return null
        return signed(Packet(Packet.TYPE_ANNOUNCE, identity.peerId.toBytes(), now(), payload))?.encode()
    }

    private fun signed(packet: Packet): Packet? {
        val data = packet.bytesForSigning() ?: return null
        return packet.copy(signature = identity.sign(data))
    }

    /**
     * An announce must come from the Peer ID its Noise key derives, be signed by the signing key it
     * carries, and keep the signing key we pinned for that Peer. Noise keys are public, so without
     * the pin anyone could re-announce a Peer's Noise key with their own signing key and post as them.
     */
    private fun receiveAnnounce(linkId: String, packet: Packet) {
        val announcement = Announcement.decode(packet.payload) ?: return
        val peerId = PeerId(packet.senderId)
        if (Identity.peerIdOf(announcement.noisePublicKey) != peerId) return
        val pinned = signingKeys[peerId]
        if (pinned != null && !pinned.contentEquals(announcement.signingPublicKey)) return
        if (!hasValidSignature(packet, announcement.signingPublicKey)) return
        signingKeys[peerId] = announcement.signingPublicKey
        // The other side's announce can arrive before our adapter reports the link.
        links.getOrPut(linkId) { mutableSetOf() } += peerId
        nicknames[peerId] = announcement.nickname
        updatePeers()
    }

    /** A Nearby message is shown only if signed by a Peer we know. */
    private fun receiveNearbyMessage(packet: Packet) {
        val peerId = PeerId(packet.senderId)
        val signingKey = signingKeys[peerId] ?: return
        if (!hasValidSignature(packet, signingKey)) return
        val text = packet.payload.decodeUtf8OrNull() ?: return
        addNearbyMessage(NearbyMessage(peerId, nicknames[peerId] ?: return, text, packet.timestamp, isMine = false))
    }

    private fun hasValidSignature(packet: Packet, signingKey: ByteArray): Boolean {
        val signature = packet.signature ?: return false
        val data = packet.bytesForSigning() ?: return false
        return Identity.verify(signature, data, signingKey)
    }

    private fun updatePeers() {
        val reachable = links.values.flatten().toSet()
        _state.update { state -> state.copy(peers = reachable.map { Peer(it, nicknames[it] ?: "") }.sortedBy { it.nickname }) }
    }

    /** Nearby is held in memory only and capped, so a busy crowd can't grow it without bound. */
    private fun addNearbyMessage(message: NearbyMessage) {
        _state.update { it.copy(nearby = (it.nearby + message).takeLast(MAX_NEARBY_MESSAGES)) }
    }

    companion object {
        /** Longest message, in characters, as in bitchat. */
        const val MAX_MESSAGE_LENGTH = 60_000

        private const val MAX_NEARBY_MESSAGES = 500
    }
}

/** What the UI shows. */
data class MeshState(
    val nickname: String,
    val nearby: List<NearbyMessage> = emptyList(),
    val peers: List<Peer> = emptyList(),
)

data class NearbyMessage(
    val sender: PeerId,
    val nickname: String,
    val text: String,
    val timestamp: Long,
    val isMine: Boolean,
)

data class Peer(val id: PeerId, val nickname: String)
