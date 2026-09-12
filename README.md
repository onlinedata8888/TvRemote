# TV Remote (Android → Android TV, same WiFi, no root)

Ek simple Android app jo tumhare phone ko Android TV ka remote bana deta hai —
D-pad, Home/Back/Menu, Volume, Power, ek Touchpad (swipe se navigate + tap = OK)
aur Voice Command (bolke command do: "home", "back", "volume up", "netflix kholo" etc).

Connection **Wireless ADB** ke through hota hai — same WiFi par, koi root/cable
nahi chahiye. Yeh library use karta hai: `cgutman/AdbLib` (pure-Java ADB
protocol implementation).

## Features
- D-pad (Up/Down/Left/Right/OK)
- Home, Back, Menu
- Volume Up/Down/Mute
- Power
- Play/Pause, Rewind, Fast-forward
- Touchpad area — swipe = navigate, tap = select, long-press = back
- Voice command button (Android SpeechRecognizer se) → keywords match karke
  ADB keyevent ya app launch karta hai
- IP address save ho jata hai (SharedPreferences), dobara connect karna easy

## TV side setup (ek baar)
1. TV par **Settings → Device Preferences → About** me jaakar "Build" par
   7 baar tap karo → Developer Options on ho jayega.
2. **Settings → Device Preferences → Developer Options** me jaao.
3. **Network debugging / Wireless debugging** ON karo.
   - Android 11+ TV: "Wireless debugging" → "Pair device with pairing code" se
     ek 6-digit code aur port milega — pehli baar connect karte waqt yeh use
     hoga (see note below).
   - Purane Android TV (jinme sirf "USB debugging"/"Network debugging" hota
     hai): seedha `IP:5555` par app se connect ho jayega.
4. TV ka IP address note karo (Settings → Network → your WiFi → IP address).

## App me connect kaise kare
1. App kholo, phone aur TV **same WiFi** par hone chahiye.
2. Upar wale box me TV ka IP address daalo (port default 5555 hai; agar
   pairing wala flow use kar rahe ho to pehle pairing port pe ek baar pair
   karna padega — abhi is app me sirf connect (5555) implement hai, pairing
   step manually TV ke ADB pairing screen se karna hoga agar TV Android 11+
   hai).
3. **Connect** dabao. Pehli baar TV screen par ek "Allow USB debugging?"
   jaisa popup aayega — "Allow" karo. Uske baad remote turant kaam karega.

## Build kaise kare (GitHub → Android Studio)
1. Yeh poora folder GitHub repo me push karo (ya zip GitHub par upload karke
   clone karo).
2. Android Studio kholo → **Open** → is project folder ko select karo.
3. Gradle sync hone do (internet chahiye — `jitpack.io` se AdbLib download
   hoga).
4. Phone connect karo ya emulator chalao → **Run ▶**.
5. APK chahiye ho to: **Build → Build Bundle(s)/APK(s) → Build APK(s)**.

## Important note
`cgutman/AdbLib` ek chhota community library hai; iski exact method-naming
Gradle sync ke time thoda adjust karni pad sakti hai agar library ka koi
naya/alag version resolve ho (jaise `AdbConnection.create(...)` ya
`AdbCrypto.loadAdbKeyPair(...)` signature). Agar build error aaye:
1. `https://github.com/cgutman/AdbLib` repo ka README dekho for exact API.
2. `AdbRemoteClient.kt` file me sirf usi class/method ke naam match karne
   honge — baaki sara app logic waisa hi rahega.

## Permissions
- `INTERNET` — TV se WiFi par connect karne ke liye.
- `RECORD_AUDIO` — Voice command (speech-to-text) ke liye, runtime pe app
  khud maang legi.

## GitHub Actions (auto-build)
`.github/workflows/build.yml` already added hai — jaise hi tum GitHub par
`main` branch par push karoge, GitHub khud APK build kar dega (Actions tab
me jaake "tv-remote-debug-apk" artifact download kar sakte ho, Android
Studio khole bina).

## Folder structure
```
TVRemote/
  .github/workflows/build.yml   <- auto-build APK on push
  app/
    src/main/
      java/com/remote/tvremote/   <- Kotlin source
      res/                        <- layouts, strings, colors
      AndroidManifest.xml
    build.gradle
  build.gradle
  settings.gradle
  gradle.properties
  gradlew / gradlew.bat / gradle/wrapper/  <- gradle wrapper (build without Android Studio too)
```

Enjoy your remote 🎮📺
