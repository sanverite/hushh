# Own codebase, wire-compatible with bitchat

We write our own code under our own name, but speak bitchat's BLE wire protocol (packet format, Noise_XX_25519_ChaChaPoly_SHA256, service UUIDs). A mesh is only useful when enough nearby phones run it, and compatibility lets hushh users relay for, and chat with, existing bitchat users from day one. We deliberately do not fork bitchat's code or branding, to keep a distinct identity and avoid inheriting its codebase. The cost is that bitchat's protocol changes become ours to track.

## Consequences

- bitchat has no version handshake. Features are gated by the packet version byte and a capability bitmask in announcements, so compatibility means matching a specific subset of message types, not "the whole protocol".
