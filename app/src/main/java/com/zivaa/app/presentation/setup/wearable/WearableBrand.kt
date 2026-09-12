package com.zivaa.app.presentation.setup.wearable

enum class WearableBrand(
    val id: String,
    val displayName: String,
    val tagline: String,
    val packageName: String,
    val playStorePackage: String,
    val bridgeTitle: String,
    val bridgeSteps: List<String>,
    val pairingSteps: List<String>
) {
    SAMSUNG(
        id = "samsung",
        displayName = "Samsung Galaxy Watch",
        tagline = "Galaxy Watch 4, 5, 6, 7 & Ultra",
        packageName = "com.sec.android.app.shealth",
        playStorePackage = "com.sec.android.app.shealth",
        bridgeTitle = "Allow Samsung Health to Share Data",
        bridgeSteps = listOf(
            "Open Samsung Health on your phone.",
            "Tap More (⋮ or Settings) in the top-right corner.",
            "Select Health Connect and turn ON 'Allow all'."
        ),
        pairingSteps = listOf(
            "Turn on your Galaxy Watch and keep it near your phone.",
            "Open Galaxy Wearable or Samsung Health.",
            "Follow the on-screen prompt to pair via Bluetooth."
        )
    ),

    OURA(
        id = "oura",
        displayName = "Oura Ring",
        tagline = "Oura Ring Gen 3, Gen 4 & Horizon",
        packageName = "com.ouraring.oura",
        playStorePackage = "com.ouraring.oura",
        bridgeTitle = "Connect Oura to Health Connect",
        bridgeSteps = listOf(
            "Open the Oura app on your phone.",
            "Tap the Menu icon (≡) in the top-left corner.",
            "Tap Settings, then Data Sharing.",
            "Tap Health Connect and select 'Allow all'."
        ),
        pairingSteps = listOf(
            "Place your Oura Ring on its charger.",
            "Open the Oura app on your phone.",
            "Follow the in-app guide to pair your ring."
        )
    ),

    ULTRAHUMAN(
        id = "ultrahuman",
        displayName = "Ultrahuman Ring",
        tagline = "Ring AIR & M1 Continuous Tracker",
        packageName = "com.ultrahuman",
        playStorePackage = "com.ultrahuman",
        bridgeTitle = "Sync Ultrahuman with Health Connect",
        bridgeSteps = listOf(
            "Open the Ultrahuman app.",
            "Tap your Profile icon in the bottom-right corner.",
            "Go to Settings, then Data & Integration.",
            "Select Google Health Connect and tap 'Sync All'."
        ),
        pairingSteps = listOf(
            "Place your Ultrahuman Ring on the charging dock.",
            "Open the Ultrahuman app on your phone.",
            "Tap 'Pair Device' and hold your ring close."
        )
    ),

    FITBIT(
        id = "fitbit",
        displayName = "Fitbit / Pixel Watch",
        tagline = "Google Pixel Watch, Charge, Sense, Inspire",
        packageName = "com.fitbit.FitbitMobile",
        playStorePackage = "com.fitbit.FitbitMobile",
        bridgeTitle = "Connect Fitbit to Health Connect",
        bridgeSteps = listOf(
            "Open the Fitbit app on your phone.",
            "Tap the 'You' tab at the bottom, then Settings (gear icon).",
            "Tap 'Connected apps', then Health Connect.",
            "Turn ON 'Sync with Health Connect' and allow all."
        ),
        pairingSteps = listOf(
            "Turn on your Fitbit or Pixel Watch.",
            "Open the Fitbit app and sign in with Google.",
            "Tap 'Add Device' and follow the pairing steps."
        )
    ),

    GABIT(
        id = "gabit",
        displayName = "Gabit Smart Ring",
        tagline = "Gabit Ring & Health Band",
        packageName = "com.gabit.app",
        playStorePackage = "com.gabit.app",
        bridgeTitle = "Connect Gabit to Health Connect",
        bridgeSteps = listOf(
            "Open the Gabit app on your phone.",
            "Go to Profile, then Connected Wearables.",
            "Select Google Health Connect.",
            "Tap 'Connect' and allow all permissions."
        ),
        pairingSteps = listOf(
            "Place your Gabit Ring on its charging case.",
            "Open the Gabit app on your phone.",
            "Tap 'Connect Ring' and accept the Bluetooth request."
        )
    ),

    GARMIN(
        id = "garmin",
        displayName = "Garmin",
        tagline = "Venu, Forerunner, vívoactive, Lily",
        packageName = "com.garmin.android.apps.connectmobile",
        playStorePackage = "com.garmin.android.apps.connectmobile",
        bridgeTitle = "Connect Garmin Connect",
        bridgeSteps = listOf(
            "Open the Garmin Connect app.",
            "Tap More (⋯) in the bottom-right corner.",
            "Select Settings, then Connected Apps.",
            "Tap Health Connect and tap 'Turn On'."
        ),
        pairingSteps = listOf(
            "Turn on your Garmin watch.",
            "Open Garmin Connect on your phone.",
            "Tap 'Add Device' and follow the pairing code prompt."
        )
    ),

    INDIAN_BRANDS(
        id = "indian_brands",
        displayName = "Noise / boAt / Amazfit",
        tagline = "NoiseFit, boAt Crest, Zepp & others",
        packageName = "com.mediatek.wwsync.noise",
        playStorePackage = "com.mediatek.wwsync.noise",
        bridgeTitle = "Connect Your Watch App",
        bridgeSteps = listOf(
            "Open your watch's companion app (NoiseFit, boAt Crest, or Zepp).",
            "Go to your Profile or Settings tab.",
            "Look for 'Third-Party Access' or 'Health Connect'.",
            "Connect and allow sharing."
        ),
        pairingSteps = listOf(
            "Turn on your smartwatch and keep it close to your phone.",
            "Open your watch's companion app.",
            "Scan the QR code on your watch or tap 'Add Device'."
        )
    ),

    NONE(
        id = "none",
        displayName = "I don't have a wearable yet",
        tagline = "Use your phone's built-in step tracker for now",
        packageName = "",
        playStorePackage = "",
        bridgeTitle = "Phone Step Tracker Ready",
        bridgeSteps = emptyList(),
        pairingSteps = emptyList()
    );

    companion object {
        fun fromId(id: String?): WearableBrand {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}
