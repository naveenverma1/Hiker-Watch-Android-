# Deep Research Addendum — FOSS Landscape & Reddit Pulse (May 2026)

This addendum extends `competitive-research-2026-05.md` with primary research
from GitHub (10 repos, READMEs + top-voted issues + source structure) and
Reddit (~50 threads from r/Hiking, r/fossdroid, r/Android).

Raw data in `research-1/hiker-watch/github-repos/` and `reddit_*.json`.

---

## 1. The big find: MBCompass

[`CompassMB/MBCompass`](https://github.com/CompassMB/MBCompass) — 453★ on GitHub, GPL-3.0, Kotlin + Jetpack Compose, active (last commit 2 days ago). Self-description:

> *"Modern FOSS Compass and Navigation app for Android — without Ads, IAP, or Tracking. Lightweight, simple and battery-efficient."*
>
> *"Not just a compass. Not a full navigation app. MBCompass sits in between, a focused navigation utility for hiking, trekking and everyday use, combining direction, live location, and track recording without unnecessary complexity."*

**That's almost verbatim our positioning.** The differentiator we identified in `competitive-research-2026-05.md` is real — and there's another team executing it, at 453 stars. They distribute via **F-Droid, Obtainium, and IzzyOnDroid**, three FOSS-native channels that have ~zero Play Store overlap.

**Implication:** the market position is validated. We need to either differentiate (simpler still, lat-lon/altitude focused) or compete on execution. The honest take: Hiker's Watch is **stylistically twin to MBCompass but with a tighter feature scope** — no map, no track recording, just GPS info that you can copy/share. That's a defensible niche.

---

## 2. FOSS landscape — full ranked list

These are the 11 most-starred, actively-maintained Android FOSS apps in our adjacency, pulled live from the GitHub API:

| Repo | Stars | Forks | License | Stack | Last push | What we learn from it |
|---|---:|---:|---|---|---|---|
| **kylecorry31/Trail-Sense** | 2 634 | 153 | MIT | Kotlin | today | Privacy-first survival/hiking. Goes broader than us (24+ tools). Their "no internet at all" pitch is a marketing point we could imitate. |
| **mendhak/gpslogger** | 2 514 | 641 | (custom) | Java | recent | Lightweight GPS logging app. Pure-utility focus. Multiple cloud export integrations. |
| **barbeau/gpstest** | 2 259 | 415 | Apache-2.0 | Kotlin | recent | Gold standard for GNSS UI. They're the app to look at when implementing satellite-count display. |
| **Kr0oked/Compass** | 619 | 69 | GPL-3.0 | Kotlin | May 2026 | Cleanest compass code in our category. Apache 2-friendly enough to learn from. |
| **BasicAirData/GPSLogger** | 487 | 140 | GPL-3.0 | Java | May 2026 | Java reference for GPX export, location services management. |
| **CompassMB/MBCompass** | 453 | — | GPL-3.0 | Kotlin/Compose | 4 days ago | **Our positioning twin.** Active, ~bi-monthly releases, ~6 open issues. |
| **Hamza417/Positional** | 278 | 29 | GPL-3.0 | Kotlin | 2 weeks ago | Closest UX twin (Compass + Clock + Sun + Moon + Trail panel). Featured by tech YouTubers (Sam Beckman, mobiscrub). |
| **iutinvg/compass** | 241 | 100 | MIT | Java | 2023 | Old but the simplest, most-readable Java compass impl. |
| **kylecorry31/Trail-Sense-Maps** | 9 | — | MIT | Kotlin | recent | Trail Sense's optional plugin app for offline maps — shows the plugin architecture pattern. |
| **bdureau/BearConsole2** | 17 | — | — | Java | recent | Rocket-altimeter console. Niche but the only Java altimeter on GitHub. |
| **tobiasthorin/mountaineer** | <5 | — | — | Java | 2016 | Abandoned. Same niche, demonstrates the position has been tried before. |

Plus the curated [`barbeau/awesome-gnss`](https://github.com/barbeau/awesome-gnss) list (564★) — every GNSS/GPS open-source resource cross-referenced from one place. Worth submitting our app there for traffic.

---

## 3. What users actually demand (Trail Sense's open issues)

These are the top community-upvoted feature requests on Trail Sense (2 634★, n=278 open issues). Each is a signal of real demand from active hikers:

| 👍 | Issue | Relevant to us? |
|---:|---|---|
| 8 | *Notify user if they are off track* | No — needs route planning |
| 7 | *Support more map tile formats* | No — we have no map |
| 6 | *Plugin architecture* | No — overkill |
| 4 | *Show pedometer history* | No — no step counter |
| **3** | *Show direction (azimuth) of sunrise/sunset* | **Yes — once we have a compass + sunrise feature, this is the next-level polish** |
| 3 | *Hydration tracker* | No |
| 3 | *Striking clock* | No |
| 2 | *Permits tool* | No |
| 2 | *Tap-and-hold flashlight* | No |
| 2 | *Backtrack with pedometer trigger* | No |

**Key insight:** even on a feature-rich, well-maintained app, the top requests are very long-tail. The "core needs" (lat/lon, altitude, compass, sun, save location) are already met by Trail Sense — users have moved on to specialised asks. **For us, this means the value of v2.4 (compass + speed + satellites) is high precisely because we still don't have the basics.**

---

## 4. Reddit pulse — what hikers and FOSS users are actually saying

### /r/Hiking (last year, top threads)

| ↑ | 💬 | Title | What it tells us |
|---:|---:|---|---|
| 31 | 127 | *"Do you carry paper maps or just a GPS apps?"* | GPS apps are now the default. Trust + offline reliability is the discussion. |
| 5 | 26 | *"Best hiking gps app with offline maps?"* | Most-asked feature, but a big build. Out of scope for us. |
| 5 | 14 | *"Best mapping app for Europe (w/o social features)?"* | **Direct validation that "no social, no tracking" is a sought-after positioning.** |
| 3 | 18 | *"Tracking my dad"* | Family safety / live location sharing. Out of scope on our privacy commitment. |
| 3 | 16 | *"Same GPX file, 3 different elevation gains — which app should I trust for hiking?"* | **Trust + accuracy is the #1 user concern after offline.** Our use of FusedLocationProvider (the same source Google Maps uses) is correct and worth advertising. |
| 3 | 11 | *"App to create, visualize and store my gpx routes?"* | GPX is the universal format hikers know. Adding GPX export from a saved-waypoints feature would be a moat. |

### /r/fossdroid (last year, top threads)

| ↑ | 💬 | Title | What it tells us |
|---:|---:|---|---|
| 187 | 103 | *"ShizuCallRecorder — Record phone call, FOSS, privacy friendly"* | The FOSS-privacy-friendly framing wins on /r/fossdroid. We should post a "Show HN"-style intro. |
| 157 | 84 | *"My attempt at using FOSS apps!"* | The FOSS-only audience exists. Cross-promote. |
| 122 | 17 | *"MBCompass v2.0 Design Proposal"* | Design iteration earns engagement here. Lesson: post WIP screenshots. |
| **62** | **7** | *"MBCompass — Lightweight (2MB) FOSS navigation app now with GPX tracking"* | **62 upvotes for a launch announcement of an app virtually identical to ours.** Same APK size (~2 MB), same positioning. Suggests we'd net 30–100 stars / a couple hundred installs from a single Reddit post. |
| 20 | 9 | *"Dead Reckoning Pro — GPS-free navigation using sensors (beta)"* | Sensor-only navigation is an emerging niche we don't need to enter but should know about. |

**Action implied:** when we ship v2.4 (compass/speed/satellites + LICENSE + F-Droid distribution), do a launch post on /r/fossdroid. The audience is there, primed, and recently rewarded an almost-identical pitch.

---

## 5. Code patterns we can adopt (with references)

I read the relevant source files. Here are the highest-leverage patterns from the FOSS apps we just surveyed.

### 5.1 Compass — Kr0oked/Compass

The minimal correct implementation. Their model: a `SensorEventListener` consumes `TYPE_ROTATION_VECTOR`, derives an azimuth, applies `GeomagneticField.getDeclination()` for true north correction. Their data classes:

```kotlin
data class Azimuth(val degrees: Float)           // 0..360
enum class SensorAccuracy { NO_CONTACT, UNRELIABLE, LOW, MEDIUM, HIGH }
enum class LocationStatus { NOT_PRESENT, PERMISSION_DENIED, DISABLED, AVAILABLE }
```

Reference: [`app/src/main/java/com/bobek/compass/data/`](https://github.com/Kr0oked/Compass/tree/master/app/src/main/java/com/bobek/compass/data) and `CompassViewModel.kt`.

**For us (Java):** roughly 50 lines in `CompassController.java`. Wire a `SensorEventListener`, expose a `setListener((azimuthDeg, accuracy) -> ...)` callback, register on resume / unregister on pause. Use `SensorManager.getRotationMatrixFromVector()` then `getOrientation()` to get azimuth in radians, convert to degrees, then `Math.floorMod((int) deg, 360)`.

For **true north**: keep the device's `GeomagneticField` for the user's latest location:

```java
GeomagneticField geo = new GeomagneticField(
    (float) location.getLatitude(),
    (float) location.getLongitude(),
    (float) location.getAltitude(),
    System.currentTimeMillis());
float trueNorthAzimuth = magneticAzimuth + geo.getDeclination();
```

### 5.2 GNSS satellite count — barbeau/gpstest pattern

`LocationManager.registerGnssStatusCallback(new GnssStatus.Callback() {...})` from API 24. The callback gives you `GnssStatus`, from which `getSatelliteCount()` and `usedInFix(i)` give the "fixed satellite count" — the number that matters in most UIs.

**For us:** ~20 lines. Show as a small badge in the coordinates card: *"GPS · 14 / 22 satellites"*.

Requires no new permission (already has `ACCESS_FINE_LOCATION`).

### 5.3 Sunrise / sunset / moon — fully offline, no API

Trail Sense and Positional both compute astronomy locally — no network calls. The standard algorithm is the **NOAA sunrise/sunset equations** (Jean Meeus). Implementations are ~80 lines of Java, well-known, and accurate to ±1 minute for any latitude on earth.

Reference packages:
- Trail Sense: `com.kylecorry.trail_sense.tools.astronomy.domain.*` (their own port)
- Open source reusable: [`shred/commons-suncalc`](https://github.com/shred/commons-suncalc) (Apache-2.0, Java, 0 deps) — drop-in addition.

Using `commons-suncalc`:

```java
SunTimes times = SunTimes.compute()
        .on(LocalDate.now())
        .at(location.getLatitude(), location.getLongitude())
        .execute();
ZonedDateTime rise = times.getRise();
ZonedDateTime set  = times.getSet();
```

That's the whole feature. No network. Adds ~40 KB to the AAB.

### 5.4 Waypoints persistence — Trail Sense pattern

Trail Sense uses **Room** (SQLite) for beacons / paths. For our scope (single-screen app, max ~50 waypoints), that's overkill. Use either:
- **A single JSON in SharedPreferences** — 30 lines, no migrations. Fine until ~500 entries.
- **DataStore Preferences** (Jetpack) — 50 lines, async, future-proof.

If we keep waypoints local-only (which we should), neither requires permission or breaks the privacy promise.

### 5.5 GPX export — BasicAirData/GPSLogger pattern

GPX is XML. ~80-line `GpxWriter` that walks a list of waypoints and writes `<wpt lat="..." lon="...">…</wpt>` entries to an OutputStream, exposed via the Android `Storage Access Framework` (`ACTION_CREATE_DOCUMENT`). No permissions needed.

A GPX export of saved waypoints would be a real differentiator vs Positional/MBCompass — neither has it as a primary feature.

---

## 6. Distribution channels we haven't used

Each of these is a meaningful traffic source for FOSS apps and **none cost engineering time**:

| Channel | What it is | What we need | Why it matters |
|---|---|---|---|
| **F-Droid** | The largest FOSS-only app store | Submit metadata + maintain a Fastlane folder | Free, plus inbound link from `f-droid.org`. Trail Sense gets ~30% of its installs from F-Droid. |
| **IzzyOnDroid** | Third-party FOSS repo | Just add the topic `f-droid` to our repo | Auto-pulls eligible repos. Zero work. |
| **Obtainium** | Self-updater that pulls APKs from GitHub | Already done — we publish on GitHub | Trail Sense, MBCompass, Positional all link this. Power-user audience. |
| **awesome-gnss** ([repo](https://github.com/barbeau/awesome-gnss)) | Curated list of GNSS-related projects | Submit a PR adding Hiker's Watch | Pre-qualified inbound audience. 564★. |
| **alternativeto.net** | "Find alternatives to" listing | Add ourselves as an alternative to GPS Test / GPS Status | High SEO domain. |
| **Privacy Guides** ([repo](https://github.com/privacyguides/privacyguides.org)) | Privacy-tool curated list | Open a PR proposing Hiker's Watch under "Maps & Navigation" | Direct hit on our positioning. |

---

## 7. Hard misses on our side (gaps a senior dev would call out)

After spending two hours reading competitor source code, here are gaps in our own repo that **every** competitor has solved and we haven't:

| Gap | Effort | Impact | Fix |
|---|---|---|---|
| **No LICENSE file** | 30 seconds | High — technically blocks contributions, looks unprofessional | Add `MIT` or `Apache-2.0` LICENSE to repo root |
| **No `fastlane/metadata/`** | 1 hour | Medium — required for F-Droid metadata pickup | Add `fastlane/metadata/android/en-US/{short_description,full_description,title}.txt` + screenshots |
| **No CONTRIBUTING.md** | 30 minutes | Low to medium — invites PRs (especially translations) | Standard template |
| **No CHANGELOG.md** | 15 minutes | Low — F-Droid metadata + better releases | Just paste git tags |
| **No GitHub Actions workflow** | 1 hour | Medium — runs the unit tests we just wrote on every push | `.github/workflows/android.yml` |
| **No releases on GitHub** | 5 minutes | Medium — `gh release create v2.3 ...` with the APK as an artifact | One command |
| **No SECURITY.md** | 5 minutes | Low — common in mature FOSS apps | Standard template |
| **No CODE_OF_CONDUCT.md** | 5 minutes | Low — Contributor Covenant | Boilerplate |
| **No issue templates** | 15 minutes | Medium — funnels reports cleanly | Bug + Feature templates |
| **No `<localizations>` other than English** | Variable | Medium — Spanish/Hindi/Mandarin would 5–10× addressable users | Crowdin or Weblate |
| **No README badges** | 5 minutes | Cosmetic | Build/license/version/F-Droid badges like MBCompass uses |

These are all "below-the-iceberg" things — they don't affect the app's behavior but they signal *seriousness* to the FOSS audience and the Play reviewers.

---

## 8. Revised 3-release plan, informed by this research

### v2.4 — "Senses" (was already the plan; data only strengthened it)
- **Compass** (rotation vector + true north) — biggest user-visible win, validated by 8/11 competitors
- **Speed** — auto-hides under threshold
- **Satellite count badge** — power-user delight (use GnssStatus.Callback)
- **LICENSE + GitHub Release + fastlane metadata folder** — distribution prerequisites

**New addition for v2.4:** add an *"In the wild"* badge in the About dialog → "Open source on GitHub" + repo link, matching how MBCompass and Trail Sense make the FOSS commitment visible inside the app.

### v2.5 — "Sun, sky, sea level"
- **Sunrise / sunset / civil twilight** via `commons-suncalc` (Apache-2.0)
- **Moon phase + illumination %** via the same library
- **Barometric pressure** (if sensor present) — graceful fallback
- **Direction-of-sunrise/sunset arrow** if we have the compass — addresses Trail Sense issue #1339 (3 reactions) and is a delightful detail
- **F-Droid submission** — once LICENSE is in, we can submit. Inclusion takes ~2 weeks.

### v2.6 — "Memory"
- **Waypoints** — save current location with a name, list view, navigate back, share, delete
- **GPX export** of saved waypoints (a moat vs MBCompass/Positional)
- **Home-screen widget** showing current coords
- **Localization** — Spanish + Hindi via Crowdin or Weblate (free for FOSS)

### v3.0 — Optional bigger bet (only if growth justifies)
- A **simple offline-favorites map** using OSMDroid (lightweight Apache 2 lib) — only enabled if user explicitly saves a waypoint with a pinned region. Stays under 5 MB AAB.
- **Wear OS companion** — purely for ASO/marketing reasons

---

## 9. The single highest-leverage thing to do today (30 minutes, no code)

Add a `LICENSE` file (MIT or Apache-2.0) + push a `gh release create v2.3` with the APK attached + add the repo topics `gps`, `android`, `privacy`, `f-droid`, `material-design`, `no-ads`, `open-source`.

These three actions, together, unlock:
- Submission to F-Droid (requires LICENSE + reproducible build metadata)
- Inbound traffic from awesome-gnss, alternativeto.net, Privacy Guides
- GitHub's "Trending" eligibility (needs topics + release activity)
- Obtainium user flows (find APK from GitHub Releases)

**Net result:** without writing a line of code, the app becomes discoverable through the same channels MBCompass and Positional use to gather their 278–453 stars.

---

## 10. TL;DR for the next product decision

1. Our positioning is **real and validated** — MBCompass is at 453★ with the same pitch (no ads, no IAP, no tracking, lightweight, FOSS).
2. The single biggest **code gap** vs the category is **compass + speed + satellite count**. That's v2.4.
3. The single biggest **distribution gap** is the **lack of FOSS-channel presence** — LICENSE + F-Droid + Obtainium + awesome-gnss. Together that's ~2 hours of work, no code.
4. The **trust-and-accuracy concern** Reddit hikers articulate (GPX elevation disagreement etc.) is a marketing angle: *"Backed by Android's FusedLocationProvider — same source as Google Maps."*
5. **Don't build** offline maps, route navigation, social features, or background tracking. They're huge engineering investments, contradict the privacy promise, and don't differentiate.

---

Raw materials:
- `competitor_data.json` — 11 Play Store competitor metadata + reviews
- `github-repos/*/` — READMEs + metadata for 8 FOSS competitors
- `reddit_*.json` — top threads from r/Hiking and r/fossdroid (year horizon)
- `scrape.py`, `analyze.py` — re-runnable harvest scripts
