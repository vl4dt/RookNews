# RookNews — Project Status & Roadmap

> Last updated: 2026-05-09
> Build: ✅ **Green** — `composeApp:assembleDebug` passes (20MB APK)
> Codebase: ~2000 lines of Kotlin across 55+ files

---

## What is RookNews?

Offline-first, on-device AI news curator for **Android & iOS**.
Built with **Kotlin Multiplatform (KMP)** + **Compose Multiplatform**.

**Core value**: Read the news your way — AI-summarized, bias-analyzed, personalized — without sending your reading habits to a server.

**Two tiers**:
- **Free** — fully offline, on-device AI (Gemma 4 E2B/E4B via llama.cpp)
- **Pro** — optional cloud models (Gemini API) for speed + power

---

## Architecture

```
composeApp/               ← UI, navigation, DI wiring (Android + shared Compose)
core/
├── domain/               ← Pure Kotlin: models, repository interfaces, use cases
├── data/                 ← Repositories, SQLDelight, Ktor, RSS parsing, FeedCatalog
└── ai/                   ← InferenceProvider abstraction, AI pipeline, prompt templates
```

**Clean Architecture flow**: `composeApp` → `core:domain` (interfaces) → `core:data` + `core:ai` (implementations)

**Key stack**:
| Concern | Choice |
|---------|--------|
| Language | Kotlin 2.1.0 |
| UI | Compose Multiplatform 1.7.3 (~95% shared) |
| Navigation | Compose ViewModel (5-tab bottom nav, no Decompose) |
| State | MVVM + StateFlow + custom `AiStreamState` |
| DI | Koin 4.0.1 |
| Database | SQLDelight 2.0.1 |
| Network | Ktor 3.0.1 (CIO / Darwin engines) |
| Inference | llama.cpp via KMP cinterop (GGUF Gemma 4 E2B/E4B) |
| Cloud fallback | Google Gemini API (Pro tier) |

**Platform targets**:
- Android: minSdk 24, targetSdk 35
- iOS: deployment target 14.1
- Kotlin: 2.1.0

---

## Current Status

### ✅ Phase 1 — Foundation ✅

| Area | What's in place |
|------|----------------|
| **Build system** | Gradle 8.10.2, Kotlin DSL, version catalog, 4 modules |
| **Domain models** | `FeedSource`, `Article`, `Briefing`, `UserPreferences`, `AiModel` + enums |
| **Repository interfaces** | `ArticleRepository`, `FeedRepository`, `PreferencesRepository`, `BriefingRepository`, `AiRepository`, `FeatureAccessRepository` |
| **Use cases** | `GetArticlesUseCase`, `AiUseCases` (summarize, briefing, bias, deep research) |
| **Data layer** | Repository stubs, `KtorClientFactory`, `FeedCatalog.kt` (18 seed feeds) |
| **AI layer** | `InferenceProvider` interface, `LocalInferenceProvider`, `CloudInferenceProvider`, `AiProcessingPipeline` (with prompt templates) |
| **DI** | Koin modules wired: `dataModule`, `aiModule`, `AppKoinModule` |
| **Android** | `MainActivity`, `RookNewsApplication`, `AndroidManifest.xml`, network security config, ProGuard rules |
| **iOS** | `Podfile` stub |
| **Build** | `composeApp:assembleDebug` → 19MB APK ✅ |

### ✅ Phase 2 — Data Layer ✅

| Area | What's in place |
|------|----------------|
| **SQLDelight schema** | `Article.sq`, `FeedSource.sq`, `Briefing.sq` — 3 tables with full CRUD queries |
| **Database provider** | `DatabaseProvider` with expect/actual pattern (AndroidSqliteDriver / NativeSqliteDriver) |
| **Model mappers** | `Article.toDomain()`, `Feed_source.toDomain()`, `Briefing.toDomain()`, JSON array helpers |
| **Article repository** | `SqlDelightArticleRepository` — full flow-based reads, sort/filter, search, mark read/bookmark |
| **Feed repository** | `DefaultFeedRepository` — CRUD + seed from `FeedCatalog` |
| **Briefing repository** | `DefaultBriefingRepository` — CRUD with JSON article ID arrays |
| **Preferences repository** | `DefaultPreferencesRepository` — Okio file-based key-value store |
| **RSS fetching** | `RssFetcher` — Ktor-based HTTP client with timestamp cursor |
| **RSS parsing** | `RssParser` — string-based RSS 2.0 + Atom 1.0 parser (KMP-compatible, no `java.util.Date`) |
| **Koin wiring** | `dataModule` provides `ArticleDatabase`, `RssFetcher`, `RssParser`, all repositories |
| **Android init** | `RookNewsApplication.onCreate()` sets app context + lazy DB init |

