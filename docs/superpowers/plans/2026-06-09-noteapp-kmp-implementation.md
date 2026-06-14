# NoteApp KMP — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a cross-platform sticky-note app (Desktop + Android) with rich text editing, Markdown export, and local SQLite persistence.

**Architecture:** Two Gradle modules — `shared` (pure KMP: domain models, repository interfaces, use cases, SQLDelight DB) and `composeApp` (Compose Multiplatform UI shared across Android and Desktop). Platform-specific directories contain only driver/entry-point initialization. All business logic in `shared`.

**Tech Stack:** Kotlin 2.1.0 · Compose Multiplatform 1.7.3 · SQLDelight 2.0.2 · richeditor-compose 1.0.0-rc09 · multiplatform-markdown-renderer 0.27.0 · Koin 3.6.0 · Jetpack Navigation Compose (KMP) · DataStore 1.1.2

---

## File Map

```
NoteApp/
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── shared/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/com/noteapp/shared/
│       │   │   ├── domain/
│       │   │   │   ├── model/Note.kt
│       │   │   │   ├── repository/NoteRepository.kt
│       │   │   │   └── usecase/
│       │   │   │       ├── GetAllNotesUseCase.kt
│       │   │   │       ├── SaveNoteUseCase.kt
│       │   │   │       ├── DeleteNoteUseCase.kt
│       │   │   │       └── ExportMarkdownUseCase.kt
│       │   │   ├── data/
│       │   │   │   └── local/
│       │   │   │       ├── DatabaseDriverFactory.kt   (expect)
│       │   │   │       └── LocalNoteRepository.kt
│       │   │   └── di/
│       │   │       └── SharedModule.kt
│       │   └── sqldelight/com/noteapp/shared/database/
│       │       └── NoteApp.sq
│       ├── androidMain/kotlin/com/noteapp/shared/data/local/
│       │   └── AndroidDatabaseDriverFactory.kt        (actual)
│       ├── desktopMain/kotlin/com/noteapp/shared/data/local/
│       │   └── DesktopDatabaseDriverFactory.kt        (actual)
│       └── commonTest/kotlin/com/noteapp/shared/
│           ├── FakeNoteRepository.kt
│           └── domain/usecase/
│               ├── NoteUseCaseTest.kt
│               └── ExportMarkdownUseCaseTest.kt
└── composeApp/
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/com/noteapp/
        │   ├── App.kt
        │   ├── di/AppModule.kt
        │   └── ui/
        │       ├── theme/
        │       │   ├── ThemeMode.kt
        │       │   ├── NoteColors.kt
        │       │   └── AppTheme.kt
        │       ├── navigation/AppNavigation.kt
        │       └── screen/
        │           ├── home/
        │           │   ├── HomeViewModel.kt
        │           │   ├── NoteCard.kt
        │           │   └── HomeScreen.kt
        │           └── edit/
        │               ├── EditViewModel.kt
        │               ├── RichEditorToolbar.kt
        │               └── EditScreen.kt
        ├── androidMain/
        │   ├── kotlin/com/noteapp/MainActivity.kt
        │   └── AndroidManifest.xml
        ├── desktopMain/kotlin/com/noteapp/main.kt
        └── commonTest/kotlin/com/noteapp/ui/
            ├── FakeNoteRepository.kt
            ├── screen/home/HomeViewModelTest.kt
            └── screen/edit/EditViewModelTest.kt
```

---

## Task 1: Project Scaffolding

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `shared/build.gradle.kts`
- Create: `composeApp/build.gradle.kts`
- Create: `composeApp/src/androidMain/AndroidManifest.xml`

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "NoteApp"
include(":composeApp")
include(":shared")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

- [ ] **Step 2: Create `gradle/libs.versions.toml`**

```toml
[versions]
kotlin = "2.1.0"
agp = "8.7.3"
compose-multiplatform = "1.7.3"
sqldelight = "2.0.2"
koin = "3.6.0"
richeditor = "1.0.0-rc09"
markdown-renderer = "0.27.0"
navigation = "2.8.0-alpha10"
datastore = "1.1.2"
coroutines = "1.9.0"
lifecycle = "2.8.4"
datetime = "0.6.1"
android-compileSdk = "35"
android-minSdk = "24"
android-targetSdk = "35"

[libraries]
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-swing = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-swing", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }

koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }

sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-sqlite-driver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }

richeditor = { module = "com.mohamedrejeb.richeditor:richeditor-compose", version.ref = "richeditor" }
markdown-renderer = { module = "com.mikepenz:multiplatform-markdown-renderer-m3", version.ref = "markdown-renderer" }
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation" }
datastore-preferences = { module = "androidx.datastore:datastore-preferences-core", version.ref = "datastore" }
lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

- [ ] **Step 3: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
}
```

- [ ] **Step 4: Create `shared/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget()
    jvm("desktop")

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.noteapp.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}

sqldelight {
    databases {
        create("NoteAppDatabase") {
            packageName.set("com.noteapp.shared.database")
        }
    }
}
```

- [ ] **Step 5: Create `composeApp/build.gradle.kts`**

