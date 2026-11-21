#!/bin/sh
# Prints golden bytes from bitchat iOS's own encoder (public domain), for
# shared/src/commonTest/.../BitchatGoldenBytesTest.kt. Needs macOS with Xcode.
# Usage: tools/bitchat-golden/generate.sh [bitchat commit]
set -eu
COMMIT=${1:-04995f6}
HERE=$(cd "$(dirname "$0")" && pwd)
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

git clone -q --filter=blob:none --no-checkout https://github.com/permissionlesstech/bitchat.git "$WORK/bitchat"
git -C "$WORK/bitchat" checkout -q "$COMMIT" -- localPackages/BitFoundation/Sources bitchat/Protocols/Packets.swift

SRC="$WORK/bitchat/localPackages/BitFoundation/Sources/BitFoundation"
mkdir "$WORK/build"
for f in BinaryProtocol BitchatPacket CompressionUtil MessagePadding MessageType Constants \
         PacketPayloadLimits FileTransferLimits PeerCapabilities; do
  grep -v "import BitLogger" "$SRC/$f.swift" > "$WORK/build/$f.swift"
done
# Only AnnouncementPacket from Packets.swift; the rest needs the whole app.
{ echo "import Foundation"
  awk '/^struct AnnouncementPacket/{p=1} /^\/\/\/ State that is authoritative/{p=0} p' \
    "$WORK/bitchat/bitchat/Protocols/Packets.swift"; } > "$WORK/build/AnnouncementPacket.swift"

xcrun swiftc -O -o "$WORK/gen" "$WORK"/build/*.swift "$HERE/Stub.swift" "$HERE/main.swift"
"$WORK/gen"
