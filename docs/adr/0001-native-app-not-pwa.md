# Native app, not a PWA

hushh ships as a native mobile app rather than a Progressive Web App. Web Bluetooth only lets a page connect to other devices as a client: it cannot advertise, accept connections (act as a GATT peripheral) or run in the background. iOS Safari does not support it at all, and there is no web API for Wi-Fi Direct. A browser page therefore cannot be a node in a Bluetooth mesh. Revisit only if those browser capabilities change.
