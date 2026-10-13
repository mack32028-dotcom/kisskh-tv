# KissKH TV — unofficial Android TV website wrapper

This is an **unofficial** Android TV WebView launcher for `https://kisskh.do/`. It is not affiliated with KissKH. No streaming content is embedded or downloaded. Only use services/content you are entitled to view.

## Features
- Android TV / Google TV Leanback launch entry, custom icon and 640×360 TV banner
- Landscape immersive WebView with JavaScript, DOM storage and HTML5 media
- D-pad **virtual touch cursor** overlay; OK sends a click, directional keys move the cursor
- Hold OK (long press and release) or MENU/INFO to switch between touch cursor and native focus navigation
- Back closes video fullscreen, otherwise traverses history, otherwise exits the app
- Android `WebChromeClient` fullscreen custom view for compliant HTML5 players
- CHANNEL UP/DOWN: scroll page; F5 (external keyboard): refresh

## Open and build with Android Studio
1. Install Android Studio, Android SDK Platform **35**, and JDK **17** (Android Studio bundled JDK is suitable).
2. Unzip this project and select **File → Open**; choose the `KissKHTV` directory.
3. Set Gradle to use the **Gradle wrapper distribution** if available. This archive includes `gradle/wrapper/gradle-wrapper.properties` but **not** the wrapper JAR/executables. To generate these with a locally installed Gradle 8.11.1, run `gradle wrapper --gradle-version 8.11.1` at the project root. Alternatively, configure Android Studio to use a locally installed Gradle 8.11.1 for the first sync, then run the `wrapper` task.
4. Wait for Gradle sync. Internet access is needed to download the Android Gradle Plugin and Android SDK dependencies.
5. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.
7. Optionally build a signed release under **Build → Generate Signed Bundle / APK**. Keep your signing key private.

## Install on Hisense Google TV / Android TV
1. Transfer the resulting APK using a trusted USB file manager or Android Debug Bridge.
2. Allow the file manager to **Install unknown apps** in TV Settings (menu varies by firmware).
3. Open the APK in your TV file manager and install it. Launch **KissKH TV** from Apps.

### ADB alternative
Enable developer options and USB/network debugging on the TV (exact procedure varies by model), then:

```sh
adb connect TV_IP_ADDRESS:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Limitations and troubleshooting
- This is a browser wrapper. KissKH may block WebView, change domains, present bot checks, or use DRM / third-party video players that are incompatible with WebView. **Playback and subtitles have not been verified on a Hisense TV.**
- The cursor sends taps to the WebView; complex drag gestures, embedded cross-site players and special remote controls may require changes.
- Some third-party pages may fail because the wrapper intentionally blocks non-HTTPS top-level URLs and cleartext traffic. No ad or DRM bypass is implemented.
- Use MENU to switch to native D-pad focus if on-screen text inputs don't respond to cursor taps. TV keyboard input depends on the firmware.
- No native account integration, offline downloads, streaming catalog, or media piracy feature is included.
- This source archive is **not a compiled APK**; an Android SDK/Gradle build environment is required.

## Configuration
Edit `START_URL` in `app/src/main/java/com/kisskhtv/launcher/MainActivity.java`. The URL is currently `https://kisskh.do/`.

## Compatibility
- minSdk 23 (Android 6+), targetSdk 35, compiled against API 35.
- Android Gradle Plugin 8.9.2 and Gradle 8.11.1; Java source 17.

## GitHub Actions: compile and distribute the APK
This repository includes `.github/workflows/android-apk.yml`, which compiles the Android TV APK on GitHub's Linux runners. No Android Studio installation is required for this path.

1. On GitHub, create a **new empty repository** named `KissKHTV` (public if the TV must access its release page without logging in). Do not initialize it with a README.
2. Upload/push the **contents of this `KissKHTV` folder**, including the hidden `.github/workflows/android-apk.yml` path, to the repository's `main` branch. Prefer `git` on your Mac, because web uploads can overlook hidden folders.
3. In your repo open **Actions → Build Android TV APK** and verify that the build passes. `Actions → ... → Artifacts` hosts a ZIP artifact but may require GitHub login.
4. To publish an easy-to-download APK on **Releases**, create a version tag such as `v1.0.0` against the successful main commit. On your Mac, inside the checked-out repository run:

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

   Alternatively create a tag through GitHub's Releases interface, then verify the Actions workflow creates the GitHub Release with the `KissKHTV.apk` asset.
5. Your TV can open `https://github.com/USERNAME/KissKHTV/releases/latest` and download `KissKHTV.apk` from the assets list. If browser downloads from GitHub are troublesome, transfer the APK via USB or a trusted file-transfer app.

**IMPORTANT: Signing and updates.** This CI workflow creates a debug-signed APK. Each fresh GitHub Actions runner can generate a different debug signing key, so subsequent APKs may not install **over** a previous version. For a new version, you may need to uninstall the existing app first (which removes its app data). For seamless updates, change CI to use a securely stored, persistent **release signing key** (in GitHub Actions secrets) and increment `versionCode`. Do not commit private keys or credentials to the repository.

**Security:** A public source repository is optional, but a *public Release* is necessary for an anonymous TV download. Only install the APK generated in your own repository. Confirm that the site you access and content viewed are authorized and trustworthy. This is an unofficial, unverified WebView wrapper, not an official KissKH app.
