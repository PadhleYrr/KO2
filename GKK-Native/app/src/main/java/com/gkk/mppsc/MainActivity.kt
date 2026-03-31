package com.gkk.mppsc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.gkk.mppsc.ui.navigation.*
import com.gkk.mppsc.ui.screens.*
import com.gkk.mppsc.ui.theme.*
import com.gkk.mppsc.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GKKApp() }
    }
}

@Composable
fun GKKApp() {
    val vm: MainViewModel = viewModel()
    val theme by vm.theme.collectAsStateWithLifecycle()

    GKKThemeWrapper(theme = theme) {
        val navController = rememberNavController()
        GKKScaffold(navController = navController, vm = vm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GKKScaffold(navController: NavHostController, vm: MainViewModel) {
    val colors      = gkkColors
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()
    val navBackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackEntry?.destination?.route ?: Route.DASHBOARD
    val streak by vm.streak.collectAsStateWithLifecycle()

    // Hide drawer on sub-screens (session, result)
    val showDrawer = currentRoute !in listOf(Route.TEST_SESSION, Route.TEST_RESULT)

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent   = {
            GKKSidebar(
                currentRoute = currentRoute,
                streak       = streak,
                colors       = colors,
                onNavigate   = { route ->
                    scope.launch { drawerState.close() }
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Route.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute !in listOf(Route.TEST_SESSION)) {
                    GKKTopBar(
                        route  = currentRoute,
                        colors = colors,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            },
            containerColor = colors.bg
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = Route.DASHBOARD,
                modifier         = Modifier.padding(innerPadding)
            ) {
                composable(Route.DASHBOARD)       { DashboardScreen(vm, navController) }
                composable(Route.SYLLABUS)        { SyllabusScreen(vm) }
                composable(Route.NOTES)           { NotesScreen(vm) }
                composable(Route.FLASHCARDS)      { FlashcardsScreen(vm) }
                composable(Route.TEST)            { TestHomeScreen(vm, navController) }
                composable(Route.TEST_SESSION)    { TestSessionScreen(vm, navController) }
                composable(Route.TEST_RESULT)     { TestResultScreen(vm, navController) }
                composable(Route.DAILY)           { DailyScreen(vm, navController) }
                composable(Route.TIMED)           { TimedScreen(vm, navController) }
                composable(Route.PYQ)             { PYQScreen(vm, navController) }
                composable(Route.CURRENT_AFFAIRS) { CurrentAffairsScreen(vm) }
                composable(Route.BOOKMARKS)       { BookmarksScreen(vm) }
                composable(Route.WEAK_AREAS)      { WeakAreasScreen(vm) }
                composable(Route.PROGRESS)        { ProgressScreen(vm) }
                composable(Route.REVIEW)          { ReviewScreen(vm) }
                composable(Route.DONATE)          { DonateScreen(vm) }
                composable(Route.SETTINGS)        { SettingsScreen(vm) }
            }
        }
    }
}

// ── Sidebar ─────────────────────────────────────────────────────────
@Composable
fun GKKSidebar(
    currentRoute: String,
    streak:       Int,
    colors:       GKKColors,
    onNavigate:   (String) -> Unit,
    onClose:      () -> Unit
) {
    ModalDrawerSheet(
        modifier            = Modifier.width(240.dp),
        drawerContainerColor= colors.sidebar
    ) {
        // Logo header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = "MP GK Portal",
                        fontFamily = Syne,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 17.sp,
                        color      = Color.White,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text      = "MPPSC 2026 — Complete Prep",
                        fontSize  = 11.sp,
                        color     = Color.White.copy(alpha = 0.45f),
                        modifier  = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(
                    onClick  = onClose,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // Nav sections — scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            sidebarSections.forEach { section ->
                // Section label
                Text(
                    text      = section.title.uppercase(),
                    fontSize  = 10.sp,
                    fontWeight= FontWeight.Bold,
                    color     = Color.White.copy(alpha = 0.3f),
                    letterSpacing = 1.4.sp,
                    modifier  = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 6.dp)
                )

                section.items.forEach { item ->
                    val isActive = currentRoute == item.route
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isActive) colors.saff else Color.Transparent
                            )
                            .clickable { onNavigate(item.route) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isActive) Color.White else Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.weight(1f)
                        )
                        // Badge
                        val badge = item.badgeText
                        if (badge != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isActive) Color.White.copy(alpha = 0.25f) else Color(0xFFF9A825)
                            ) {
                                Text(
                                    text = badge, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White else Color.Black,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (item.isNew) {
                            Spacer(Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF7C3AED)
                            ) {
                                Text(
                                    text = "NEW", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Streak pill at bottom
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔥", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "$streak",
                    fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp, color = Color(0xFFFFD54F)
                )
                Text("day streak", fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
            }
        }
    }
}

// ── Top bar ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GKKTopBar(
    route:       String,
    colors:      GKKColors,
    onMenuClick: () -> Unit
) {
    val titles = mapOf(
        Route.DASHBOARD       to Pair("Dashboard",       "Your complete study overview"),
        Route.SYLLABUS        to Pair("Full Syllabus",   "Paper 1 · Paper 2 · Paper 3 — Complete MPPSC Coverage"),
        Route.NOTES           to Pair("Notes",           "Chapter-wise study material"),
        Route.FLASHCARDS      to Pair("Flashcards",      "Swipe to remember faster"),
        Route.TEST            to Pair("MCQ Test",        "421 questions — 20 chapters"),
        Route.DAILY           to Pair("Daily 10",        "10 questions — builds habit"),
        Route.TIMED           to Pair("Timed Mock",      "Real exam simulation"),
        Route.PYQ             to Pair("PYQ Papers",      "2021–2024 previous year papers"),
        Route.CURRENT_AFFAIRS to Pair("Current Affairs", "Daily updates — National · MP · International"),
        Route.BOOKMARKS       to Pair("Bookmarks",       "Your saved questions"),
        Route.WEAK_AREAS      to Pair("Weak Areas",      "Focus on what needs work"),
        Route.PROGRESS        to Pair("My Progress",     "Track your performance"),
        Route.REVIEW          to Pair("Smart Review",    "Spaced repetition — due today"),
        Route.DONATE          to Pair("Support Us",      "Help keep this app free"),
        Route.SETTINGS        to Pair("Settings",        "Language, notifications, and more"),
        Route.TEST_RESULT     to Pair("Result",          "How did you do?")
    )
    val (title, sub) = titles[route] ?: Pair("GKK MPPSC", "")

    TopAppBar(
        title = {
            Column {
                Text(
                    text       = title,
                    fontFamily = Syne,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 22.sp,
                    color      = colors.text
                )
                if (sub.isNotEmpty()) {
                    Text(text = sub, fontSize = 13.sp, color = colors.muted)
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colors.text)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.bg)
    )
}
