# Release and signing

Version 1.4.1 / code 6 uses the `release` Gradle build type with `debuggable=false` and a dedicated 3072-bit RSA signing key. Shrinking remains disabled. APKs are uploaded as GitHub Release assets, not committed into source history.

## Local build

1. Configure JDK 21 and Android SDK 35.
2. Set all four `SPATIAL_RELEASE_*` variables described in the README. All-or-none validation prevents partial signing configuration. Keep the private keystore and passwords outside the checkout and do not enable shell tracing.
3. Run `./scripts/build-release.sh`.
4. Check `apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk`, verify the application ID/version and confirm the release manifest is not debuggable.
5. Run release instrumentation and inspect real compositor screenshots/video before publishing. Keep QA statements tied to the actual variant tested.
6. Compute SHA-256, copy the APK to `SpatialHeadlines-VERSION-release.apk`, create a matching Git tag and GitHub Release, then upload the APK and `SHA256SUMS.txt`.

Without credentials, `./gradlew :app:assembleRelease` intentionally produces an unsigned APK. A private key is not distributed with source or release assets. The maintainer must securely back up the existing release key and credentials; generating a replacement key will change Android's upgrade identity.

## Runtime QA

Build the test APK against Release using the **same** signing environment:

```sh
./gradlew :app:assembleRelease :app:assembleReleaseAndroidTest -PtestBuildType=release
pico-cli app install app/build/outputs/apk/release/app-release.apk --device EMULATOR_SERIAL --replace
pico-cli app install app/build/outputs/apk/androidTest/release/app-release-androidTest.apk --device EMULATOR_SERIAL --replace --test
adb -s EMULATOR_SERIAL shell am instrument -w -r -e class 'com.headline.news.SettingsWindowTest#manualScrollToolbarRefreshAndPositionRetention' com.headline.news.test/androidx.test.runner.AndroidJUnitRunner
```

Run each case individually; CLI shell commands time out after 30 seconds. Available cases include `twoModesCountsSettingsAndRepeatedTransitions`, `reachingEndRefreshesWithoutMovingTheList`, `LiveNewsTest#allSourcesReturnRealPublisherArticles`, `LiveNewsTest#visibleHeadlineOpensItsExactArticle`, and `SettingsWindowTest#captureReviewState` with `-e scene all`.

## Debug → Release migration

The previous Debug build and the published Release have different signing certificates. Android rejects an in-place update (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`). Do not silently uninstall the user's app. Keep Debug installed until the user is ready to lose its local preferences/cache and install Release, or test Release on a clean emulator/device. Subsequent releases must reuse this publication's key.

## Repository hygiene

Commit app/Gradle source, portable docs, reviewed QA logs and media, and font licenses. Exclude build directories, local properties, tool caches, host/plugin configuration, signing files/passwords and unreviewed environment dumps. The project's context document is a curated handoff, not a wholesale export of tool logs or chat history.