### ✅ Phase 3 — UI Foundation ✅

| Area | What's in place |
|------|----------------|
| **Design system** | `RookNewsTheme` with light/sepia/dark modes, amber accent, typography hierarchy |
| **Navigation** | `Scaffold` + `NavigationBar` with 5 tabs, full-screen push for article detail |
| **Home tab** | Article feed list with loading/error/empty states, `ArticleCard` with summary |
| **Feeds tab** | Feed catalog with sticky category filter chips, toggle enable/disable |
| **Briefings tab** | Briefing list with FAB to generate, `BriefingCard` with content preview |
| **Research tab** | Query input + result display with provider badge, loading/error states |
| **Settings tab** | Reading mode radio rows, sync settings, AI model info, about section |
| **Article detail** | Full article view with AI summary toggle, bias indicator, topic chips |
| **ViewModels** | 6 ViewModels: Home, Feeds, Briefings, Research, Settings, ArticleDetail |
| **Architecture** | Decompose removed — direct Compose ViewModel + StateFlow pattern |

### 🔴 Phase 4 — AI Pipeline (In progress)

| Area | What's in place |
|------|----------------|
| **AiStreamState** | Sealed interface: Idle, Loading, Streaming(token, fullText), Complete, Error |
| **ModelCatalog** | 2 models: Gemma 4 E2B (phone, 3.5GB), Gemma 4 E4B (tablet, 5.4GB) with GGUF URLs, capabilities, RAM gating |
| **ModelManager** | Download progress tracking, activation, local scan via Okio |
| **CloudInferenceProvider** | Gemini API integration (gemini-2.0-flash), request/response types |
| **LocalInferenceProvider** | Stub (llama.cpp cinterop pending) |
| **AiProcessingPipeline** | 5 operations: summarize, bias, classify, briefing, research + streaming variants |
| **DefaultAiRepository** | Wired to pipeline + model manager |
| **BriefingsViewModel** | AI briefing generation with FAB trigger |
| **HomeViewModel** | On-demand article summarization |
| **ResearchViewModel** | Deep research with result display |

### 🔴 Phase 5 — Sync & Background (Not started)

### 🔴 Phase 6 — Monetization (Not started)

### 🔴 Phase 7 — Polish & Launch (Not started)

---

## Roadmap

### Phase 3: UI Foundation ✅ (Complete)

**Goal**: App is navigable with the 5-tab shell and design system.

- [x] Implement "Warm Paper" design system
  - [x] Color palette (light, sepia, dark modes)
  - [x] Typography (Source Serif 4 + Inter)
  - [x] Theme composable with mode switching
- [x] Set up navigation
  - [x] Compose ViewModel pattern (Decompose removed)
  - [x] Bottom navigation bar
  - [x] Full-screen push for article detail
- [x] Build screens (scaffold → content)
  - [x] **Home tab** — article feed list with loading/error/empty states
  - [x] **Feeds tab** — feed catalog with category filter chips + toggle
  - [x] **Briefings tab** — briefing history + "generate" FAB
  - [x] **Research tab** — deep research input + results
  - [x] **Settings tab** — model management, theme, preferences
- [x] Article detail screen (full-screen push)
  - [x] AI summary toggle
  - [x] Bias indicator badge
  - [ ] Markdown rendering (library or WebView fallback)

### Phase 4: AI Pipeline 🔜 (In progress)

**Goal**: On-device inference working end-to-end.

- [x] `AiStreamState` sealed interface for token streaming UI
- [x] Model catalog (Gemma 4 E2B for phones, Gemma 4 E4B for tablets) with RAM gating
- [x] `ModelManager` with download progress, activation, Okio storage
- [x] `CloudInferenceProvider` — Gemini API integration (gemini-2.0-flash)
- [ ] Set up llama.cpp KMP cinterop
  - [ ] Native library integration (Android ABI + iOS arm64)
  - [ ] `LocalInferenceProvider` actual implementation
