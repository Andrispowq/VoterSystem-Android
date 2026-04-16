# Repository Guidelines

## Project Structure & Module Organization
`VoterSystem` is a single-module Android app in `app/`. Kotlin sources live in `app/src/main/java/com/akmeczo/votersystem`, with UI split across `ui/auth`, `ui/main`, and `ui/navigation`. Network and session code lives in `server`, with DTOs under `server/requests` and `server/responses`.

Android resources are in `app/src/main/res`, and app configuration is in `app/src/main/AndroidManifest.xml`. Local JVM tests belong in `app/src/test`, while emulator or device tests go in `app/src/androidTest`.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root:

- `./gradlew assembleDebug` builds the debug APK.
- `./gradlew installDebug` installs the app on a connected device or running emulator.
- `./gradlew testDebugUnitTest` runs local JVM tests.
- `./gradlew connectedDebugAndroidTest` runs instrumentation tests.
- `./gradlew lint` runs Android lint checks.

Use `gradlew.bat` on Windows. Open the project in Android Studio for Compose previews, layout inspection, and emulator-driven development.

## Coding Style & Naming Conventions
Follow standard Kotlin style with 4-space indentation and trailing commas only where the IDE inserts them consistently. Keep package names lowercase, use `PascalCase` for classes and composables, and `camelCase` for functions, properties, and local variables.

Match the existing package layout: UI stays under `ui/*`, service code under `server`, and DTOs in `server/requests` or `server/responses`. Prefer small composables, explicit model names like `UserLoginRequest`, and focused files such as `VotingDateTimeFormatter.kt`.

## Testing Guidelines
Add fast logic tests to `app/src/test` and Android-dependent tests to `app/src/androidTest`. Name test files after the unit under test, for example `VotingDateTimeFormatterTest.kt`, and use descriptive names such as `login_returnsTokens_whenCredentialsAreValid`.

There is no coverage gate configured yet, but new behavior should include tests when practical, especially for request handling and validation logic.

## Commit & Pull Request Guidelines
Recent commits use short, imperative summaries like `Login screen added to get started with compose` and `Created the dev branch + server object`. Keep commit messages concise, explain the user-visible or architectural change, and avoid mixing unrelated edits.

Pull requests should include a brief summary, testing notes, linked issues when applicable, and screenshots or recordings for UI changes.

## Configuration Tips
Do not commit secrets or machine-specific settings. Treat `local.properties` as local-only, and keep API endpoints, tokens, and debug-only values out of source control unless they are intentionally shared defaults.
