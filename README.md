# BraviaYouTubeTV – Android TV App

A full-screen WebView Android TV app that loads `tv.html` as a native app
on your Sony Bravia KD-55 A8F (or any Android TV device).

---

## Project Structure

```
BraviaYouTubeTV/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── tv.html              ← Your YouTube TV web app (bundled)
│   │   ├── java/com/braviatv/app/
│   │   │   └── TVActivity.java      ← WebView shell + D-pad key handling
│   │   ├── res/
│   │   │   ├── layout/activity_tv.xml
│   │   │   ├── values/styles.xml
│   │   │   └── mipmap-hdpi/ic_launcher.png
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## How to Build (Android Studio)

### Prerequisites
- Android Studio Hedgehog (2023.1) or newer
- JDK 17+
- Android SDK 34

### Steps

1. **Open project**
   - Launch Android Studio → File → Open → select this folder

2. **Sync Gradle**
   - Android Studio will prompt to sync. Click "Sync Now".

3. **Update tv.html** (if you've changed it)
   - Replace `app/src/main/assets/tv.html` with your latest `tv.html`

4. **Build APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

5. **Or build from command line**
   ```bash
   ./gradlew assembleDebug
   # APK at: app/build/outputs/apk/debug/app-debug.apk
   ```

---

## How to Install on Bravia KD-55 A8F

### Method 1: ADB over WiFi (Recommended)

1. **Enable Developer Options on TV**
   - Settings → Device Preferences → About → Build (click 7 times)
   - Settings → Device Preferences → Developer Options → ON

2. **Enable ADB Debugging**
   - Developer Options → USB Debugging → ON
   - Developer Options → ADB over Network → ON (note the IP:port shown, e.g. `192.168.1.50:5555`)

3. **Install from your computer**
   ```bash
   # Connect
   adb connect 192.168.1.50:5555

   # Install
   adb install app/build/outputs/apk/debug/app-debug.apk

   # Launch immediately
   adb shell am start -n com.braviatv.app/.TVActivity
   ```

4. **App appears in TV launcher** under "Apps" as "YouTube TV"

### Method 2: USB Sideload

1. Copy APK to a USB drive
2. Install a file manager app on the TV (e.g. "X-plore")
3. Open file manager → navigate to USB → tap APK → Install

### Method 3: Downloader App

1. Install "Downloader" app from Google Play on TV
2. Host the APK on a local server or file share
3. Enter the URL in Downloader

---

## Updating the App

When you change `tv.html`:

```bash
# Copy new html into assets
cp /path/to/tv.html app/src/main/assets/tv.html

# Rebuild and reinstall
./gradlew assembleDebug
adb connect 192.168.1.50:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The `-r` flag reinstalls over the existing version without losing data.

---

## Remote Control Mapping

| TV Remote Button      | Action                    |
|-----------------------|---------------------------|
| Back                  | Close player / search     |
| Search (🔍)           | Open YouTube TV search    |
| Play/Pause            | Toggle playback           |
| Fast Forward          | Skip +10 seconds          |
| Rewind                | Skip -10 seconds          |
| Stop                  | Close player              |
| D-pad Up/Down/Left/Right | Navigate UI elements   |
| D-pad Center / OK     | Select / click            |

---

## Syncing with Mobile

The app uses `localStorage` shared between `tv.html` (in the app) and
`mobile.html` (opened in a browser on the same device/network).

For true cross-device sync:
- Host both HTML files on a local HTTP server (e.g. `python3 -m http.server 8080`)
- Access `mobile.html` from your phone at `http://TV_IP:8080/mobile.html`
- Both pages share the same localStorage via the server origin

---

## Customisation

- **App name**: Edit `app/src/main/res/values/strings.xml`
- **Launcher icon**: Replace `mipmap-hdpi/ic_launcher.png` with a 96×96px PNG
  (for TV launcher banner use 320×180px)
- **Package ID**: Edit `applicationId` in `app/build.gradle`
# androidtv-select
