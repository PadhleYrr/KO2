package com.gkk.mppsc.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// ── Route strings ────────────────────────────────────────────────────
object Route {
    const val DASHBOARD      = "dashboard"
    const val SYLLABUS       = "syllabus"
    const val NOTES          = "notes"
    const val FLASHCARDS     = "flashcards"
    const val TEST           = "test"
    const val TEST_SESSION   = "test_session"     // active quiz
    const val TEST_RESULT    = "test_result"      // result screen
    const val DAILY          = "daily"
    const val TIMED          = "timed"
    const val PYQ            = "pyq"
    const val CURRENT_AFFAIRS= "currentaffairs"
    const val BOOKMARKS      = "bookmarkspage"
    const val WEAK_AREAS     = "weakareas"
    const val PROGRESS       = "progress"
    const val REVIEW         = "review"
    const val DONATE         = "donate"
    const val SETTINGS       = "settings"
}

// ── Sidebar nav items (same order as your HTML sidebar) ──────────────
sealed class NavItem(
    val route:        String,
    val label:        String,
    val icon:         ImageVector,
    val badgeText:    String? = null,
    val isNew:        Boolean = false
) {
    // Study section
    object Dashboard     : NavItem(Route.DASHBOARD,       "Dashboard",       Icons.Default.Dashboard)
    object Syllabus      : NavItem(Route.SYLLABUS,        "Full Syllabus",   Icons.Default.MenuBook,    isNew = true)
    object Notes         : NavItem(Route.NOTES,           "Notes",           Icons.Default.Book,        badgeText = "15")
    object Flashcards    : NavItem(Route.FLASHCARDS,      "Flashcards",      Icons.Default.Style)

    // Practice section
    object Test          : NavItem(Route.TEST,            "MCQ Test",        Icons.Default.Edit,        badgeText = "421")
    object Daily         : NavItem(Route.DAILY,           "Daily 10",        Icons.Default.CalendarToday)
    object Timed         : NavItem(Route.TIMED,           "Timed Mock",      Icons.Default.Timer)

    // More section
    object PYQ           : NavItem(Route.PYQ,             "PYQ Papers",      Icons.Default.Article,     badgeText = "4yr")
    object CurrentAffairs: NavItem(Route.CURRENT_AFFAIRS, "Current Affairs", Icons.Default.Newspaper,   isNew = true)
    object Bookmarks     : NavItem(Route.BOOKMARKS,       "Bookmarks",       Icons.Default.Bookmark)

    // Progress section
    object WeakAreas     : NavItem(Route.WEAK_AREAS,      "Weak Areas",      Icons.Default.TrendingDown)
    object Progress      : NavItem(Route.PROGRESS,        "My Progress",     Icons.Default.TrendingUp)

    // Other section
    object Donate        : NavItem(Route.DONATE,          "Support Us",      Icons.Default.Favorite)
    object Settings      : NavItem(Route.SETTINGS,        "Settings",        Icons.Default.Settings,    isNew = true)
}

// ── Grouped sidebar sections matching your HTML nav-section dividers ──
data class NavSection(val title: String, val items: List<NavItem>)

val sidebarSections = listOf(
    NavSection("Study", listOf(
        NavItem.Dashboard,
        NavItem.Syllabus,
        NavItem.Notes,
        NavItem.Flashcards
    )),
    NavSection("Practice", listOf(
        NavItem.Test,
        NavItem.Daily,
        NavItem.Timed
    )),
    NavSection("More", listOf(
        NavItem.PYQ,
        NavItem.CurrentAffairs,
        NavItem.Bookmarks
    )),
    NavSection("Progress", listOf(
        NavItem.WeakAreas,
        NavItem.Progress
    )),
    NavSection("Other", listOf(
        NavItem.Donate,
        NavItem.Settings
    ))
)
