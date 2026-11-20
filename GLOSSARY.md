# hushh

An open-source chat app for phones that works with no internet, passing end-to-end encrypted messages between nearby phones over a Bluetooth mesh. Built first for internet shutdowns, protests and crowds.

## Network

**Peer**:
Another phone running a compatible app that is currently reachable through the Mesh, directly or via other Peers.
_Avoid_: Node, device, user, contact

**Mesh**:
The ad-hoc network formed by Peers relaying each other's messages over Bluetooth, with no internet or central server.
_Avoid_: Network, swarm

## People

**Identity**:
The cryptographic self a phone generates on first launch; the only thing that makes one Peer recognisable to another. No phone number, email or account is ever attached.
_Avoid_: Account, profile, user ID

**Nickname**:
The human-readable name shown alongside an Identity. Not unique and not verified.
_Avoid_: Username, handle, display name

**Favourite**:
An Identity you have starred so the app keeps it in your list and holds messages for it while it is out of range.
_Avoid_: Contact, friend, buddy

## Conversations

**Nearby**:
The one open room that every Peer in the Mesh can read and post to. Its messages are not private.
_Avoid_: Public chat, broadcast, mesh chat, global chat

**Direct Message**:
An end-to-end encrypted conversation between exactly two Identities.
_Avoid_: DM, private chat, PM

**Verified**:
The state of an Identity whose owner you confirmed in person by scanning their QR code. Any other Identity is unverified.
_Avoid_: Trusted, confirmed, authenticated

## Safety

**Panic Wipe**:
The single action that erases everything the app holds (messages, Identity, favourites and settings), leaving it indistinguishable from a fresh install.
_Avoid_: Erase switch, kill switch, emergency wipe, reset
