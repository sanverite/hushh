# Design brief: "hushh" — offline mesh chat app (iOS + Android)

The UI is shared between iOS and Android (Compose Multiplatform, ADR-0003), so this is **one design** with a short list of platform differences. Terms follow `GLOSSARY.md`.

## What the app is
An offline chat app for phones. Phones running it connect to nearby phones over Bluetooth and relay messages for each other. No internet, SIM, account or phone number is needed. Private chats are end-to-end encrypted. It is built first for internet shutdowns, protests and big crowds in India.

"hushh" is a placeholder name. Use it in the logo spot, but keep the branding neutral and easy to replace.

## Who uses it and in what situation
- Ordinary people (students, protesters, families at festivals), many on cheap Android phones, some on iPhones.
- Often stressed, outdoors, in bright sunlight, one-handed, in a crowd.
- Many prefer Hindi. Every layout must fit Hindi text, which runs about 30% longer than English.

## Design principles
1. **Calm and obvious.** Every screen should be usable in 2 seconds by someone who has never seen it. No jargon: never say "mesh", "node", "encryption keys" or "Noise" in the UI.
2. **Big touch targets** (at least 48dp / 44pt) and high contrast. Must be readable in sunlight.
3. **Dark and light themes.** Dark is the default.
4. **Discreet.** Nothing on screen or in notifications should reveal message content to someone looking over a shoulder.
5. **Minimal.** Only 3 tabs and only the screens listed below. No onboarding carousel, profile pictures, stories or emojis panel.

## Vocabulary (use exactly these words in the UI)
- **Nearby**: the one open room everyone in range can read. Public, not private.
- **People**: the list of others who are reachable, plus your Favourites.
- **Direct Message**: a private encrypted 1:1 chat. The UI can just say "Message".
- **Favourite**: a starred person.
- **Verified**: a person you confirmed in person by scanning their QR code. Shown with a ✓ shield badge.
- **Nickname**: a person's display name. It isn't unique. When two people nearby share a Nickname, show a short tag after it, e.g. "Rahul#3f9a".
- **Panic Wipe**: erases everything on the phone instantly.

## Frames to produce
Draw every screen at **Android 360×800** and **iPhone 390×844**, in **dark and light**.

### Global layout
- **Top bar:** the app logo on the left, with the screen title next to it.
  - The logo is also the **Panic Wipe** trigger. Pressing and holding it for 2 seconds fills a ring around it, and letting go early cancels.
  - Design 3 states: idle, about 50% filled, and wiping.
- **Bottom navigation:** 3 tabs, Nearby | People | Settings.

### 1. First launch (single screen)
- A short welcome line ("Chat with people around you — no internet needed").
- An auto-generated Nickname (e.g. "quiet-otter-42") with an edit pencil.
- A permission checklist: Bluetooth, Notifications, and (Android only) "Allow running in background". Each row has one plain sentence explaining why, and a button to allow it.
- A big "Start" button.
- States: all permissions pending, some allowed, and one denied (show what won't work and how to fix it).

### 2. Nearby (home)
- A chat feed of everyone's public messages, each with the sender's Nickname and time, and a message composer at the bottom.
- A header pill showing how many people are reachable (e.g. "12 people nearby").
- States:
  - **Empty / searching:** a gentle animation and the text "Looking for people nearby…"
  - **Active feed:** include two people both named "Rahul", shown with tags.
  - **One-time notice** (bottom sheet): "Nearby is public. Anyone around can read it, and your posts can be linked to you." with a "Got it" button.
  - **Long-press on a message:** a menu with "Message privately", "Add to Favourites" and "Block".
  - **Warning banner:** "Your phone can't be discovered by others, but you can still reach them" (some cheap Android phones).
  - **iPhone-only hint banner:** "Keep hushh open in crowds — iPhones relay less in the background".

### 3. People
- Two sections:
  - **Nearby now**: people currently reachable
  - **Favourites**: starred people, shown "away" (greyed) when out of range
- Each row shows the Nickname (with a tag if needed), the Verified badge, a Favourite star toggle, and reachable/away status. Tapping a row opens a Direct Message.
- States: empty ("No one nearby yet") and populated (mix verified and unverified, reachable and away).

### 4. Direct Message thread
- A header with the Nickname, the Verified badge or an "Unverified" label, and a Favourite star.
- **Banner when unverified:** "Not verified — meet in person and scan their code to be sure it's them" with a "Verify" button.
- Small lock text at the top of the thread: "Messages are end-to-end encrypted and auto-delete after 24 h".
- **Message status:** pending (clock icon, "Will send when they're nearby"), delivered ✓ and read ✓✓.
- States: a normal conversation; the other person is away, with a pending message; and an empty new chat.

### 5. Settings
- **Nickname** (editable).
- **Language:** System / English / हिन्दी.
- **Auto-delete messages after:** 1 h / 24 h (default) / 7 days.
- **Keep relaying in background** toggle. Android only, on by default.
- **Verify a person:** opens screen 6.
- **Panic Wipe:** red and visually separated at the bottom, with a one-line explanation: "Erases all messages, your identity, favourites and settings. Cannot be undone."
- **Small print:** "Tip: hold the logo for 2 seconds to Panic Wipe from anywhere."

### 6. Verify (QR)
- Two tabs: "My code" (a large QR code, your Nickname and a short code like "3F9A 12C0") and "Scan" (a camera viewfinder).
- Result states: success ("Rahul is verified" with a ✓ shield) and failure ("This code doesn't match — it may not be them").

### 7. Panic Wipe
- The hold-on-logo ring states (see Global layout).
- The confirmation dialog for the Settings button: red "Erase everything" and "Cancel".
- After the wipe, the app looks exactly like a fresh first launch (reuse screen 1).

### 8. System surfaces
- **Direct Message notification:** it shows only "hushh — New message", with no sender and no text.
- **Android ongoing notification:** "hushh is relaying messages nearby", with a "Stop" action.

## Platform differences (keep minimal)
- **Android:** Material 3 feel, a system back gesture, and a foreground-service notification.
- **iOS:** iOS safe areas and home indicator, a swipe-back gesture, no background-relay toggle, and the "keep open" hint.
- Everything else is identical on both platforms.

## Components to define
- Message bubble: own and others, plus status icons
- Person row
- Verified badge
- Favourite star
- Nickname tag
- Banners: info, warning and danger
- Bottom sheet
- Panic Wipe ring
- Primary, secondary and danger buttons
- Toggle
- Segmented picker
- Empty states

Use one accent colour; red is only for Panic Wipe and other danger elements.

## Don't
- Don't use bitchat's name, logo or look.
- Don't show phone numbers, avatars that suggest real photos, or "online/last seen" timestamps.
- Don't put message text in notifications.
