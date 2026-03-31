# GKK MPPSC — Native Android App

Full native Kotlin + Jetpack Compose rewrite of the GKK MPPSC prep app.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose 2.8 |
| State | ViewModel + StateFlow |
| Storage | DataStore Preferences (replaces localStorage) |
| Backend | Firebase Auth + Firestore |
| Push | Firebase Cloud Messaging |
| Build | Gradle 8.7 with Version Catalog |
| Min SDK | Android 8.0 (API 26) — 95%+ devices |

## Project Structure

```
app/src/main/
├── java/com/gkk/mppsc/
│   ├── GKKApp.kt                    Application class
│   ├── MainActivity.kt              Entry point + Scaffold + Sidebar
│   ├── data/
│   │   ├── models/                  Question, PYQPaper, Note, Bookmark, etc.
│   │   └── repository/
│   │       ├── ContentRepository    Loads JSON assets (offline-first)
│   │       └── PrefsRepository      DataStore — streak, stats, bookmarks, SRS
│   ├── ui/
│   │   ├── navigation/NavGraph      Route constants + sidebar sections
│   │   ├── theme/
│   │   │   ├── AppTheme             10 themes (exact CSS colour match)
│   │   │   ├── Typography           DM Sans + Syne fonts
│   │   │   └── GKKThemeWrapper      Compose theme provider
│   │   ├── screens/                 17 screens (built session by session)
│   │   └── components/              Shared UI components
│   ├── viewmodel/MainViewModel      Single ViewModel for all app state
│   └── utils/GKKMessagingService    FCM push notifications
└── assets/data/
    ├── questions.json               392 MCQ questions
    ├── pyq.json                     5 PYQ papers (2021–2024)
    ├── notes.json                   15 chapters HTML notes
    ├── syllabus.json                3 papers full syllabus
    └── current_affairs.json         12 current affairs items
```

## Setup

### 1. Clone & open in Android Studio
```bash
git clone https://github.com/YOUR_USERNAME/gkk-mppsc-native.git
```
Open in **Android Studio Ladybug (2024.2)** or newer.

### 2. Add your google-services.json
- Go to [Firebase Console](https://console.firebase.google.com)
- Select your project `mpgk-9496d`
- Add Android app with package name `com.gkk.mppsc`
- Download `google-services.json` → replace `app/google-services.json`

### 3. Add fonts
Download from Google Fonts and place in `app/src/main/res/font/`:
- [DM Sans](https://fonts.google.com/specimen/DM+Sans): `dmsans_regular.ttf`, `dmsans_medium.ttf`, `dmsans_semibold.ttf`
- [Syne](https://fonts.google.com/specimen/Syne): `syne_semibold.ttf`, `syne_bold.ttf`, `syne_extrabold.ttf`

### 4. Add launcher icons
Place your icon files in `app/src/main/res/`:
- `mipmap-hdpi/ic_launcher.png` (72×72)
- `mipmap-xhdpi/ic_launcher.png` (96×96)
- `mipmap-xxhdpi/ic_launcher.png` (144×144)
- `mipmap-xxxhdpi/ic_launcher.png` (192×192)
- Same for `ic_launcher_round.png`

### 5. Build debug APK
```bash
./gradlew assembleDebug
```

---

## GitHub Actions — Signed Release APK

### Required Secrets (Settings → Secrets → Actions)

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | Your keystore file encoded as base64 |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

### Generate a keystore
```bash
keytool -genkey -v \
  -keystore release.jks \
  -alias gkk-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Encode keystore to base64 (for GitHub secret)
```bash
# macOS / Linux
base64 -i release.jks | pbcopy    # copies to clipboard on Mac
base64 release.jks                # prints on Linux — copy output
```

### Trigger a build
- **Automatic**: Push to `main` branch
- **Manual**: GitHub → Actions → Build Release APK → Run workflow
- **Release**: Push a tag like `v1.0.0` → creates a GitHub Release with APK attached

---

## Screens (17 total)

| Screen | Route | Status |
|---|---|---|
| Dashboard | `dashboard` | 🔨 Building |
| Full Syllabus | `syllabus` | 🔨 Building |
| Notes | `notes` | 🔨 Building |
| Flashcards | `flashcards` | 🔨 Building |
| MCQ Test Home | `test` | 🔨 Building |
| Test Session | `test_session` | 🔨 Building |
| Test Result | `test_result` | 🔨 Building |
| Daily 10 | `daily` | 🔨 Building |
| Timed Mock | `timed` | 🔨 Building |
| PYQ Papers | `pyq` | 🔨 Building |
| Current Affairs | `currentaffairs` | 🔨 Building |
| Bookmarks | `bookmarkspage` | 🔨 Building |
| Weak Areas | `weakareas` | 🔨 Building |
| My Progress | `progress` | 🔨 Building |
| Smart Review (SRS) | `review` | 🔨 Building |
| Support Us | `donate` | 🔨 Building |
| Settings | `settings` | 🔨 Building |
