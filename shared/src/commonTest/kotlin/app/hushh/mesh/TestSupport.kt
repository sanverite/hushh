package app.hushh.mesh

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import kotlinx.coroutines.test.runTest

fun hex(s: String): ByteArray = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

/** Runs [block] with libsodium initialised. */
fun withSodium(block: () -> Unit) = runTest {
    if (!LibsodiumInitializer.isInitialized()) LibsodiumInitializer.initialize()
    block()
}

/** The fixed Identity used by bitchat's golden bytes: noise seed 20..3f, signing seed 00..1f. */
fun goldenIdentity() = Identity.fromSeeds(
    noiseSeed = ByteArray(32) { (0x20 + it).toByte() },
    signingSeed = ByteArray(32) { it.toByte() },
)
