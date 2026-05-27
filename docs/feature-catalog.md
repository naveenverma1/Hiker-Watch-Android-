# Feature Catalog — Hiker's Watch (every option, ranked)

This is the **exhaustive** list of features we could plausibly add, distilled from:

- The 11 Play Store competitors we surveyed (`competitive-research-2026-05.md`)
- The 10+ FOSS GitHub apps we read in detail (`competitive-research-addendum-2026-05.md`)
- Reddit `/r/Hiking` and `/r/fossdroid` threads
- Android platform capabilities we're not yet using
- "What if?" features that no competitor has yet

Each row is rated on:

- **Tier** — when to ship: P0 (this release), P1 (next 1–2 releases), P2 (someday), P3 (never / out of scope)
- **Effort** — engineering size: S (≤4h), M (1d), L (2–5d), XL (1+ week)
- **Fit** — alignment with our positioning *(privacy-first, no-ads, lightweight, single-screen)*: ✅ on-strategy, ⚠️ risky, ❌ off-strategy
- **Permission** — does adding it require a new Android permission?

The legend stays the same across all sections below.

---

## A. Core GPS display enhancements

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| A1 | Already shipped: lat, lon, altitude, accuracy, address | — | — | — | — |
| A2 | **Compass with magnetic + true north toggle** | **P0 (v2.4)** | M | ✅ | none |
| A3 | **Speed display (auto-hide when stationary)** | **P0 (v2.4)** | S | ✅ | none |
| A4 | **Satellite count + signals in fix** (GnssStatus.Callback) | **P0 (v2.4)** | S | ✅ | none |
| A5 | **Bearing** — direction the device is pointing | **P0 (v2.4)** | S | ✅ | none |
| A6 | **Heading** — direction of motion (different from bearing) | P1 | S | ✅ | none |
| A7 | GNSS constellation breakdown (GPS / GLONASS / Galileo / BeiDou / IRNSS counts) | P1 | M | ✅ | none |
| A8 | HDOP / PDOP / VDOP — dilution of precision metrics | P2 | M | ✅ | none |
| A9 | Magnetic field strength in µT (like MBCompass does) | P2 | S | ✅ | none |
| A10 | Time-to-first-fix indicator | P2 | M | ✅ | none |
| A11 | Accuracy circle / radius visualization | P2 | M | ✅ | none |
| A12 | Altitude above ellipsoid vs MSL (geoid correction) | P2 | M | ✅ | none |
| A13 | Heading-up vs north-up compass mode | P2 | S | ✅ | none |
| A14 | Magnetic declination indicator (signed degrees) | P2 | S | ✅ | none |

---

## B. Coordinate systems & formats

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| B1 | Already shipped: decimal degrees + DMS | — | — | — | — |
| B2 | **DDM (degrees + decimal minutes)** — used by aviation/marine | P1 | S | ✅ | none |
| B3 | **Plus Codes (Open Location Code, Google)** — short shareable codes | P1 | S | ✅ | none |
| B4 | **UTM coordinates** — required by surveyors, military, search & rescue | P1 | M | ✅ | none |
| B5 | **MGRS (Military Grid Reference System)** | P1 | M | ✅ | none |
| B6 | Maidenhead grid locator — used by amateur radio | P2 | S | ✅ | none |
| B7 | What3Words-style 3-word encoding (our own implementation, fully offline) | P2 | L | ⚠️ | none |
| B8 | CH1903 / Swiss Grid | P3 | M | ⚠️ niche | none |
| B9 | OSGB36 / British National Grid | P3 | M | ⚠️ niche | none |
| B10 | Geohash | P2 | S | ✅ | none |
| B11 | QR code of current location (offline render) | P1 | S | ✅ | none |
| B12 | Convert any coordinate format to any other on demand | P2 | M | ✅ | none |

---

