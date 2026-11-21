package app.hushh.mesh

import com.ionspin.kotlin.crypto.hash.Hash
import com.ionspin.kotlin.crypto.scalarmult.ScalarMultiplication
import com.ionspin.kotlin.crypto.signature.Signature
import com.ionspin.kotlin.crypto.util.LibsodiumRandom
import kotlin.random.Random

/**
 * This phone's cryptographic self: a Curve25519 static key (for Noise) and an
 * Ed25519 signing key. libsodium must be initialised before use.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class Identity private constructor(
    private val noisePrivateKey: UByteArray,
    private val signingSecretKey: UByteArray,
) {
    val noisePublicKey: ByteArray = ScalarMultiplication.scalarMultiplicationBase(noisePrivateKey).asByteArray()
    val signingPublicKey: ByteArray = Signature.ed25519SkToPk(signingSecretKey).asByteArray()

    /** First 8 bytes of SHA-256 of the Noise static public key. */
    val peerId: PeerId = peerIdOf(noisePublicKey)

    fun sign(data: ByteArray): ByteArray = Signature.detached(data.asUByteArray(), signingSecretKey).asByteArray()

    companion object {
        fun generate() = Identity(LibsodiumRandom.buf(32), Signature.keypair().secretKey)

        /** A fixed Identity from 32-byte seeds, for tests. */
        fun fromSeeds(noiseSeed: ByteArray, signingSeed: ByteArray) =
            Identity(noiseSeed.asUByteArray(), Signature.seedKeypair(signingSeed.asUByteArray()).secretKey)

        fun peerIdOf(noisePublicKey: ByteArray) =
            PeerId(Hash.sha256(noisePublicKey.asUByteArray()).asByteArray().copyOf(Packet.ID_SIZE))

        /** True only if [signature] is a valid Ed25519 signature of [data] by [publicKey]. */
        fun verify(signature: ByteArray, data: ByteArray, publicKey: ByteArray): Boolean = try {
            Signature.verifyDetached(signature.asUByteArray(), data.asUByteArray(), publicKey.asUByteArray())
            true
        } catch (e: Exception) {
            false
        }
    }
}

/** A Peer's 8-byte ID, shown and compared as hex. */
data class PeerId(val hex: String) {
    constructor(bytes: ByteArray) : this(bytes.toHex())

    fun toBytes(): ByteArray = ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

fun ByteArray.toHex(): String = joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

private val ADJECTIVES = listOf(
    "quiet", "brave", "swift", "calm", "bright", "gentle", "lucky", "clever",
    "sunny", "misty", "bold", "kind", "happy", "silent", "wild", "steady",
)
private val ANIMALS = listOf(
    "otter", "tiger", "sparrow", "panda", "falcon", "koala", "heron", "lynx",
    "dolphin", "fox", "owl", "peacock", "turtle", "wolf", "yak", "mongoose",
)

/** A random Nickname like "quiet-otter-42". */
fun randomNickname() = "${ADJECTIVES.random()}-${ANIMALS.random()}-${Random.nextInt(10, 100)}"