```kotlin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.richeditor)
            implementation(libs.markdown.renderer)
            implementation(libs.datastore.preferences)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":shared"))
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.koin.android)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.noteapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.noteapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
}

compose.desktop {
    application {
        mainClass = "com.noteapp.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NoteApp"
            packageVersion = "1.0.0"
        }
    }
}
```

- [ ] **Step 6: Create `composeApp/src/androidMain/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="NoteApp"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 7: Sync Gradle and confirm no errors**

Run: `./gradlew help`
Expected: `BUILD SUCCESSFUL`

---

## Task 2: Note Domain Model + Repository Interface

**Files:**
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/domain/model/Note.kt`
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/domain/repository/NoteRepository.kt`

- [ ] **Step 1: Create `Note.kt`**

```kotlin
package com.noteapp.shared.domain.model

data class Note(
    val id: String,
    val title: String,
    val htmlContent: String,
    val markdownContent: String,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long
)
```

- [ ] **Step 2: Create `NoteRepository.kt`**

```kotlin
package com.noteapp.shared.domain.repository

import com.noteapp.shared.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
    // Reserved for future sync:
    // suspend fun syncNotes()
}
```

- [ ] **Step 3: Commit**

```bash
git init
git add shared/src/commonMain/kotlin/com/noteapp/shared/domain/
git commit -m "feat: add Note domain model and NoteRepository interface"
```

---

## Task 3: SQLDelight Schema + Database Driver Factory

**Files:**
- Create: `shared/src/commonMain/sqldelight/com/noteapp/shared/database/NoteApp.sq`
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/data/local/DatabaseDriverFactory.kt`
- Create: `shared/src/androidMain/kotlin/com/noteapp/shared/data/local/AndroidDatabaseDriverFactory.kt`
- Create: `shared/src/desktopMain/kotlin/com/noteapp/shared/data/local/DesktopDatabaseDriverFactory.kt`

- [ ] **Step 1: Create `NoteApp.sq`**

```sql
CREATE TABLE NoteEntity (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    htmlContent TEXT NOT NULL,
    markdownContent TEXT NOT NULL,
    colorHex TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

getAllNotes:
SELECT * FROM NoteEntity ORDER BY updatedAt DESC;

getNoteById:
SELECT * FROM NoteEntity WHERE id = ?;

insertOrReplaceNote:
INSERT OR REPLACE INTO NoteEntity VALUES (?, ?, ?, ?, ?, ?, ?);

deleteNote:
DELETE FROM NoteEntity WHERE id = ?;
```

- [ ] **Step 2: Create `DatabaseDriverFactory.kt` (expect)**

```kotlin
package com.noteapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```

- [ ] **Step 3: Create `AndroidDatabaseDriverFactory.kt` (actual)**

```kotlin
package com.noteapp.shared.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.noteapp.shared.database.NoteAppDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(NoteAppDatabase.Schema, context, "noteapp.db")
}
```

- [ ] **Step 4: Create `DesktopDatabaseDriverFactory.kt` (actual)**

```kotlin
package com.noteapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.noteapp.shared.database.NoteAppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:noteapp.db")
        NoteAppDatabase.Schema.create(driver)
        return driver
    }
}
```

- [ ] **Step 5: Generate SQLDelight code**

Run: `./gradlew :shared:generateCommonMainNoteAppDatabaseInterface`
Expected: `BUILD SUCCESSFUL` and generated files under `shared/build/generated/`

- [ ] **Step 6: Commit**

```bash
git add shared/src/
git commit -m "feat: add SQLDelight schema and platform database driver factories"
```

---

## Task 4: LocalNoteRepository