- [ ] Model management
  - [ ] Model download flow (staged, resumable)
  - [ ] Model storage tracking + UI
  - [ ] Single active model selection
- [ ] AI processing pipeline
  - [x] On-demand summarization (user taps "summarize")
  - [x] Briefing generation (FAB in Briefings tab)
  - [x] Deep research (Research tab)
  - [ ] Lightweight on-download classification/scoring
  - [ ] Background briefing generation
  - [ ] Bias analysis
- [ ] Cloud inference provider (Gemini API integration) ✅ wired, needs API key
- [ ] ViewModels connect to AI pipeline ✅ done

### Phase 5: Sync & Background

**Goal**: Articles arrive in background without user action.

- [ ] Android: WorkManager periodic sync (~30 min)
- [ ] iOS: BGAppRefreshTaskRequest (best-effort)
- [ ] Foreground sync on app open
- [ ] Sync cursor management (lastSync timestamps)
- [ ] Local notifications for breaking news
- [ ] "Swipe to sync" in article feed

### Phase 6: Monetization

**Goal**: Pro tier functional on both platforms.

- [ ] Google Play Billing integration (Android)
- [ ] StoreKit 2 integration (iOS)
- [ ] `FeatureAccessRepository` implementations
- [ ] Pro feature gating (cloud model access, advanced briefings)
- [ ] License verification (serverless, client-side validation)
- [ ] Subscription UI in Settings

### Phase 7: Polish & Launch

**Goal**: Production-ready app.

- [ ] Error handling + retry UX
- [ ] Offline indicators
- [ ] Empty states
- [ ] Loading skeletons
- [ ] Accessibility (content descriptions, dynamic type)
- [ ] Unit tests (use cases, domain logic)
- [ ] Compose Previews for all screens
- [ ] ProGuard/R8 rules finalized
- [ ] App icons, splash screen
- [ ] Store listing assets
- [ ] Privacy policy

---

## Locked Decisions (Cross-Reference)

### Design System — "Warm Paper"
- Light, sepia, dark reading modes
- Amber accent color
- Source Serif 4 (headings) + Inter (body)
- Minimalist card layouts
- Amber-accented AI indicators

### AI Processing — Hybrid Pipeline
- `InferenceProvider` interface abstracts local vs cloud
- Lightweight on-download → on-demand → background heavy AI
- Gemma 4 E2B (~1.5GB) and E4B (~2.8GB) Q4_K_M models

### Data — Offline-First
- RSS ingestion + SQLDelight metadata + Okio Markdown bodies
- Timestamp cursor sync
- Markdown caching

### Navigation — 5 Tabs
1. **Home** — article feed
2. **Feeds** — feed catalog management
3. **Briefings** — AI-generated briefings
4. **Research** — deep research
5. **Settings** — preferences, model management

### Monetization — Confirmed for Honduras
- ✅ Google Play Billing — Honduras supported, USD payouts
- ✅ Apple StoreKit 2 — Honduras supported, USD payouts

---

## SQLDelight Schema Summary

### Article table
- `id` (TEXT PK), `feed_source_id` (TEXT), `title` (TEXT)
- `summary`, `body_markdown`, `author` (TEXT nullable)
- `published_at`, `fetched_at` (INTEGER)
- `image_url` (TEXT nullable), `original_url` (TEXT)
- `read`, `bookmarked` (INTEGER 0/1)
- `ai_summary` (TEXT nullable), `ai_bias_score` (REAL nullable)
- `ai_topics` (TEXT — JSON array), `relevance_score` (REAL nullable)

### Feed_source table
- `id` (TEXT PK), `title` (TEXT), `url` (TEXT), `feed_url` (TEXT)
- `category` (TEXT), `region`, `language` (TEXT nullable)
- `enabled` (INTEGER 0/1), `last_fetched` (INTEGER nullable)

### Briefing table
- `id` (TEXT PK), `title` (TEXT), `content_markdown` (TEXT)
- `generated_at` (INTEGER), `type` (TEXT — enum name)
- `article_ids` (TEXT — JSON array), `reading_time_minutes` (INTEGER)

