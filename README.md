# hushh

**Chat with people around you — no internet needed.**

hushh is a free, open-source chat app for Android and iPhone that works with no internet, SIM, account or phone number. Phones running it connect over Bluetooth and relay each other's messages, so a message can hop from phone to phone across a crowd. It is built first for internet shutdowns, protests and big crowds.

> **Status: pre-alpha.** The design is done; the app is not built yet. Nothing here is ready to rely on for your safety.

"hushh" is a code name; the app will get a different public name before its first release.

## What it does

- **Nearby**: one open room that everyone in range can read and post to. It is public, not private.
- **Direct Messages**: private 1:1 chats, end-to-end encrypted (Noise XX). Phones that relay them can't read them.
- **No accounts**: your phone creates a random identity on first launch. There is no sign-up and no phone number.
- **Verify in person**: scan a friend's QR code to be sure you're talking to them, not an impostor.
- **Panic Wipe**: hold the logo for 2 seconds to erase everything (messages, identity, favourites and settings).
- **Works with bitchat**: hushh speaks the same Bluetooth protocol as [bitchat](https://github.com/permissionlesstech/bitchat), so the two apps can chat and relay for each other. hushh is an independent project with its own code and is not affiliated with bitchat.

## Screens

| Nearby | Direct Message | People | Panic Wipe |
|---|---|---|---|
| <img src="design/exports/android/dark/02b%20Nearby%20%E2%80%94%20active%20feed.png" width="200"> | <img src="design/exports/android/dark/04a%20Message%20%E2%80%94%20conversation.png" width="200"> | <img src="design/exports/android/dark/03b%20People%20%E2%80%94%20populated.png" width="200"> | <img src="design/exports/android/dark/07a%20Panic%20Wipe%20%E2%80%94%20holding%20logo.png" width="200"> |

All screens, in dark and light for both platforms, are in [`design/exports/`](design/exports). The source file is `design/hushh.figcraft`.

## How it's built

- One Kotlin Multiplatform codebase with a Compose Multiplatform UI. Only the Bluetooth layer is native per platform.
- The encryption uses libsodium primitives with a Noise_XX_25519_ChaChaPoly_SHA256 handshake.
- Data stored on the phone is encrypted (SQLDelight + SQLCipher), with the key held in the Android Keystore or iOS Keychain.
- The minimum OS versions are Android 8.0 and iOS 16.

The code is written by AI coding agents (Claude Code) and tested by hand on real phones.

## Build and run

The repo has one shared Kotlin Multiplatform module (`shared/`: core and Compose UI) and two thin app targets (`androidApp/` and `iosApp/`). You need JDK 17 for every build.

### Android (8.0+)

You also need the Android SDK (platform 37). Point `ANDROID_HOME` at it, or put `sdk.dir=...` in `local.properties`.

```sh
./gradlew :androidApp:assembleDebug     # APK in androidApp/build/outputs/apk/debug/
./gradlew :androidApp:installDebug      # install on a connected phone (USB debugging on)
```

Or open the repo root in Android Studio and run `androidApp`.

### iOS (16+)

You also need a Mac with Xcode. Open `iosApp/iosApp.xcodeproj`, choose your team under Signing & Capabilities, pick your iPhone and press Run. Xcode builds the shared Kotlin framework through Gradle, so `java` must be on the `PATH` that Xcode sees (or set `JAVA_HOME`).

The Xcode project and `Info.plist` are generated from `iosApp/project.yml`. To change them, edit that file and run `xcodegen` in `iosApp/` (`brew install xcodegen`), then commit the regenerated files.

### Tests

```sh
./gradlew :shared:allTests
```

CI (`.github/workflows/ci.yml`) runs the shared tests and builds the Android APK on Linux, then builds the iOS app on macOS, on every push.

## Project docs

- **Spec:** [issue #1](https://github.com/sanverite/hushh/issues/1), broken into build tickets as its sub-issues.
- **Vocabulary:** [`GLOSSARY.md`](GLOSSARY.md).
- **Decisions:** [`docs/adr/`](docs/adr), recorded as Architecture Decision Records.
- **Screen design brief:** [`design/BRIEF.md`](design/BRIEF.md).

## Contributing and security

- See [`CONTRIBUTING.md`](CONTRIBUTING.md) to contribute.
- **Found a security problem?** Report it privately as described in [`SECURITY.md`](SECURITY.md). Please don't open a public issue.

## Licence

[Apache-2.0](LICENSE).
