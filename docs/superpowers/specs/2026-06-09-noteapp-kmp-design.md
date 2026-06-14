# NoteApp KMP — Design Spec

**Date:** 2026-06-09
**Platform:** Kotlin Multiplatform — Desktop (Windows/macOS/Linux) + Android
**Stage:** Demo / MVP

---

## 1. Overview

A cross-platform sticky-note application built with Kotlin Multiplatform and Compose Multiplatform. Users can create, edit, and delete notes with rich text formatting and Markdown support. Data is stored locally with the architecture prepared for future cloud sync.

---

## 2. Project Structure

```
NoteApp/
├── composeApp/
│   └── src/
│       ├── commonMain/      # Shared UI: Screens, Components, Theme
│       ├── androidMain/     # Android entry point (MainActivity)
│       └── desktopMain/     # Desktop entry point (main.kt)
├── shared/
│   └── src/
│       ├── commonMain/      # Data models, Repository interfaces, UseCases
│       ├── androidMain/     # SQLDelight AndroidDriver
│       └── desktopMain/     # SQLDelight JvmDriver
└── build.gradle.kts
```

**Module responsibilities:**
- `shared` — all business logic, data models, repository interfaces, local DB implementation. Zero UI dependencies.
- `composeApp/commonMain` — all Compose UI (screens and components). Depends on `shared` only through interfaces.
- Platform-specific directories contain only driver/entry-point initialization.

---

## 3. Data Model

```kotlin
data class Note(
    val id: String,              // UUID, generated on creation
    val title: String,
    val htmlContent: String,     // Rich text stored as HTML (source of truth for editor)
    val markdownContent: String, // Markdown export, derived from htmlContent on save
    val colorHex: String,        // Card background color (hex string)
    val createdAt: Long,         // Unix timestamp (ms)
    val updatedAt: Long          // Unix timestamp (ms)
)
```

**Storage:** SQLDelight 2.x with a single `notes` table mirroring the model above.

---

## 4. Repository Layer

```kotlin
interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
    // Reserved for future sync:
    // suspend fun syncNotes()
}
```

`LocalNoteRepository` implements this interface using SQLDelight.
Future `SyncedNoteRepository` will implement the same interface with cloud sync logic — callers need no changes.

---

## 5. Use Cases

| UseCase | Input | Output |
|---------|-------|--------|
| `GetAllNotesUseCase` | — | `Flow<List<Note>>` |
| `SaveNoteUseCase` | `Note` | `Unit` |
| `DeleteNoteUseCase` | `id: String` | `Unit` |
| `ExportMarkdownUseCase` | `htmlContent: String` | `String` (MD text) |

---

## 6. UI Screens

### HomeScreen
- Staggered/grid card layout (2 columns on mobile, 3–4 on desktop)
- Each `NoteCard` shows: title, content preview (plain text, truncated), background color
- Top bar: app title + search field + "New Note" FAB
- Long-press or right-click on card → delete option
- Tapping a card navigates to `EditScreen`

### EditScreen
- Full-screen editor
- Top bar: back button, note title field, toggle button (WYSIWYG ↔ MD source)
- **WYSIWYG mode:** `RichTextEditor` (richeditor-compose) with formatting toolbar
  - Toolbar actions: Bold, Italic, H1/H2, Unordered list, Ordered list, Inline code
- **MD source mode:** Plain text field showing raw Markdown; content synced back to HTML on mode switch
- Auto-save on navigation back (debounced 500ms while typing)
- Color picker strip at bottom: select card color from preset palette

---

## 7. Theme System

```kotlin
enum class ThemeMode { COLORFUL, DARK }

@Composable
fun AppTheme(mode: ThemeMode = ThemeMode.COLORFUL, content: @Composable () -> Unit)
```

**COLORFUL mode (implemented):**
- White/warm backgrounds, colorful card palette
- Card color palette: `#FEF3C7`, `#D1FAE5`, `#FCE7F3`, `#DBEAFE`, `#EDE9FE`, `#FEE2E2`
- Accent color: Amber `#F59E0B`

**DARK mode (interface reserved, not implemented in Demo):**
- `AppTheme(mode = ThemeMode.DARK)` call site is wired up
- Actual dark color scheme returns same as COLORFUL until implemented
- `ThemeMode` preference persisted in `DataStore` (key: `theme_mode`)

---

## 8. Navigation

Using `compose-navigation`. Two destinations:

```
Home  ──(tap card / FAB)──►  Edit(noteId: String?)
        ◄──(back)────────────
```

`noteId = null` → create new note (auto-generate UUID, pick random color).

---

## 9. Dependencies

| Purpose | Library | Version |
|---------|---------|---------|
| UI | Compose Multiplatform | 1.7.x |
| Database | SQLDelight | 2.0.x |
| Rich text editor | richeditor-compose | 1.0.x |
| Markdown rendering | multiplatform-markdown-renderer | 0.27.x |
| Navigation | compose-navigation | 2.8.x |
| Dependency injection | Koin KMP | 3.6.x |
| Preferences | DataStore (KMP) | 1.1.x |
| UUID | kotlinx-uuid | — (use `kotlin.uuid.Uuid` in Kotlin 2.x) |

---

## 10. Out of Scope (Demo)

- Cloud sync (interface reserved, not implemented)
- Dark mode rendering (enum and call site reserved, not implemented)
- Note tags / categories
- Image attachments
- Search (UI placeholder only, no backend logic)
- Export to file

---

## 11. Success Criteria

- [ ] App launches on Desktop (JVM) and Android
- [ ] Create, edit, delete notes
- [ ] Rich text formatting (bold, italic, headings, lists) works in WYSIWYG mode
- [ ] Toggle to Markdown source view and back, content preserved
- [ ] Notes displayed as colored cards in grid layout
- [ ] Notes persisted in local SQLite — survive app restart