## C. Astronomy & time (all locally computed, no network)

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| C1 | **Sunrise / sunset** | **P1 (v2.5)** | S | ✅ | none |
| C2 | **Civil / nautical / astronomical twilight** | **P1 (v2.5)** | S | ✅ | none |
| C3 | **Golden hour / blue hour** — photographers love this | P1 | S | ✅ | none |
| C4 | **Moon phase + illumination %** | **P1 (v2.5)** | S | ✅ | none |
| C5 | **Moonrise / moonset / moon transit** | P1 | S | ✅ | none |
| C6 | **Direction (azimuth) of sunrise/sunset/moonrise** — arrow on compass | P1 | M | ✅ | none |
| C7 | Sun's current azimuth + altitude in sky | P1 | S | ✅ | none |
| C8 | Solar noon time | P1 | S | ✅ | none |
| C9 | Day length / night length | P2 | S | ✅ | none |
| C10 | Local time + UTC + ISO 8601 simultaneously | P2 | S | ✅ | none |
| C11 | Timezone offset + DST indicator | P2 | S | ✅ | none |
| C12 | Julian date / day-of-year / week number | P3 | S | ⚠️ | none |
| C13 | Next equinox / solstice countdown | P3 | M | ⚠️ delightful but extra | none |
| C14 | Live sun-direction arrow that rotates with the device | P2 | M | ✅ | none |

---

## D. Environment & device sensors (no network)

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| D1 | **Barometric pressure (if sensor present)** | **P1 (v2.5)** | S | ✅ | none |
| D2 | **Pressure-based altitude (sea-level calibration)** | P1 | M | ✅ | none |
| D3 | **Pressure trend over the last 3h** — weather hint | P1 | M | ✅ | none |
| D4 | Magnetic field strength in µT | P2 | S | ✅ | none |
| D5 | Ambient temperature (very few phones have a sensor) | P3 | S | ❌ rarely useful | none |
| D6 | Ambient humidity (almost no phones) | P3 | S | ❌ | none |
| D7 | Ambient light in lux | P3 | S | ⚠️ gimmick | none |
| D8 | Step counter integration (Activity Recognition) | P2 | M | ⚠️ adds permission | `ACTIVITY_RECOGNITION` |

---

## E. Persistence & waypoints

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| E1 | **Save current location with a name** | **P1 (v2.6)** | M | ✅ | none |
| E2 | **List of saved waypoints (max ~50, JSON in SharedPreferences)** | **P1 (v2.6)** | M | ✅ | none |
| E3 | **Edit / delete / star waypoint** | **P1 (v2.6)** | S | ✅ | none |
| E4 | **Distance + bearing to a saved waypoint from current location** | P1 | M | ✅ | none |
| E5 | **"Back to waypoint" arrow** — compass rotates to point at it | P1 | M | ✅ | none |
| E6 | **GPX export** of saved waypoints (Storage Access Framework) | **P1 (v2.6)** | S | ✅ | none |
| E7 | **GPX import** to load waypoints from another app | P2 | M | ✅ | none |
| E8 | KML export | P3 | S | ⚠️ rarely used | none |
| E9 | Recent locations history (last N, opt-in) | P2 | M | ⚠️ privacy nuance | none |
| E10 | Photo attached to a waypoint (Storage Access Framework, not raw camera) | P2 | M | ✅ | none |

---

## F. Trip / activity tracking (local only)

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| F1 | Start / pause / stop trip recording | P2 | L | ⚠️ scope creep | none for foreground |
| F2 | Cumulative trip distance | P2 | M | ⚠️ | none |
| F3 | Cumulative elevation gain/loss | P2 | M | ⚠️ | none |
| F4 | Average speed during trip | P2 | S | ⚠️ | none |
| F5 | Max altitude reached | P2 | S | ⚠️ | none |
| F6 | Trip summary view + history list | P2 | L | ⚠️ | none |
| F7 | Background trip recording (foreground service + notification) | P3 | XL | ❌ triggers Play re-review | `POST_NOTIFICATIONS` + foreground service |
| F8 | GPX export of trips | P2 | S | ✅ | none |

Note: F1–F6 are the boundary line. They add real value but push us toward "GPS Logger" territory. Worth doing only if user analytics show repeat sessions.

---

## G. Sharing & communication

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| G1 | Already shipped: share via system share sheet | — | — | — | — |
| G2 | Already shipped: copy to clipboard | — | — | — | — |
| G3 | Already shipped: open in Maps (geo: intent) | — | — | — | — |
| G4 | **QR code dialog** for current location | P1 | S | ✅ | none |
| G5 | **NFC tap-to-share** to another phone | P2 | M | ⚠️ low adoption | `NFC` |
| G6 | **One-tap SMS** to a pre-set emergency contact | P2 | M | ⚠️ contacts perm | `SEND_SMS` |
| G7 | Custom share format selector — pick which coordinate format gets shared | P1 | S | ✅ | none |
| G8 | "Email my location" pre-filled mailto | P2 | S | ✅ | none |
| G9 | Direct WhatsApp / Telegram share via deep link | P3 | S | ❌ links to closed-source services | none |
| G10 | APRS-style position report (amateur radio) | P3 | M | ⚠️ very niche | none |

