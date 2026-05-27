# Hiker's Watch

A simple, private, on-device GPS display for Android.
Shows your current latitude, longitude, altitude, accuracy, and reverse-geocoded
address. Share the location, copy it to the clipboard, or open it in your
preferred maps app.

**No accounts. No ads. No trackers. No data leaves your device.**

Live on Google Play: <https://play.google.com/store/apps/details?id=com.nv.nvgeowatch>

## Features

- Real-time GPS readings (lat, lon, altitude, accuracy)
- Reverse-geocoded street address
- Compass with true / magnetic north toggle (rotation-vector sensor)
- Speed and bearing readouts (auto-hide when stationary)
- Live satellite count (in-fix / in-view)
- Barometric pressure and altitude (devices with a barometer)
- Sunrise, sunset, solar noon, day length, and moon phase for your position
- Open a `geo:lat,lon` link from any app — see distance + bearing to the target
- Quick Settings tile and home-screen shortcut to copy your coords instantly
- Keep-screen-on toggle for trail use
- Share / Copy / Open-in-Maps from any fix
- Graceful recovery from denied permissions, blocked permissions, or disabled
  Location Services — no dead ends
- Approximate-location (coarse) support for users who only grant it
- Light and dark themes
- Edge-to-edge UI for Android 15

## Build

Requirements: Android Studio Hedgehog+, JDK 17, Android SDK Platform 35.

```bash
./gradlew clean assembleRelease         # signed APK
./gradlew clean bundleRelease           # signed AAB (for Play Store)
```

Outputs:

- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

The release build is signed via `keystore.properties` (git-ignored) — see the
file's comments for the expected keys.

## Publish to Google Play

Two paths, documented in `scripts/README.md`:

1. **Manual** — Drag the AAB into Play Console → Test and release → choose a
   track → release notes → roll out.
2. **Automated** — `python scripts/play_deploy.py …` once you've created a
   service-account key (one-time browser setup).

## Privacy policy

Hosted via GitHub Pages from `docs/privacy-policy.html`:

<https://naveenverma1.github.io/Hiker-Watch-Android-/privacy-policy.html>

To enable: GitHub repo Settings → Pages → Source: "Deploy from a branch" →
Branch: `master` / folder: `/docs`.

## Permissions

| Permission | Why we need it |
|---|---|
| `INTERNET` | Lets Android's system Geocoder resolve coordinates to an address. |
| `ACCESS_FINE_LOCATION` | Precise GPS. Preferred. |
| `ACCESS_COARSE_LOCATION` | Approximate location. Accepted as a fallback when the user only grants "Approximate". |

No background-location permission. The app stops requesting updates when you
leave the screen.

## License

Personal project by Naveen Verma. All rights reserved.
