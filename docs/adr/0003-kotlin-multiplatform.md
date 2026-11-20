# Kotlin Multiplatform with Compose Multiplatform UI

We ship iOS and Android from day one using one Kotlin Multiplatform codebase. The protocol, crypto, routing, storage and Panic Wipe live in shared Kotlin, and the UI uses Compose Multiplatform. Only the Bluetooth layer is native per platform (CoreBluetooth on iOS, Android BLE APIs). The security-critical logic then exists exactly once instead of being written twice in Swift and Kotlin, as bitchat does.

## Considered Options

- **Two native apps:** rejected because every security-critical path would be duplicated.
- **Flutter or React Native:** rejected because their Bluetooth plugins are weak at the peripheral role, so the Bluetooth layer would have to be native anyway, behind an extra bridge.
- **KMP with separate SwiftUI and Compose UIs:** rejected for now because it means two UIs.