---

## H. Safety & emergency

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| H1 | "I'm here" big-button mode — large coords, easy to read in the cold/rain | P1 | S | ✅ | none |
| H2 | Pre-set emergency contact → SMS my location + "I need help" | P2 | M | ⚠️ adds perm | `SEND_SMS` |
| H3 | Auto-beacon: send location every 10 min to a contact (opt-in) | P3 | L | ❌ contradicts privacy promise | `SEND_SMS` |
| H4 | Last-known-good location preserved on relaunch | P1 | S | ✅ | none |
| H5 | Battery-saver mode — minimal UI, 30-sec poll | P2 | M | ✅ | none |
| H6 | Flashlight toggle (some phones have it via CameraManager) | P2 | S | ⚠️ scope drift | `FLASHLIGHT` (no runtime perm) |
| H7 | Strobe / SOS Morse light | P3 | M | ⚠️ | `FLASHLIGHT` |
| H8 | Loud-alarm button (Trail Sense has this) | P3 | S | ⚠️ scope drift | none |
| H9 | "Find my car" — drop a pin where I parked | P2 | M | ✅ | none |

---

## I. Navigation helpers (without a map)

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| I1 | Distance + bearing to a saved waypoint | P1 | M | ✅ | none |
| I2 | "Compass arrow" pointing toward a waypoint | P1 | M | ✅ | none |
| I3 | Distance from origin (where I launched the app today) | P2 | S | ✅ | none |
| I4 | Linear distance to a famous point (paste a coord, get range) | P2 | S | ✅ | none |
| I5 | Distance to the antipode (the point directly opposite on the globe) | P3 | S | ⚠️ delightful but obscure | none |
| I6 | "Geocaching mode" — enter a target coord, get distance/bearing | P2 | M | ✅ | none |

---

## J. Map / visualization (the minimal kind)

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| J1 | Embedded interactive map with tiles | P3 | XL | ❌ different product; bloats AAB | none |
| J2 | Static map *preview* image (one-time PNG from OpenStreetMap static URL) | P2 | S | ⚠️ first network call ever | none |
| J3 | Visual compass dial (rotating, animated) | P1 | M | ✅ | none |
| J4 | Analogue altimeter dial | P2 | M | ⚠️ skeuomorphic, design call | none |
| J5 | Speedometer dial | P2 | M | ⚠️ same | none |
| J6 | Skyplot of GNSS satellites (azimuth/elevation chart) | P2 | M | ✅ | none |
| J7 | Day/night terminator on a tiny world map | P3 | M | ⚠️ delightful | none |

---

## K. System integration (Android platform capabilities)

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| K1 | **Home-screen widget — current coords + last fix time** | P1 | M | ✅ | none |
| K2 | **App shortcut (long-press launcher icon) → "Copy my coords"** | P1 | S | ✅ | none |
| K3 | **Quick Settings tile** — long-press for current location *(unique; no competitor has)* | P1 | M | ✅ | none |
| K4 | Always-on display variant (low-power) | P2 | L | ⚠️ | none |
| K5 | Picture-in-picture mode (some competitors have) | P2 | M | ✅ | none |
| K6 | Lock-screen widget (Android 14+) | P2 | M | ✅ | none |
| K7 | Wear OS companion app | P3 | XL | ⚠️ separate module | none |
| K8 | Intent API — let other apps query our location | P2 | M | ✅ | declared `<intent-filter>` |
| K9 | Receive intent — "open this lat,lon in Hiker's Watch" | P1 | S | ✅ | declared `<intent-filter>` |
| K10 | Tasker / Macrodroid integration | P3 | M | ⚠️ niche | none |
| K11 | App link from `https://hiker-watch.app/?lat=...&lon=...` | P2 | S | ⚠️ needs a domain | none |
| K12 | Google Assistant integration ("Hey Google, what's my latitude?") | P3 | M | ⚠️ requires App Actions | none |

---