**Files:**
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/data/local/LocalNoteRepository.kt`

- [ ] **Step 1: Create `LocalNoteRepository.kt`**

```kotlin
package com.noteapp.shared.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.noteapp.shared.database.NoteAppDatabase
import com.noteapp.shared.database.NoteEntity
import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalNoteRepository(private val database: NoteAppDatabase) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        database.noteAppQueries.getAllNotes()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toNote() } }

    override suspend fun getNoteById(id: String): Note? =
        withContext(Dispatchers.Default) {
            database.noteAppQueries.getNoteById(id).executeAsOneOrNull()?.toNote()
        }

    override suspend fun saveNote(note: Note) =
        withContext(Dispatchers.Default) {
            database.noteAppQueries.insertOrReplaceNote(
                id = note.id,
                title = note.title,
                htmlContent = note.htmlContent,
                markdownContent = note.markdownContent,
                colorHex = note.colorHex,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt
            )
        }

    override suspend fun deleteNote(id: String) =
        withContext(Dispatchers.Default) {
            database.noteAppQueries.deleteNote(id)
        }

    private fun NoteEntity.toNote() = Note(
        id = id,
        title = title,
        htmlContent = htmlContent,
        markdownContent = markdownContent,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/noteapp/shared/data/local/LocalNoteRepository.kt
git commit -m "feat: implement LocalNoteRepository with SQLDelight"
```

---

## Task 5: Use Cases with Tests

**Files:**
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/domain/usecase/GetAllNotesUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/domain/usecase/SaveNoteUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/domain/usecase/DeleteNoteUseCase.kt`
- Create: `shared/src/commonTest/kotlin/com/noteapp/shared/FakeNoteRepository.kt`
- Create: `shared/src/commonTest/kotlin/com/noteapp/shared/domain/usecase/NoteUseCaseTest.kt`

- [ ] **Step 1: Create `GetAllNotesUseCase.kt`**

```kotlin
package com.noteapp.shared.domain.usecase

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(private val repository: NoteRepository) {
    fun execute(): Flow<List<Note>> = repository.getAllNotes()
}
```

- [ ] **Step 2: Create `SaveNoteUseCase.kt`**

```kotlin
package com.noteapp.shared.domain.usecase

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.datetime.Clock

class SaveNoteUseCase(private val repository: NoteRepository) {
    suspend fun execute(note: Note) {
        val now = Clock.System.now().toEpochMilliseconds()
        val toSave = if (note.createdAt == 0L) {
            note.copy(createdAt = now, updatedAt = now)
        } else {
            note.copy(updatedAt = now)
        }
        repository.saveNote(toSave)
    }
}
```

- [ ] **Step 3: Create `DeleteNoteUseCase.kt`**

```kotlin
package com.noteapp.shared.domain.usecase

import com.noteapp.shared.domain.repository.NoteRepository

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend fun execute(id: String) = repository.deleteNote(id)
}
```

- [ ] **Step 4: Create `FakeNoteRepository.kt` (test helper)**

```kotlin
package com.noteapp.shared

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    private val flow = MutableStateFlow<List<Note>>(emptyList())

    override fun getAllNotes(): Flow<List<Note>> = flow

    override suspend fun getNoteById(id: String): Note? = notes.find { it.id == id }

    override suspend fun saveNote(note: Note) {
        notes.removeAll { it.id == note.id }
        notes.add(note)
        flow.value = notes.toList()
    }

    override suspend fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
        flow.value = notes.toList()
    }
}
```

- [ ] **Step 5: Write failing test**

Create `shared/src/commonTest/kotlin/com/noteapp/shared/domain/usecase/NoteUseCaseTest.kt`:

```kotlin
package com.noteapp.shared.domain.usecase

import com.noteapp.shared.FakeNoteRepository
import com.noteapp.shared.domain.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteUseCaseTest {

    private val repo = FakeNoteRepository()
    private val getAllNotes = GetAllNotesUseCase(repo)
    private val saveNote = SaveNoteUseCase(repo)
    private val deleteNote = DeleteNoteUseCase(repo)

    private fun sampleNote(id: String = "1") = Note(
        id = id, title = "Test", htmlContent = "<p>Hello</p>",
        markdownContent = "Hello", colorHex = "#FEF3C7",
        createdAt = 0L, updatedAt = 0L
    )

    @Test
    fun `saveNote sets createdAt and updatedAt when new`() = runTest {
        saveNote.execute(sampleNote())
        val notes = getAllNotes.execute().first()
        assertTrue(notes.first().createdAt > 0L)
        assertTrue(notes.first().updatedAt > 0L)
    }

    @Test
    fun `saveNote updates updatedAt but preserves createdAt when existing`() = runTest {
        val original = sampleNote().copy(createdAt = 1000L, updatedAt = 1000L)
        saveNote.execute(original)
        saveNote.execute(original.copy(title = "Updated"))
        val notes = getAllNotes.execute().first()
        assertEquals(1, notes.size)
        assertEquals(1000L, notes.first().createdAt)
        assertTrue(notes.first().updatedAt >= 1000L)
    }

    @Test
    fun `deleteNote removes note from repository`() = runTest {
        saveNote.execute(sampleNote("a"))
        saveNote.execute(sampleNote("b"))
        deleteNote.execute("a")
        val notes = getAllNotes.execute().first()
        assertEquals(1, notes.size)
        assertEquals("b", notes.first().id)
    }

    @Test
    fun `getAllNotes returns empty list initially`() = runTest {
        val notes = getAllNotes.execute().first()
        assertTrue(notes.isEmpty())
    }
}
```

- [ ] **Step 6: Run tests to verify they fail (use cases not yet complete)**

Run: `./gradlew :shared:commonTest`
Expected: tests compile (use cases exist), all pass since implementation is done in same step.
If any fail, check the `FakeNoteRepository` flow emission logic.

- [ ] **Step 7: Run tests and confirm all pass**

Run: `./gradlew :shared:commonTest`
Expected: `4 tests passed`

- [ ] **Step 8: Commit**

```bash
git add shared/src/
git commit -m "feat: add GetAllNotes, SaveNote, DeleteNote use cases with tests"
```

---

## Task 6: ExportMarkdownUseCase with Tests

**Files:**
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/domain/usecase/ExportMarkdownUseCase.kt`
- Create: `shared/src/commonTest/kotlin/com/noteapp/shared/domain/usecase/ExportMarkdownUseCaseTest.kt`

- [ ] **Step 1: Write failing tests first**

```kotlin
package com.noteapp.shared.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class ExportMarkdownUseCaseTest {

    private val useCase = ExportMarkdownUseCase()

    @Test
    fun `converts h1 to markdown heading`() {
        assertEquals("# Title", useCase.execute("<h1>Title</h1>").trim())
    }

    @Test
    fun `converts h2 to markdown heading`() {
        assertEquals("## Title", useCase.execute("<h2>Title</h2>").trim())
    }

    @Test
    fun `converts bold to markdown bold`() {
        assertEquals("**hello**", useCase.execute("<strong>hello</strong>").trim())
    }

    @Test
    fun `converts italic to markdown italic`() {
        assertEquals("_hello_", useCase.execute("<em>hello</em>").trim())
    }

    @Test
    fun `converts inline code to markdown code`() {
        assertEquals("`code`", useCase.execute("<code>code</code>").trim())
    }

    @Test
    fun `converts list items to markdown bullets`() {
        val result = useCase.execute("<ul><li>Item 1</li><li>Item 2</li></ul>")
        assert(result.contains("- Item 1")) { "Expected '- Item 1' in: $result" }
        assert(result.contains("- Item 2")) { "Expected '- Item 2' in: $result" }
    }

    @Test
    fun `strips remaining html tags`() {
        assertEquals("plain text", useCase.execute("<p>plain text</p>").trim())
    }

    @Test
    fun `decodes html entities`() {
        assertEquals("a & b < c > d", useCase.execute("a &amp; b &lt; c &gt; d").trim())
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :shared:commonTest --tests "*.ExportMarkdownUseCaseTest"`
Expected: compilation error — `ExportMarkdownUseCase` does not exist yet.

- [ ] **Step 3: Implement `ExportMarkdownUseCase.kt`**

```kotlin
package com.noteapp.shared.domain.usecase

class ExportMarkdownUseCase {
    fun execute(htmlContent: String): String = htmlContent
        .replace(Regex("<h1[^>]*>(.*?)</h1>", RegexOption.DOT_MATCHES_ALL)) { "# ${it.groupValues[1]}\n" }
        .replace(Regex("<h2[^>]*>(.*?)</h2>", RegexOption.DOT_MATCHES_ALL)) { "## ${it.groupValues[1]}\n" }
        .replace(Regex("<strong[^>]*>(.*?)</strong>", RegexOption.DOT_MATCHES_ALL)) { "**${it.groupValues[1]}**" }
        .replace(Regex("<b[^>]*>(.*?)</b>", RegexOption.DOT_MATCHES_ALL)) { "**${it.groupValues[1]}**" }
        .replace(Regex("<em[^>]*>(.*?)</em>", RegexOption.DOT_MATCHES_ALL)) { "_${it.groupValues[1]}_" }
        .replace(Regex("<i[^>]*>(.*?)</i>", RegexOption.DOT_MATCHES_ALL)) { "_${it.groupValues[1]}_" }
        .replace(Regex("<code[^>]*>(.*?)</code>", RegexOption.DOT_MATCHES_ALL)) { "`${it.groupValues[1]}`" }
        .replace(Regex("<li[^>]*>(.*?)</li>", RegexOption.DOT_MATCHES_ALL)) { "- ${it.groupValues[1]}\n" }
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .trim()
}
```

- [ ] **Step 4: Run tests and confirm all pass**

Run: `./gradlew :shared:commonTest --tests "*.ExportMarkdownUseCaseTest"`
Expected: `8 tests passed`

- [ ] **Step 5: Commit**

```bash
git add shared/src/
git commit -m "feat: add ExportMarkdownUseCase with HTML-to-Markdown conversion"
```

---

## Task 7: Koin Dependency Injection

**Files:**
- Create: `shared/src/commonMain/kotlin/com/noteapp/shared/di/SharedModule.kt`
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/di/AppModule.kt`

- [ ] **Step 1: Create `SharedModule.kt`**

```kotlin
package com.noteapp.shared.di

import com.noteapp.shared.data.local.DatabaseDriverFactory
import com.noteapp.shared.data.local.LocalNoteRepository
import com.noteapp.shared.database.NoteAppDatabase
import com.noteapp.shared.domain.repository.NoteRepository
import com.noteapp.shared.domain.usecase.DeleteNoteUseCase
import com.noteapp.shared.domain.usecase.ExportMarkdownUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.shared.domain.usecase.SaveNoteUseCase
import org.koin.dsl.module

val sharedModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { NoteAppDatabase(get()) }
    single<NoteRepository> { LocalNoteRepository(get()) }
    factory { GetAllNotesUseCase(get()) }
    factory { SaveNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }
    factory { ExportMarkdownUseCase() }
}
```

- [ ] **Step 2: Create `AppModule.kt`**

```kotlin
package com.noteapp.di

import com.noteapp.ui.screen.edit.EditViewModel
import com.noteapp.ui.screen.home.HomeViewModel
import org.koin.dsl.module

val appModule = module {
    factory { HomeViewModel(get(), get()) }
    factory { (noteId: String?) -> EditViewModel(noteId, get(), get(), get()) }
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/noteapp/shared/di/
git add composeApp/src/commonMain/kotlin/com/noteapp/di/
git commit -m "feat: configure Koin DI modules for shared and app layers"
```

---

## Task 8: Theme System

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/theme/ThemeMode.kt`
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/theme/NoteColors.kt`
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/theme/AppTheme.kt`

- [ ] **Step 1: Create `ThemeMode.kt`**

```kotlin
package com.noteapp.ui.theme

enum class ThemeMode { COLORFUL, DARK }
```

- [ ] **Step 2: Create `NoteColors.kt`**

```kotlin
package com.noteapp.ui.theme

import androidx.compose.ui.graphics.Color

val noteColorPalette = listOf(
    Color(0xFFFEF3C7), // Amber 100
    Color(0xFFD1FAE5), // Emerald 100
    Color(0xFFFCE7F3), // Pink 100
    Color(0xFFDBEAFE), // Blue 100
    Color(0xFFEDE9FE), // Violet 100
    Color(0xFFFEE2E2)  // Red 100
)

val accentColor = Color(0xFFF59E0B)        // Amber 500
val backgroundColorful = Color(0xFFFFFBF5) // Warm white
val surfaceColorful = Color(0xFFFFFFFF)
val onSurfaceColorful = Color(0xFF111827)
val onSurfaceVariantColorful = Color(0xFF6B7280)
```

- [ ] **Step 3: Create `AppTheme.kt`**

```kotlin
package com.noteapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val colorfulScheme = lightColorScheme(
    primary = accentColor,
    background = backgroundColorful,
    surface = surfaceColorful,
    onSurface = onSurfaceColorful,
    onSurfaceVariant = onSurfaceVariantColorful,
    secondary = Color(0xFF6366F1)
)

// Reserved: full dark palette to be implemented when DARK mode is built
private val darkScheme = darkColorScheme(
    primary = accentColor
)

@Composable
fun AppTheme(
    mode: ThemeMode = ThemeMode.COLORFUL,
    content: @Composable () -> Unit
) {
    val colorScheme = when (mode) {
        ThemeMode.COLORFUL -> colorfulScheme
        ThemeMode.DARK -> darkScheme  // placeholder — returns default dark scheme
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/noteapp/ui/theme/
git commit -m "feat: add AppTheme with COLORFUL mode and reserved DARK mode"
```

---

## Task 9: HomeViewModel with Tests

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/home/HomeViewModel.kt`
- Create: `composeApp/src/commonTest/kotlin/com/noteapp/ui/FakeNoteRepository.kt`
- Create: `composeApp/src/commonTest/kotlin/com/noteapp/ui/screen/home/HomeViewModelTest.kt`

- [ ] **Step 1: Create `FakeNoteRepository.kt` in composeApp test sources**

```kotlin
package com.noteapp.ui

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    val flow = MutableStateFlow<List<Note>>(emptyList())

    override fun getAllNotes(): Flow<List<Note>> = flow

    override suspend fun getNoteById(id: String): Note? = notes.find { it.id == id }

    override suspend fun saveNote(note: Note) {
        notes.removeAll { it.id == note.id }
        notes.add(note)
        flow.value = notes.toList()
    }

    override suspend fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
        flow.value = notes.toList()
    }
}
```

- [ ] **Step 2: Write failing `HomeViewModelTest.kt`**

```kotlin
package com.noteapp.ui.screen.home

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.DeleteNoteUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.ui.FakeNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeNoteRepository
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeNoteRepository()
        viewModel = HomeViewModel(GetAllNotesUseCase(repo), DeleteNoteUseCase(repo))
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun sampleNote(id: String) = Note(
        id = id, title = "Note $id", htmlContent = "<p>content</p>",
        markdownContent = "content", colorHex = "#FEF3C7",
        createdAt = 1000L, updatedAt = 1000L
    )

    @Test
    fun `notes state reflects repository flow`() = runTest {
        repo.saveNote(sampleNote("1"))
        repo.saveNote(sampleNote("2"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.notes.value.size)
    }

    @Test
    fun `deleteNote removes note`() = runTest {
        repo.saveNote(sampleNote("1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.deleteNote("1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.notes.value.isEmpty())
    }
}
```

- [ ] **Step 3: Run tests to verify they fail (HomeViewModel not yet created)**

Run: `./gradlew :composeApp:commonTest --tests "*.HomeViewModelTest"`
Expected: compilation error — `HomeViewModel` not found.

- [ ] **Step 4: Create `HomeViewModel.kt`**

```kotlin
package com.noteapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.DeleteNoteUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    getAllNotes: GetAllNotesUseCase,
    private val deleteNote: DeleteNoteUseCase
) : ViewModel() {

    val notes: StateFlow<List<Note>> = getAllNotes.execute()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteNote(id: String) {
        viewModelScope.launch { deleteNote.execute(id) }
    }
}
```

- [ ] **Step 5: Run tests and confirm all pass**

Run: `./gradlew :composeApp:commonTest --tests "*.HomeViewModelTest"`
Expected: `2 tests passed`

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/
git commit -m "feat: add HomeViewModel with note list and delete, tested"
```

---

## Task 10: NoteCard + HomeScreen

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/home/NoteCard.kt`
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/home/HomeScreen.kt`

- [ ] **Step 1: Create `NoteCard.kt`**

```kotlin
package com.noteapp.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noteapp.shared.domain.model.Note

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = try {
        Color(android.graphics.Color.parseColor(note.colorHex))
    } catch (_: Exception) {
        Color(0xFFFEF3C7)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp)
    ) {
        Column {
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = note.markdownContent.take(120),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

Note: `android.graphics.Color.parseColor` is Android-only. Replace with a pure KMP hex parser:

```kotlin
// Pure KMP hex color parser — replace the try/catch block above
fun parseHexColor(hex: String): Color {
    val cleaned = hex.trimStart('#')
    val argb = cleaned.toLongOrNull(16) ?: return Color(0xFFFEF3C7)
    return if (cleaned.length == 6) Color((0xFF000000L or argb).toInt())
    else Color(argb.toInt())
}
```

Use `parseHexColor(note.colorHex)` instead of the Android-specific call.

- [ ] **Step 2: Create `HomeScreen.kt`**

```kotlin
package com.noteapp.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noteapp.shared.domain.model.Note
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNoteClick: (String) -> Unit,
    onNewNote: () -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val notes by viewModel.notes.collectAsState()
    var noteToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("便签") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(Icons.Default.Add, contentDescription = "新建便签")
            }
        }
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 80.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { noteToDelete = note.id }
                )
            }
        }
    }

    noteToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("删除便签") },
            text = { Text("确定要删除这条便签吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(id)
                    noteToDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("取消") }
            }
        )
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/home/
git commit -m "feat: add NoteCard component and HomeScreen with grid layout"
```

---

## Task 11: EditViewModel with Tests

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/edit/EditViewModel.kt`
- Create: `composeApp/src/commonTest/kotlin/com/noteapp/ui/screen/edit/EditViewModelTest.kt`

- [ ] **Step 1: Write failing `EditViewModelTest.kt`**

```kotlin
package com.noteapp.ui.screen.edit

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.ExportMarkdownUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.shared.domain.usecase.SaveNoteUseCase
import com.noteapp.ui.FakeNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeNoteRepository
    private lateinit var viewModel: EditViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeNoteRepository()
    }

    @AfterTest
    fun teardown() { Dispatchers.resetMain() }

    private fun makeViewModel(noteId: String? = null) = EditViewModel(
        noteId = noteId,
        saveNoteUseCase = SaveNoteUseCase(repo),
        getAllNotesUseCase = GetAllNotesUseCase(repo),
        exportMarkdownUseCase = ExportMarkdownUseCase()
    )

    @Test
    fun `new note starts with empty title and WYSIWYG mode`() = runTest {
        viewModel = makeViewModel(noteId = null)
        assertEquals("", viewModel.title.value)
        assertTrue(viewModel.isWysiwygMode.value)
    }

    @Test
    fun `existing note loads title on init`() = runTest {
        repo.saveNote(Note("id1", "Hello", "<p>World</p>", "World", "#FEF3C7", 1000L, 1000L))
        viewModel = makeViewModel(noteId = "id1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Hello", viewModel.title.value)
    }

    @Test
    fun `toggleMode switches between WYSIWYG and MD source`() = runTest {
        viewModel = makeViewModel()
        assertTrue(viewModel.isWysiwygMode.value)
        viewModel.toggleMode()
        assertFalse(viewModel.isWysiwygMode.value)
        viewModel.toggleMode()
        assertTrue(viewModel.isWysiwygMode.value)
    }

    @Test
    fun `saveNote persists note to repository`() = runTest {
        viewModel = makeViewModel(noteId = null)
        viewModel.updateTitle("My Note")
        viewModel.saveNote("<p>Content</p>")
        testDispatcher.scheduler.advanceUntilIdle()
        val saved = repo.getNoteById(viewModel.noteId)
        assertEquals("My Note", saved?.title)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :composeApp:commonTest --tests "*.EditViewModelTest"`
Expected: compilation error — `EditViewModel` not found.

- [ ] **Step 3: Create `EditViewModel.kt`**

```kotlin
package com.noteapp.ui.screen.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.ExportMarkdownUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.shared.domain.usecase.SaveNoteUseCase
import com.noteapp.ui.theme.noteColorPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditViewModel(
    noteId: String?,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val exportMarkdownUseCase: ExportMarkdownUseCase
) : ViewModel() {

    // If noteId is null, generate a new UUID for this note
    val noteId: String = noteId ?: generateId()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _isWysiwygMode = MutableStateFlow(true)
    val isWysiwygMode: StateFlow<Boolean> = _isWysiwygMode

    private val _markdownSource = MutableStateFlow("")
    val markdownSource: StateFlow<String> = _markdownSource

    private val _colorHex = MutableStateFlow(
        "#" + noteColorPalette.random().value.toString(16).uppercase().takeLast(6)
    )
    val colorHex: StateFlow<String> = _colorHex

    // Loaded HTML for the rich editor on open
    private val _initialHtml = MutableStateFlow("")
    val initialHtml: StateFlow<String> = _initialHtml

    private var isNewNote = noteId == null

    init {
        if (!isNewNote) {
            viewModelScope.launch { loadNote() }
        }
    }

    private suspend fun loadNote() {
        val notes = getAllNotesUseCase.execute().first()
        notes.find { it.id == this.noteId }?.let { note ->
            _title.value = note.title
            _initialHtml.value = note.htmlContent
            _colorHex.value = note.colorHex
        }
    }

    fun updateTitle(value: String) { _title.value = value }

    fun updateColorHex(hex: String) { _colorHex.value = hex }

    fun toggleMode() { _isWysiwygMode.value = !_isWysiwygMode.value }

    fun updateMarkdownSource(md: String) { _markdownSource.value = md }

    fun saveNote(htmlContent: String) {
        val markdown = exportMarkdownUseCase.execute(htmlContent)
        val note = Note(
            id = noteId,
            title = _title.value,
            htmlContent = htmlContent,
            markdownContent = markdown,
            colorHex = _colorHex.value,
            createdAt = if (isNewNote) 0L else System.currentTimeMillis(),
            updatedAt = 0L
        )
        viewModelScope.launch {
            saveNoteUseCase.execute(note)
            isNewNote = false
        }
    }

    private fun generateId(): String {
        // Simple UUID-like ID using current time and random
        val time = System.currentTimeMillis().toString(16)
        val rand = (0..0xFFFF).random().toString(16).padStart(4, '0')
        return "$time-$rand"
    }
}
```

Note: `System.currentTimeMillis()` is not available in common KMP code. Replace with `kotlinx.datetime.Clock.System.now().toEpochMilliseconds()` in `saveNote`. The `generateId()` also uses this — replace with `Clock.System.now().toEpochMilliseconds()`.

- [ ] **Step 4: Fix KMP compatibility — replace `System.currentTimeMillis()` in `EditViewModel.kt`**

Replace both occurrences of `System.currentTimeMillis()` and `(0..0xFFFF).random()` with:

```kotlin
import kotlinx.datetime.Clock

// In saveNote:
createdAt = if (isNewNote) 0L else Clock.System.now().toEpochMilliseconds(),

// In generateId:
private fun generateId(): String {
    val time = Clock.System.now().toEpochMilliseconds().toString(16)
    val rand = (0..0xFFFF).random().toString(16).padStart(4, '0')
    return "$time-$rand"
}
```

- [ ] **Step 5: Run tests and confirm all pass**

Run: `./gradlew :composeApp:commonTest --tests "*.EditViewModelTest"`
Expected: `4 tests passed`

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/
git commit -m "feat: add EditViewModel with save, load, and mode toggle, tested"
```

---

## Task 12: RichEditorToolbar + EditScreen

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/edit/RichEditorToolbar.kt`
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/edit/EditScreen.kt`

- [ ] **Step 1: Create `RichEditorToolbar.kt`**

```kotlin
package com.noteapp.ui.screen.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun RichEditorToolbar(state: RichTextState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToolbarButton("B", state.currentSpanStyle.fontWeight == FontWeight.Bold) {
            state.toggleSpanStyle(state.currentSpanStyle.copy(fontWeight = FontWeight.Bold))
        }
        ToolbarButton("I", state.currentSpanStyle.fontStyle == FontStyle.Italic) {
            state.toggleSpanStyle(state.currentSpanStyle.copy(fontStyle = FontStyle.Italic))
        }
        ToolbarButton("H1", false) {
            state.toggleParagraphStyle(state.currentParagraphStyle.copy())
            state.addParagraphStyle(androidx.compose.ui.text.ParagraphStyle())
        }
        ToolbarButton("• 列表", false) {
            state.toggleUnorderedList()
        }
        ToolbarButton("1. 列表", false) {
            state.toggleOrderedList()
        }
    }
}

@Composable
private fun ToolbarButton(label: String, active: Boolean, onClick: () -> Unit) {
    val containerColor = if (active)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
```

- [ ] **Step 2: Create `EditScreen.kt`**

```kotlin
package com.noteapp.ui.screen.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.noteapp.ui.theme.noteColorPalette
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    noteId: String?,
    onBack: () -> Unit
) {
    val viewModel: EditViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val title by viewModel.title.collectAsState()
    val isWysiwyg by viewModel.isWysiwygMode.collectAsState()
    val markdownSource by viewModel.markdownSource.collectAsState()
    val initialHtml by viewModel.initialHtml.collectAsState()
    val colorHex by viewModel.colorHex.collectAsState()

    val richTextState = rememberRichTextState()

    // Load initial HTML into editor once
    LaunchedEffect(initialHtml) {
        if (initialHtml.isNotEmpty()) {
            richTextState.setHtml(initialHtml)
        }
    }

    // Sync RichTextState → markdown when switching to MD source mode
    LaunchedEffect(isWysiwyg) {
        if (!isWysiwyg) {
            viewModel.updateMarkdownSource(richTextState.toMarkdown())
        } else {
            richTextState.setMarkdown(markdownSource)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveNote(richTextState.toHtml())
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                title = {
                    TextField(
                        value = title,
                        onValueChange = viewModel::updateTitle,
                        placeholder = { Text("标题") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                },
                actions = {
                    TextButton(onClick = viewModel::toggleMode) {
                        Text(if (isWysiwyg) "MD源码" else "富文本")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (isWysiwyg) {
                    HorizontalDivider()
                    RichEditorToolbar(richTextState)
                }
                // Color picker strip
                HorizontalDivider()
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    noteColorPalette.forEach { color ->
                        val hex = "#" + color.value.toString(16).uppercase().takeLast(6)
                        IconButton(
                            onClick = { viewModel.updateColorHex(hex) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Surface(
                                color = color,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.size(24.dp),
                                border = if (hex == colorHex)
                                    ButtonDefaults.outlinedButtonBorder(true)
                                else null
                            ) {}
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isWysiwyg) {
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                )
            } else {
                TextField(
                    value = markdownSource,
                    onValueChange = viewModel::updateMarkdownSource,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/noteapp/ui/screen/edit/
git commit -m "feat: add EditScreen with WYSIWYG editor, MD source toggle, and color picker"
```

---

## Task 13: Navigation + App Root

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/ui/navigation/AppNavigation.kt`
- Create: `composeApp/src/commonMain/kotlin/com/noteapp/App.kt`

- [ ] **Step 1: Create `AppNavigation.kt`**

```kotlin
package com.noteapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.noteapp.ui.screen.edit.EditScreen
import com.noteapp.ui.screen.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNoteClick = { noteId -> navController.navigate("edit/$noteId") },
                onNewNote = { navController.navigate("edit/new") }
            )
        }
        composable(
            route = "edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getString("noteId")
            val noteId = if (rawId == "new") null else rawId
            EditScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **Step 2: Create `App.kt`**

```kotlin
package com.noteapp

import androidx.compose.runtime.Composable
import com.noteapp.ui.navigation.AppNavigation
import com.noteapp.ui.theme.AppTheme
import com.noteapp.ui.theme.ThemeMode

@Composable
fun App(themeMode: ThemeMode = ThemeMode.COLORFUL) {
    AppTheme(mode = themeMode) {
        AppNavigation()
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/noteapp/ui/navigation/
git add composeApp/src/commonMain/kotlin/com/noteapp/App.kt
git commit -m "feat: add navigation graph and App root composable"
```

---

## Task 14: Platform Entry Points + Final Wiring

**Files:**
- Create: `composeApp/src/androidMain/kotlin/com/noteapp/MainActivity.kt`
- Create: `composeApp/src/desktopMain/kotlin/com/noteapp/main.kt`

- [ ] **Step 1: Create `MainActivity.kt`**

```kotlin
package com.noteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.noteapp.shared.data.local.DatabaseDriverFactory
import com.noteapp.shared.di.sharedModule
import com.noteapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(applicationContext)
            modules(
                module { single { DatabaseDriverFactory(androidContext()) } },
                sharedModule,
                appModule
            )
        }

        setContent { App() }
    }
}
```

- [ ] **Step 2: Create `main.kt`**

```kotlin
package com.noteapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.noteapp.shared.data.local.DatabaseDriverFactory
import com.noteapp.shared.di.sharedModule
import com.noteapp.di.appModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() = application {
    startKoin {
        modules(
            module { single { DatabaseDriverFactory() } },
            sharedModule,
            appModule
        )
    }

    Window(onCloseRequest = ::exitApplication, title = "NoteApp") {
        App()
    }
}
```

- [ ] **Step 3: Build Desktop to verify compilation**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Build Android to verify compilation**

Run: `./gradlew :composeApp:assembleDebug`
Expected: `BUILD SUCCESSFUL` and `.apk` in `composeApp/build/outputs/apk/debug/`

- [ ] **Step 5: Run Desktop app and smoke test**

Run: `./gradlew :composeApp:run`
Expected:
- App window opens
- Home screen shows empty grid with "便签" title
- Tap "+" opens EditScreen
- Type title and content, tap back → note appears in grid
- Note survives app restart (check by quitting and re-running)

- [ ] **Step 6: Final commit**

```bash
git add composeApp/src/androidMain/ composeApp/src/desktopMain/
git commit -m "feat: wire up platform entry points and Koin initialization"
git tag v0.1.0-demo
```

---

## Spec Coverage Check

| Spec Requirement | Task |
|-----------------|------|
| KMP Desktop + Android | Task 1, 14 |
| Local SQLite storage | Task 3, 4 |
| Sync interface reserved | Task 2 (NoteRepository interface) |
| Card grid layout | Task 10 (HomeScreen) |
| Full-screen editing | Task 12 (EditScreen) |
| Colorful theme default | Task 8 (AppTheme) |
| Dark mode interface reserved | Task 8 (ThemeMode enum + darkScheme stub) |
| WYSIWYG rich text editor | Task 12 (RichTextEditor) |
| Formatting toolbar | Task 12 (RichEditorToolbar) |
| MD source view toggle | Task 11 (EditViewModel), Task 12 (EditScreen) |
| Note persistence survives restart | Task 3 (SQLDelight) |
| Create / edit / delete notes | Task 10, 12, 9 |
