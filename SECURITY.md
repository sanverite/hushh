# Security policy

People may rely on hushh in situations where a security bug could put them at risk, so we take reports seriously.

## Reporting a vulnerability

**Please do not open a public issue, discussion or pull request for a security problem.**

Report it privately through GitHub instead:

1. Go to this repository's **Security** tab.
2. Click **Report a vulnerability**.
3. Fill in the form. Only the maintainers can see it.

Helpful things to include:

- What is affected (crypto, protocol, Panic Wipe, stored data, the Bluetooth layer, the build or release process)
- Steps to reproduce, or a proof of concept
- The impact: what an attacker can learn or do, and what they need to be able to do it (for example, being in Bluetooth range or holding the phone)
- The app version or commit, and the phone model and OS

We'll reply in the advisory thread, work with you on a fix, and credit you in the advisory unless you'd rather stay anonymous.

## What's in scope

- **Encryption:** breaking the confidentiality or integrity of Direct Messages.
- **Impersonation:** pretending to be another Identity, or bypassing QR verification.
- **Linkability:** linking a person's Identity, Nickname or messages beyond what the documented design already exposes. The design exposes a stable Identity in Bluetooth announcements and signed messages in the public Nearby room.
- **Panic Wipe:** the wipe leaving recoverable messages, keys or settings behind.
- **Stored data:** messages readable from the phone's storage without the key.
- **Crashes and resource exhaustion:** caused by malformed packets from a nearby phone.

## Supported versions

hushh is pre-release and has no supported versions yet. Reports against the `main` branch are welcome.
