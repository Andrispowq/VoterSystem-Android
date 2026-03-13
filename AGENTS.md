# Repository Guidelines

## Project Structure & Module Organization
`VoterSystem` is a single-module Android app. The main code lives in `app/src/main/java/com/akmeczo/votersystem`, with Compose UI entry points such as `MainActivity.kt` and `LoginScreen.kt`. Networking code is grouped under `app/src/main/java/com/akmeczo/votersystem/server`, with request and response DTOs in `server/requests` and `server/responses`.

Resources and app configuration are under `app/src/main/res` and `app/src/main/AndroidManifest.xml`. Local JVM tests live in `app/src/test`, and device or emulator tests live in `app/src/androidTest`.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root:

- `.\gradlew.bat assembleDebug` builds the debug APK.
- `.\gradlew.bat installDebug` installs the app on a connected device or running emulator.
- `.\gradlew.bat testDebugUnitTest` runs local unit tests from `app/src/test`.
- `.\gradlew.bat connectedDebugAndroidTest` runs instrumentation tests from `app/src/androidTest`.
- `.\gradlew.bat lint` runs Android lint checks.

Open the project in Android Studio for Compose previews and emulator-driven development.

## Coding Style & Naming Conventions
Follow standard Kotlin style with 4-space indentation and trailing commas only where the IDE inserts them consistently. Keep package names lowercase, use `PascalCase` for classes and composables, and `camelCase` for functions, properties, and local variables.

Match existing file organization: keep UI in the root package, server code in `server`, and DTOs in `server/requests` or `server/responses`. Prefer small composables and explicit request/response types such as `UserLoginRequest` and `TokensDto`.

## Testing Guidelines
Add fast logic tests to `app/src/test` and Android-dependent tests to `app/src/androidTest`. Name test files after the class under test, for example `ServerTest.kt`, and use descriptive test names such as `login_returnsTokens_whenCredentialsAreValid`.

There is no coverage gate configured yet, but new behavior should include tests when practical, especially for request handling and validation logic.

## Commit & Pull Request Guidelines
Recent commits use short, imperative summaries like `Login screen added to get started with compose` and `Created the dev branch + server object`. Keep commit messages concise, explain the user-visible or architectural change, and avoid mixing unrelated edits.

Pull requests should include a brief summary, testing notes, linked issues when applicable, and screenshots or recordings for UI changes.