## L. Personalization

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| L1 | Already shipped: light + dark themes | — | — | — | — |
| L2 | **System / Light / Dark theme picker** (manual override) | P1 | S | ✅ | none |
| L3 | **Material You dynamic color (Android 12+)** | P1 | S | ✅ | none |
| L4 | **Keep screen on while app is open** (toggle in Settings) | P1 | S | ✅ | `WAKE_LOCK` (no runtime perm) |
| L5 | Text size accessibility scaling | P1 | S | ✅ | none |
| L6 | Brightness lock (max brightness while in app) | P2 | S | ✅ | none |
| L7 | Card order customization (drag to reorder) | P2 | M | ⚠️ | none |
| L8 | Show / hide individual data points | P2 | S | ⚠️ | none |
| L9 | Alternative app icons (the user picks the icon style) | P2 | M | ⚠️ | none |
| L10 | Custom accent color | P2 | M | ⚠️ MBCompass has this | none |
| L11 | Mono / sans / serif font picker | P3 | S | ⚠️ | none |
| L12 | AMOLED true-black theme variant | P1 | S | ✅ | none |
| L13 | High contrast mode | P2 | S | ✅ | none |
| L14 | Onboarding tour for first-time users | P2 | M | ✅ | none |
| L15 | "Tap to highlight" — tap a value, it pulses + reads aloud | P3 | S | ⚠️ | none |

---

## M. Accessibility

| # | Feature | Tier | Effort | Fit | Permission |
|---|---|:---:|:---:|:---:|:---:|
| M1 | TalkBack support — `contentDescription` everywhere | P1 | M | ✅ | none |
| M2 | Speak current location aloud (TTS) | P1 | S | ✅ | none |
| M3 | Vibration feedback on each fix | P2 | S | ✅ | `VIBRATE` (no runtime perm) |
| M4 | Color-blind safe palette toggle | P2 | S | ✅ | none |
| M5 | Larger-tap-target mode | P2 | S | ✅ | none |
| M6 | RTL language support | P1 | S | ✅ | none |
| M7 | High-contrast border for cards (legibility) | P2 | S | ✅ | none |

---

## N. Distribution, OSS hygiene, and marketing (no code)

These are not features, but they multiply the impact of every feature we ship.

