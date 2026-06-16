# Trip Planner — Android

Native Android port of the Trip Planning / Trip Optimizer web app
(`harryakbar/monorepo-frontend → apps/tripplanningapp`), built with **Kotlin +
Jetpack Compose** (Material 3). Same visual style, with polished native
animations and transitions.

Planning and scope live in the issues, tracked under the epic
[#1](https://github.com/harryakbar/trip-planning-android/issues/1).

## Requirements

- JDK 21
- Android SDK (compileSdk 35, build-tools 35.0.0)
- minSdk 26

## Build & run locally

```bash
./gradlew assembleDebug        # build a debug APK
./gradlew installDebug         # install on a connected device/emulator
./gradlew testDebugUnitTest    # run unit tests
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Google Maps API key (for the itinerary map)

The interactive map (itinerary → **Map**) needs a Google Maps **Android** API
key. The project builds fine without one — the map just renders blank — so the
key is optional for everything else.

- **Local builds:** add the key to `local.properties` (which is git-ignored):

  ```properties
  MAPS_API_KEY=YOUR_ANDROID_MAPS_API_KEY
  ```

  Alternatively pass `-PMAPS_API_KEY=…` or set the `MAPS_API_KEY` env var.

- **CI / release APKs:** add a repository secret named **`MAPS_API_KEY`**
  (Settings → Secrets and variables → Actions). The CI and Release workflows
  pass it through to the build automatically.

Enable the **Maps SDK for Android** for the key in Google Cloud, and restrict it
to the app's package name (`com.tripplanner.android`) + signing certificate. The
key is injected into the manifest via `manifestPlaceholders` and is never
committed.

## CI/CD — getting a testable APK

### On every push / merge to `main`
The **CI** workflow (`.github/workflows/ci.yml`) runs unit tests and builds a
debug APK. You can grab it two ways:

1. **Latest pre-release (easiest):** a `latest` GitHub pre-release is refreshed
   on every push to `main` — always one click away at
   **Releases → "Latest build (main)" → `app-debug.apk`**.
2. **Run artifact:** open the CI run → **Artifacts** → `app-debug-<sha>`
   (kept 14 days).

> The default branch here is `main`. The workflows also trigger on `master` if
> you rename it.

### Tagged releases
Push a version tag and the **Release** workflow
(`.github/workflows/release.yml`) builds a release APK and attaches it to a new
GitHub Release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

Download from **Releases → `v0.1.0` → `trip-planner-v0.1.0.apk`**.

## Signing

For easy testing, the `release` build type is currently signed with the **debug
key**, so both the `latest` pre-release and tagged releases install directly on
a device. Before publishing to the Play Store, wire a real upload/release
keystore (e.g. via repository secrets + a `signingConfig`) and switch the
`release` build type to use it.
