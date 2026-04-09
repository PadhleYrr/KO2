# GKK MPPSC — Native Android App

A full native **Kotlin + Jetpack Compose** Android app for MPPSC (Madhya Pradesh Public Service Commission) exam preparation. Built by [PadhleYrr](https://padhleyrr.com).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.8.3 |
| State Management | ViewModel + StateFlow |
| Local Storage | DataStore Preferences |
| Backend | Firebase Auth + Firestore |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Image Loading | Coil 2.7.0 (Community screen) |
| Build System | Gradle 8.7 + Version Catalog |
| Min SDK | Android 7.0 (API 24) |
| Target SDK | Android 15 (API 35) |
| Package Name | `com.padhleyrr.mppsc` |

---

## Features

### 📚 Study
- **Dashboard** — Stats overview (attempted, correct, accuracy, streak), chapter progress, activity heatmap, weak areas summary, and quick-nav cards
- **Full Syllabus** — Complete MPPSC Paper I, II, III syllabus browser
- **Notes** — 15 chapters of HTML-formatted study notes with full-screen reader
- **Flashcards** — Category-filtered swipe flashcard deck

### 🎯 Practice
- **MCQ Test** — 421+ questions, filterable by category; supports custom and full-test modes
- **Daily 10** — Randomised 10-question daily practice session
- **Timed Mock** — Countdown-based mock test with auto-submit
- **PYQ Papers** — Previous Year Question papers (2021–2024); tap any paper to start a timed session

### 🌐 More
- **Current Affairs** — 12 curated current affairs items loaded from bundled JSON
- **Community** — Reddit-style discussion board with feed, post detail, comments, likes, and a global live chat tab; backed by Firestore in real-time
- **MP Atlas Map** — Interactive Madhya Pradesh geography map loaded from a local `maps.html` WebView asset
- **Bookmarks** — Save/unsave questions across all modes; bulk-clear support

### 📈 Progress
- **Weak Areas** — Auto-detected bottom-10 chapters by accuracy
- **My Progress** — Cumulative stats, activity heatmap (GitHub-style), per-chapter accuracy bars
- **SRS Review** — Spaced-repetition system; incorrect answers are queued and re-served on a 1→3→7→14→30-day schedule

### ⚙️ Other
- **Settings** — Choose from 10 built-in themes, toggle push notifications
- **Profile** — View account info, subscription status, trial countdown, coupon redemption
- **Subscription** — ₹100 / 6 months premium; UPI payment flow with admin verification; 7-day free trial for new users
- **Admin Panel** — (Admin-only) Accessible when email is listed in `config/admins` in Firestore

---

## Subscription Model

| Tier | Access |
|---|---|
| **Trial** | Full access for 7 days from registration |
| **Premium** | ₹100 / 6 months — unlocked via coupon code or UPI payment intent |
| **Admin** | Full access; email must be in Firestore `config/admins.emails[]` |

New users get a 7-day free trial automatically on first sign-in. After trial expiry, a paywall screen is shown with UPI payment instructions and a coupon redemption field.

---

## Project Structure

```
KO2-main/
├── .github/workflows/
│   └── build-release.yml           GitHub Actions CI — builds & signs release APK
├── app/
│   ├── build.gradle.kts            App-level Gradle config (compileSdk 35, minSdk 24)
│   ├── google-services.json        Firebase project config (project: mpgk-9496d)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/data/
│       │   ├── questions.json      421+ MCQ questions with categories & explanations
│       │   ├── pyq.json            PYQ papers 2021–2024
│       │   ├── notes.json          15 chapters of HTML study notes
│       │   ├── syllabus.json       3-paper MPPSC syllabus
│       │   └── current_affairs.json 12 current affairs items
│       ├── assets/maps.html        Interactive MP geography map (WebView)
│       ├── res/
│       │   ├── font/               DM Sans (regular, medium, semibold) + Syne (semibold, bold, extrabold)
│       │   └── values/             colors.xml, strings.xml, themes.xml
│       └── java/com/padhleyrr/mppsc/
│           ├── GKKApp.kt           Application class — initialises Firebase
│           ├── MainActivity.kt     Entry point; hosts Scaffold, top bar, sidebar drawer, NavHost
│           ├── data/
│           │   ├── models/         Question, PYQPaper, Note, Bookmark, SrsEntry,
│           │   │                   CurrentAffair, SyllabusPaper, AppState,
│           │   │                   CommunityPost, ChatMessage, Comment
│           │   └── repository/
│           │       ├── AuthRepository.kt        Firebase Auth (sign-in, sign-up, reset password)
│           │       ├── ContentRepository.kt     Loads & parses bundled JSON assets (offline-first)
│           │       ├── PrefsRepository.kt       DataStore — streak, heatmap, stats, bookmarks, SRS
│           │       ├── SubscriptionRepository.kt Firestore subscription/trial/admin state (singleton)
│           │       └── CommunityRepository.kt   Firestore real-time community posts & global chat
│           ├── ui/
│           │   ├── components/GKKComponents.kt  Shared reusable Compose components
│           │   ├── navigation/
│           │   │   ├── NavGraph.kt              Route constants, NavItem sealed class, sidebarSections
│           │   │   └── AuthNavGraph.kt          Auth flow navigation graph
│           │   ├── screens/
│           │   │   ├── auth/
│           │   │   │   ├── LoginScreen.kt
│           │   │   │   ├── SignUpScreen.kt
│           │   │   │   └── ForgotPasswordScreen.kt
│           │   │   ├── DashboardScreen.kt
│           │   │   ├── NotesScreen.kt
│           │   │   ├── FlashcardsScreen.kt
│           │   │   ├── TestScreens.kt           TestHomeScreen, TestSessionScreen, TestResultScreen
│           │   │   ├── MapScreen.kt
│           │   │   ├── CommunityScreen.kt       Feed + Post Detail + Global Chat tabs
│           │   │   ├── ProfileScreen.kt
│           │   │   ├── SubscriptionScreen.kt
│           │   │   ├── AdminScreen.kt
│           │   │   └── RemainingScreens.kt      PYQ, CurrentAffairs, Bookmarks, WeakAreas,
│           │   │                                Progress, Review, Donate, Settings, Syllabus
│           │   └── theme/
│           │       ├── AppTheme.kt              10 GKKTheme colour palettes
│           │       ├── GKKThemeWrapper.kt       Compose theme provider; exposes gkkColors
│           │       └── Typography.kt            DM Sans + Syne font families
│           ├── utils/
│           │   └── GKKMessagingService.kt       FCM push notification handler
│           └── viewmodel/
│               ├── MainViewModel.kt             All app state: content, quiz session, bookmarks,
│               │                               SRS, streak, heatmap, weak areas, subscription
│               ├── AuthViewModel.kt             Auth UI state + sign-in/sign-up/reset logic
│               └── CommunityViewModel.kt        Community posts & chat state
├── firestore.rules                 Firestore security rules
├── gradle/
│   ├── libs.versions.toml          Version catalog for all dependencies
│   └── wrapper/gradle-wrapper.properties
└── build.gradle.kts                Root Gradle config
```

---

## Screens (25 total)

| Screen | Route | File |
|---|---|---|
| Login | *(auth flow)* | `auth/LoginScreen.kt` |
| Sign Up | *(auth flow)* | `auth/SignUpScreen.kt` |
| Forgot Password | *(auth flow)* | `auth/ForgotPasswordScreen.kt` |
| Dashboard | `dashboard` | `DashboardScreen.kt` |
| Full Syllabus | `syllabus` | `RemainingScreens.kt` |
| Notes | `notes` | `NotesScreen.kt` |
| Flashcards | `flashcards` | `FlashcardsScreen.kt` |
| MCQ Test Home | `test` | `TestScreens.kt` |
| Test Session | `test_session` | `TestScreens.kt` |
| Test Result | `test_result` | `TestScreens.kt` |
| Daily 10 | `daily` | `RemainingScreens.kt` |
| Timed Mock | `timed` | `RemainingScreens.kt` |
| PYQ Papers | `pyq` | `RemainingScreens.kt` |
| Current Affairs | `currentaffairs` | `RemainingScreens.kt` |
| Community | `community` | `CommunityScreen.kt` |
| MP Atlas Map | `map` | `MapScreen.kt` |
| Bookmarks | `bookmarkspage` | `RemainingScreens.kt` |
| Weak Areas | `weakareas` | `RemainingScreens.kt` |
| My Progress | `progress` | `RemainingScreens.kt` |
| SRS Review | `review` | `RemainingScreens.kt` |
| Donate / Support | `donate` | `RemainingScreens.kt` |
| Settings | `settings` | `RemainingScreens.kt` |
| Profile | `profile` | `ProfileScreen.kt` |
| Subscription / Paywall | `subscription` | `SubscriptionScreen.kt` |
| Admin Panel | `admin` | `AdminScreen.kt` |

---

## Themes (10 built-in)

| Theme ID | Name | Emoji |
|---|---|---|
| `default` | Classic Blue | 🔵 |
| `dark` | Dark Night | 🌙 |
| `amoled` | AMOLED Black | ⚫ |
| `emerald` | Emerald Forest | 🌿 |
| `ocean` | Ocean Breeze | 🌊 |
| `purple` | Royal Purple | 💜 |
| `rose` | Rose Petal | 🌸 |
| `sunset` | Sunset Warm | 🌅 |
| `mint` | Mint Fresh | 🍃 |
| `saffron` | Saffron India | 🇮🇳 |

Theme choice is persisted via DataStore and applied on every launch.

---

## Setup

### 1. Clone & open in Android Studio

```bash
git clone https://github.com/PadhleYrr/KO2.git
```

Open in **Android Studio Ladybug (2024.2)** or newer.

### 2. Add your `google-services.json`

- Go to [Firebase Console](https://console.firebase.google.com)
- Select or create your project
- Add an Android app with package name `com.padhleyrr.mppsc`
- Download `google-services.json` → place it at `app/google-services.json`

### 3. Set up Firestore

Deploy the included `firestore.rules` to your Firebase project:

```bash
firebase deploy --only firestore:rules
```

Create the following Firestore documents manually or via the console:

- `config/admins` → `{ "emails": ["your@email.com"] }` — admin access list
- `config/coupons` → `{ "codes": [] }` — coupon redemption list

### 4. Fonts (already included)

Fonts are bundled in `app/src/main/res/font/`:
- **DM Sans** — `dmsans_regular.ttf`, `dmsans_medium.ttf`, `dmsans_semibold.ttf`
- **Syne** — `syne_semibold.ttf`, `syne_bold.ttf`, `syne_extrabold.ttf`

### 5. Build debug APK

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/`.

---

## GitHub Actions — Signed Release APK

The workflow in `.github/workflows/build-release.yml` builds and optionally signs a release APK on every push to `main`, on version tags (`v*`), or via manual trigger.

### Required Secrets (Settings → Secrets → Actions)

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | Your keystore encoded as base64 |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

If no keystore secrets are provided, the workflow still builds an unsigned APK.

### Generate a keystore

```bash
keytool -genkey -v \
  -keystore release.jks \
  -alias gkk-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Encode keystore to base64

```bash
# macOS
base64 -i release.jks | pbcopy

# Linux
base64 release.jks   # copy the output
```

### Trigger a build

- **Automatic**: push to `main`
- **Manual**: GitHub → Actions → Build Release APK → Run workflow
- **Release with APK attached**: push a tag like `v1.0.0`

---

## Firestore Data Model

| Collection | Description |
|---|---|
| `users/{uid}` | Per-user trial start, premium expiry, name, email, banned flag |
| `communityPosts/{postId}` | Community feed posts; subcollection `comments/{commentId}` |
| `globalChat/{msgId}` | Immutable global chat messages |
| `config/admins` | `emails[]` — admin whitelist |
| `config/coupons` | `codes[]` — coupon objects with `code`, `days`, `usedBy` |
| `emailIndex/{key}` | Email → UID lookup index |
| `paymentIntents/{id}` | Payment intent records logged when user taps "I've Paid" |

---

## Content Data (bundled offline)

All study content ships with the APK as JSON assets — no network required to study.

| File | Contents |
|---|---|
| `questions.json` | 421+ MCQ questions (category, question, 4 options, answer index, explanation) |
| `pyq.json` | PYQ papers 2021–2024 (year, paper name, questions array) |
| `notes.json` | 15 chapters (title, HTML body) |
| `syllabus.json` | 3 papers with topic/subtopic tree |
| `current_affairs.json` | 12 current affairs items |
| `maps.html` | Interactive MP geography atlas (WebView) |