| # | Action | Tier | Effort |
|---|---|:---:|:---:|
| N1 | **Add a `LICENSE` file** (MIT or Apache-2.0) | **P0** | 30 sec |
| N2 | **`gh release create v2.3`** with the APK attached | **P0** | 5 min |
| N3 | **Add GitHub topics**: `gps`, `android`, `privacy`, `f-droid`, `no-ads`, `open-source`, `hiking` | **P0** | 5 min |
| N4 | Add `fastlane/metadata/` for F-Droid auto-pickup | P1 | 1 h |
| N5 | Submit to F-Droid | P1 | 1 h (then ~2 wk wait) |
| N6 | Submit to IzzyOnDroid | P1 | 30 min |
| N7 | Submit to `awesome-gnss` list | P1 | 15 min PR |
| N8 | Add `CONTRIBUTING.md` + `SECURITY.md` + `CODE_OF_CONDUCT.md` | P1 | 30 min total |
| N9 | Add a `.github/workflows/android.yml` that runs unit tests on push | P1 | 1 h |
| N10 | Set up Weblate or Crowdin for translations | P1 | 1 h |
| N11 | Refresh Play Store listing copy (lead with "no ads, no trackers") | P1 | 1 h |
| N12 | Refresh Play Store screenshots from the current 2.3 build | P1 | 1 h |
| N13 | New feature graphic (1024×500 PNG) | P1 | 1 h |
| N14 | Submit to `alternativeto.net` as alternative to GPS Test / GPS Status | P2 | 15 min |
| N15 | Submit to Privacy Guides | P2 | 30 min |
| N16 | Launch post on `/r/fossdroid` (look at MBCompass's 62-upvote thread) | P2 | 1 h |
| N17 | Donate link in About dialog (Buy Me a Coffee / GitHub Sponsors) | P2 | 30 min |

---

## O. Things we shouldn't build (and the reason)

| # | Tempting feature | Why no |
|---|---|---|
| O1 | Full offline tile maps | 5-month build. Different product. Copies Locus / AllTrails. |
| O2 | Background location tracking | Triggers Play re-review. Contradicts privacy promise. |
| O3 | Social features / friend sharing | `/r/Hiking` literally asks for apps *without* these. |
| O4 | In-app ads | Reviews of competitors are uniformly hostile to ads (see § Pain Points in main research). |
| O5 | Subscription model | Same as O4. Users hate it in this category. |
| O6 | Telemetry / analytics / Crashlytics | Contradicts the published privacy policy. |
| O7 | "Pro" upgrade gating any current feature | Would break trust with current users. |
| O8 | Camera integration | Adds a major permission. Already exists in dedicated apps. |
| O9 | Cloud backup of waypoints | Contradicts privacy promise (data leaves the device). |
| O10 | Custom map tile downloads | Different product (Locus territory). |

---

## P. The shortlist — top 12 features in deploy order

If we did **everything in this list in priority order**, here are the first twelve actions:

| # | Action | Tier | Effort | Ships in |
|---|---|:---:|:---:|---|
| 1 | LICENSE + GitHub Release + Topics | **P0** | 30 min | This week, no code |
| 2 | A2 Compass with magnetic + true north | **P0** | M | v2.4 |
| 3 | A3 Speed display | **P0** | S | v2.4 |
| 4 | A4 Satellite count badge | **P0** | S | v2.4 |
| 5 | A5 Bearing readout | **P0** | S | v2.4 |
| 6 | K2 App shortcut → "Copy my coords" | **P0** | S | v2.4 |
| 7 | K9 Receive `geo:` intent (lat/lon → open in HW) | **P0** | S | v2.4 |
| 8 | L4 Keep-screen-on toggle | **P0** | S | v2.4 |
| 9 | C1–C4 Sunrise/sunset/twilight/moon phase | **P1** | M | v2.5 |
| 10 | C6 Direction-of-sunrise arrow on compass | **P1** | M | v2.5 |
| 11 | D1–D3 Barometric pressure + altitude calibration + trend | **P1** | M | v2.5 |
| 12 | E1–E6 Waypoints (save + list + GPX export) | **P1** | L | v2.6 |

Everything beyond that is **truly nice-to-have** — the app would already be the best-in-class on its position.

---

## Q. The "moats" — features that no competitor has

If we want to be *truly* distinctive, these are the items that **none of the 11 competitors offer**. Each is a single-sentence pitch line for the next Play listing rewrite:

1. **Quick Settings tile** → *"Pull down. There's your latitude."*
2. **App shortcut → "Copy my coords"** → *"Long-press the icon. Coords on the clipboard. Done."*
3. **Plus Codes** as a first-class format → *"Share your location anywhere with a 9-character code."*
4. **QR code dialog** for offline coordinate sharing → *"Hand your phone to anyone. They scan. They know where you are."*
5. **Receive `geo:` intent** → *"Tap any coordinate link in any app, see it in Hiker's Watch."*
6. **GPX export of waypoints** with built-in mailto → *"Email your camp spots in one tap."*
7. **Direction-of-sunrise/sunset arrow on the compass** → *"Don't just know what time the sun sets — know which way to face."*
8. **Magnetic field strength indicator** (visible artifact of "this app actually reads your sensors") → *"See µT live. Trust the compass."*

The shipping order doesn't have to match the user value order — but **at least three of these** belong in v2.4 alongside the basic-compass work. Quick Settings, app shortcut, and `geo:` receive intent are all small (S effort) and each is a real moat.

---

## R. What's been shipped (state of v2.3 — May 27, 2026)

For clarity, this is what already works in production:

- ✅ Lat / lon (decimal + DMS toggle)
- ✅ Altitude (meters + feet toggle)
- ✅ Accuracy in meters
- ✅ Reverse-geocoded address
- ✅ Share / Copy / Open-in-Maps action row
- ✅ Tap-to-copy on each card
- ✅ Refresh action in toolbar
- ✅ Permission first-launch UX + recovery banners
- ✅ Geocoder timeout (10 s)
- ✅ Dark mode (system-driven)
- ✅ Edge-to-edge Android 15 layout
- ✅ Splash screen
- ✅ Predictive back
- ✅ Privacy policy page (GitHub Pages)
- ✅ Signed AAB + APK shipped to Play Production at 100 %
- ✅ Unit tests (9/9 passing)

Everything in the catalog above is in addition to the above.

---

Last updated: 2026-05-27.
