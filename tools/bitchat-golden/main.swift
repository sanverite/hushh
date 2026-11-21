// Driver for generate.sh: encodes fixed packets with bitchat's own Swift code and prints hex.
import Foundation
import CryptoKit

func hex(_ d: Data) -> String { d.map { String(format: "%02x", $0) }.joined() }
let signSeed = Data((0..<32).map { UInt8($0) })            // 00 01 .. 1f
let noiseSeed = Data((0..<32).map { UInt8(0x20 + $0) })    // 20 21 .. 3f
let signKey = try! Curve25519.Signing.PrivateKey(rawRepresentation: signSeed)
let noiseKey = try! Curve25519.KeyAgreement.PrivateKey(rawRepresentation: noiseSeed)
let noisePub = noiseKey.publicKey.rawRepresentation
let signPub = signKey.publicKey.rawRepresentation
let peerID = Data(SHA256.hash(data: noisePub)).prefix(8)
print("noisePub", hex(noisePub)); print("signPub", hex(signPub)); print("peerID", hex(peerID))

func emit(_ name: String, _ p: BitchatPacket, padding: Bool = false) {
  let signing = p.toBinaryDataForSigning()!
  var s = p; s.signature = try! signKey.signature(for: signing)
  print(name + ".signing", hex(signing)); print(name + ".signature", hex(s.signature!))
  print(name + ".wire", hex(s.toBinaryData(padding: padding)!))
}
let ts: UInt64 = 1_763_700_000_000
let ann = AnnouncementPacket(nickname: "quiet-otter-42", noisePublicKey: noisePub, signingPublicKey: signPub,
                             directNeighbors: nil, capabilities: PeerCapabilities(rawValue: 0)).encode()!
print("announce.payload", hex(ann))
emit("announce", BitchatPacket(type: 0x01, senderID: peerID, recipientID: nil, timestamp: ts, payload: ann, signature: nil, ttl: 7))
emit("message", BitchatPacket(type: 0x02, senderID: peerID, recipientID: nil, timestamp: ts + 1, payload: Data("hello nearby 👋".utf8), signature: nil, ttl: 7))
let long = Data(String(repeating: "Nearby is public. ", count: 20).utf8)
emit("compressed", BitchatPacket(type: 0x02, senderID: peerID, recipientID: nil, timestamp: ts + 2, payload: long, signature: nil, ttl: 7))
emit("v2", BitchatPacket(type: 0x02, senderID: peerID, recipientID: Data([1,2,3,4,5,6,7,8]), timestamp: ts + 3, payload: Data("v2 hi".utf8), signature: nil, ttl: 3, version: 2, route: [Data([9,9,9,9,9,9,9,9])]))
emit("padded", BitchatPacket(type: 0x02, senderID: peerID, recipientID: nil, timestamp: ts + 4, payload: Data("pad me".utf8), signature: nil, ttl: 7), padding: true)
