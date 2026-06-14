# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Compile Desktop target
./gradlew :composeApp:compileKotlinDesktop

# Run Desktop app (requires JBR 17 — see note below)
./gradlew :composeApp:run

# Package Desktop as Windows installer (.msi)
./gradlew :composeApp:packageMsi

# Package Desktop as distributable directory (no installer)
./gradlew :composeApp:createDistributable

# Build Android APK (requires Android SDK)
./gradlew :composeApp:assembleDebug

# Run all tests
./gradlew :shared:commonTest
./gradlew :composeApp:commonTest

# Run a specific test class
./gradlew :shared:commonTest --tests "*.ExportMarkdownUseCaseTest"
./gradlew :composeApp:commonTest --tests "*.HomeViewModelTest"

# Regenerate SQLDelight code after schema changes
./gradlew :shared:generateCommonMainNoteAppDatabaseInterface
```

### JDK Requirement

Must use **JetBrains Runtime (JBR) 17** — standard MS JDK or Oracle JDK will crash at runtime with `UnsatisfiedLinkError` in Skiko's native bindings. Download JBR via IntelliJ IDEA → Project Structure → SDKs → Download JDK → Vendor: JetBrains Runtime, Version: 17. Then set in `gradle.properties`:

```properties
org.gradle.java.home=C:/Users/<user>/.jdks/jbr-17.x.x.x
```

## Architecture

Two Gradle modules:

- **`shared/`** — pure KMP library. Contains domain models, repository interfaces, use cases, SQLDelight DB, and Koin DI module. Zero UI or Android Context dependencies.
- **`composeApp/`** — Compose Multiplatform app. Contains all UI (Screens, ViewModels, Theme, Navigation). Depends on `shared` via interfaces only.

### Layer Structure

```
composeApp/commonMain
  ui/screen/home/   HomeScreen + HomeViewModel + NoteCard
  ui/screen/edit/   EditScreen + EditViewModel + RichEditorToolbar
  ui/navigation/    AppNavigation (NavHost)
  ui/theme/         AppTheme, ThemeMode, NoteColors
  di/               AppModule (Koin ViewModel factories)
  App.kt            Root @Composable

shared/commonMain
  domain/model/     Note (data class)
  domain/repository NoteRepository (interface)
  domain/usecase/   GetAllNotes, SaveNote, DeleteNote, ExportMarkdown
  data/local/       LocalNoteRepository, DatabaseDriverFactory (expect/actual)
  di/               SharedModule (Koin)
```

### Key Patterns

**Koin DI:** `DatabaseDriverFactory` is registered per-platform in each entry point (`MainActivity.kt` for Android, `main.kt` for Desktop) as an inline `module { single { DatabaseDriverFactory(...) } }`. `SharedModule` and `AppModule` are then added alongside it.

**ViewModel registration (Koin 4.0):** Use `viewModelOf(::HomeViewModel)` and `viewModel { (noteId: String?) -> EditViewModel(...) }` in `AppModule`. `factory { }` does not work for ViewModels in Koin 4.0.

**EditViewModel receives noteId as Koin parameter** via `parametersOf(noteId)` at the call site.

**Note saving:** `SaveNoteUseCase` sets timestamps — callers pass `createdAt = 0L` for new notes (triggers `createdAt = now`), and the original `createdAt` for existing notes. `updatedAt` is always set to `now` by the use case.

**HTML ↔ Markdown:** The editor stores `htmlContent` as source of truth. `markdownContent` is derived on every save via `ExportMarkdownUseCase` (regex-based HTML→MD conversion). On mode toggle, `EditScreen` syncs `richTextState` ↔ `markdownSource` via two `LaunchedEffect` blocks.

**Theme:** `ThemeMode.DARK` enum value and `AppTheme` wiring exist but the dark color scheme is a stub (returns Material3 defaults). `ThemeMode` preference storage via DataStore is wired in `libs.versions.toml` but not yet implemented in UI.

### SQLDelight

Schema at `shared/src/commonMain/sqldelight/com/noteapp/shared/database/NoteApp.sq`. Generated code lands in `shared/build/generated/sqldelight/`. The generated class is `NoteAppDatabase`; queries are accessed via `database.noteAppQueries`.

Desktop driver (`JdbcSqliteDriver`) checks if `NoteEntity` table already exists before calling `Schema.create()` — calling it unconditionally crashes on second launch with "table already exists".

### Navigation

Route pattern: `"home"` and `"edit/{noteId}"`. Passing `"edit/new"` creates a new note — `AppNavigation` maps the raw string `"new"` to `null` before passing to `EditScreen`. Route arguments are read via `backStackEntry.arguments?.read { getStringOrNull("noteId") }` (KMP-compatible API from `androidx.savedstate`).

## Key Dependency Versions

| Dependency | Version | Notes |
|---|---|---|
| Kotlin | 2.1.0 | |
| Compose Multiplatform | 1.8.0 | Requires JBR 17 for Desktop runtime |
| SQLDelight | 2.0.2 | |
| Koin | 4.0.0 | Koin 3.x API is incompatible; use `viewModelOf`/`viewModel {}` not `factory {}` |
| navigation-compose | 2.9.0-beta01 | JetBrains fork, not Google's |
| lifecycle | 2.9.0-beta01 | Must match navigation version |
| richeditor-compose | 1.0.0-rc09 | RC — API may change |
| markdown-renderer | 0.30.0 | 0.27.x pulls in Compose 1.8.2 which conflicts with CMP 1.7.x |

## Known Issues

- `proguard-rules.pro` is empty — Koin DI and SQLDelight classes will be stripped in release builds. Add keep rules before producing a release APK.
- `list active state` in `RichEditorToolbar`: the "• 列表" and "1. 列表" buttons always show as inactive. `RichTextState.isUnorderedList` / `isOrderedList` can be used to fix this.
- `SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder"` on Desktop run is harmless — no logging backend is configured for Koin.
- `packageMsi` requires downloading WiX toolset from GitHub; fails in restricted network environments. Use `createDistributable` instead for a portable build.

## Dependency Version Compatibility Notes

Upgrading Compose Multiplatform requires coordinating several versions simultaneously:

- CMP version determines the Skiko version bundled — mixing Skiko JVM JAR and native DLL versions causes `UnsatisfiedLinkError` at runtime.
- `markdown-renderer` 0.27.x transitively pulls `foundation-layout 1.8.2` which conflicts with CMP 1.7.x. Use 0.30.0+ alongside CMP 1.8.0+.
- CMP 1.8.0+ does not include Material Icons by default — add `compose.materialIconsExtended` explicitly to `commonMain.dependencies`.
- `navigation-compose` and `lifecycle` versions must be kept in sync (both on `2.9.0-beta01` for CMP 1.8.0).