### Key Queries
- `selectAllArticles`, `selectUnreadArticles`, `selectBookmarkedArticles`
- `selectArticlesByRelevance`, `selectArticlesByTitle`
- `searchArticles`, `selectArticleById`
- `updateArticleRead`, `updateArticleBookmarked`
- `selectAllFeeds`, `selectFeedById`, `updateFeedEnabled`
- `selectAllBriefings`, `selectBriefingById`

---

## Build Commands

```bash
# Compile Android (works on Windows)
./gradlew :composeApp:assembleDebug --no-configuration-cache

# Compile all modules (metadata check, works on Windows)
./gradlew assemble --no-configuration-cache

# iOS build (requires macOS)
./gradlew :composeApp:assembleDebug --no-configuration-cache
cd iosApp && pod install
```

---

## Known Constraints

- Developer based in **Honduras** — global audience app
- No paid CI/CD — local builds only (Windows PC + MacBook Pro M1)
- Gradle config cache issues on Windows → always use `--no-configuration-cache`
- iOS targets disabled on Windows → use `compileKotlinMetadata` for cross-platform checks
- `kotlin.native.ignoreDisabledTargets=true` in `gradle.properties`

---

## Session Notes (2026-05-09)

### Phase 2 Implementation
- Created SQLDelight `.sq` files for all 3 tables with proper queries
- Fixed SQLDelight 2.x syntax (requires `queryName:` prefix)
- Implemented `DatabaseProvider` with expect/actual for Android (needs Context) and iOS (in-memory)
- Built `ModelMappers.kt` with `toDomain()` extension functions + JSON array serialization helpers
- Rewired all repository stubs to use actual SQLDelight queries with Flow-based reads
- Implemented `RssFetcher` (Ktor HTTP client) and `RssParser` (string-based RSS 2.0 + Atom 1.0)
- Fixed `mapToList()` requiring explicit `Dispatchers.IO` context parameter
- Resolved type ambiguity in `SqlDelightArticleRepository` with explicit type casts
- Fixed `stripHtml()` return type to match non-null domain model
- Wired `RookNewsApplication.onCreate()` to set Android app context for DB init
- Final build: 97 tasks, BUILD SUCCESSFUL in 6s

### Phase 3 Implementation
- Removed Decompose dependency — replaced with direct Compose ViewModel pattern
- Created 6 ViewModels: `HomeViewModel`, `FeedsViewModel`, `BriefingsViewModel`, `ResearchViewModel`, `SettingsViewModel`, `ArticleDetailScreen` (inline state)
- Rewrote `App.kt` — `Scaffold` + `NavigationBar` with 5 tabs, full-screen push for article detail
- Built all 5 tab screens with loading/error/empty states
  - Home: article feed with `ArticleCard` (title, author, time, summary)
  - Feeds: sticky category filter chips + feed toggle rows
  - Briefings: FAB to generate, `BriefingCard` with content preview
  - Research: query input + result display with provider badge
  - Settings: reading mode radios, sync settings, AI info, about
- Built `ArticleDetailScreen` with AI summary toggle, bias indicator, topic chips
- Fixed `ChipDefaults` → `FilterChipDefaults` (Material 3 API compatibility)
- Added `@OptIn(ExperimentalMaterial3Api::class)` to all screens using `TopAppBar`
- Removed `kotlinx.datetime` dependency — replaced with simple timestamp formatting
- Final build: 97 tasks, BUILD SUCCESSFUL, 20MB APK

### Phase 4 Implementation (Partial)
- Created `AiStreamState` sealed interface (Idle, Loading, Streaming, Complete, Error)
- Updated `ModelCatalog` to Gemma 4 E2B (3.5GB, phone default) + E4B (5.4GB, tablet) with real GGUF URLs, 128K context, RAM gating
- Created `ModelManager` — download progress, activation, Okio local scan
- Implemented `CloudInferenceProvider` — real Gemini API (gemini-2.0-flash) with streaming
- Rewrote `AiProcessingPipeline` — 5 operations (summarize, bias, classify, briefing, research) + streaming variants
- Wired `DefaultAiRepository` to pipeline + model manager
- Updated `BriefingsViewModel` — AI briefing generation via FAB
- Updated `HomeViewModel` — on-demand article summarization
- Updated `ResearchViewModel` — deep research with result display
- Added `insertBriefing` to `BriefingRepository` interface + implementation
- Final build: 97 tasks, BUILD SUCCESSFUL, 20MB APK
