package com.padhleyrr.mppsc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.viewModels
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.padhleyrr.mppsc.data.repository.AuthRepository
import com.padhleyrr.mppsc.ui.navigation.AuthNavGraph
import com.padhleyrr.mppsc.ui.navigation.NavItem
import com.padhleyrr.mppsc.ui.navigation.Route
import com.padhleyrr.mppsc.ui.navigation.sidebarSections
import com.padhleyrr.mppsc.ui.screens.*
import com.padhleyrr.mppsc.ui.theme.DMSans
import com.padhleyrr.mppsc.ui.theme.GKKThemeWrapper
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors
import com.padhleyrr.mppsc.viewmodel.AuthViewModel
import com.padhleyrr.mppsc.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val authRepository by lazy { AuthRepository() }
    private val authViewModel  by lazy { AuthViewModel(authRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by mainViewModel.theme.collectAsStateWithLifecycle()
            GKKThemeWrapper(theme = theme) {
                RootNavigation(
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
fun RootNavigation(authViewModel: AuthViewModel, mainViewModel: MainViewModel) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    if (authState.isAuthenticated) {
        MainAppNavigation(mainViewModel = mainViewModel)
    } else {
        val authNavController = rememberNavController()
        AuthNavGraph(
            navController = authNavController,
            authViewModel = authViewModel,
            onAuthSuccess = {}
        )
    }
}

@Composable
fun MainAppNavigation(mainViewModel: MainViewModel) {
    val navController  = rememberNavController()
    val drawerState    = rememberDrawerState(DrawerValue.Closed)
    val scope          = rememberCoroutineScope()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backstackEntry?.destination?.route ?: Route.DASHBOARD
    val streak         by mainViewModel.streak.collectAsStateWithLifecycle()
    val srsDue         by mainViewModel.srsDueCount.collectAsStateWithLifecycle()

    val pageTitle = sidebarSections
        .flatMap { it.items }
        .find { it.route == currentRoute }?.label ?: "Dashboard"

    val pageSubtitle = when (currentRoute) {
        Route.DASHBOARD      -> "Your complete study overview"
        Route.NOTES          -> "Read & revise your notes"
        Route.FLASHCARDS     -> "Quick revision cards"
        Route.TEST           -> "Practice MCQ questions"
        Route.DAILY          -> "10 questions a day"
        Route.TIMED          -> "Timed mock test"
        Route.PYQ            -> "Previous year papers"
        Route.CURRENT_AFFAIRS-> "Stay updated"
        Route.BOOKMARKS      -> "Your saved questions"
        Route.WEAK_AREAS     -> "Topics to improve"
        Route.PROGRESS       -> "Track your journey"
        Route.REVIEW         -> "Spaced repetition review"
        Route.SYLLABUS       -> "Complete MPPSC syllabus"
        Route.SETTINGS       -> "Preferences & themes"
        Route.DONATE         -> "Support the app"
        else                 -> "GKK MPPSC"
    }

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = true,
        drawerContent   = {
            GKKSidebar(
                currentRoute = currentRoute,
                streak       = streak,
                srsDue       = srsDue,
                onNavigate   = { route ->
                    scope.launch { drawerState.close() }
                    if (currentRoute != route) {
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
        // No Scaffold/TopAppBar — topbar is part of page content just like the web
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gkkColors.bg)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            GKKTopBar(
                title       = pageTitle,
                subtitle    = pageSubtitle,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            AppNavHost(
                navController = navController,
                vm            = mainViewModel,
                modifier      = Modifier.weight(1f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TOP BAR — matches web exactly:
//  bg colour = page bg (not navy), Syne title, DM Sans subtitle,
//  search box on right, hamburger on left
// ══════════════════════════════════════════════════════════════
@Composable
fun GKKTopBar(
    title:       String,
    subtitle:    String,
    onMenuClick: () -> Unit
) {
    val c = gkkColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: hamburger + title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Hamburger ☰
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Text("☰", fontSize = 22.sp, color = c.text)
            }

            // Title + subtitle
            Column {
                Text(
                    text          = title,
                    fontFamily    = Syne,
                    fontWeight    = FontWeight.ExtraBold,
                    fontSize      = 22.sp,
                    color         = c.text,
                    maxLines      = 1
                )
                Text(
                    text       = subtitle,
                    fontFamily = DMSans,
                    fontSize   = 13.sp,
                    color      = c.muted,
                    modifier   = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Right: search box
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(c.card)
                .border(1.dp, c.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🔍", fontSize = 13.sp)
            Text(
                "Search...",
                fontFamily = DMSans,
                fontSize   = 13.sp,
                color      = c.muted
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  SIDEBAR
// ══════════════════════════════════════════════════════════════
@Composable
fun GKKSidebar(
    currentRoute: String,
    streak:       Int,
    srsDue:       Int,
    onNavigate:   (String) -> Unit,
    onClose:      () -> Unit
) {
    val c = gkkColors
    ModalDrawerSheet(
        drawerContainerColor = c.sidebar,
        modifier             = Modifier
            .width(240.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 14.dp, top = 22.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "GKK MPPSC",
                        fontFamily    = Syne,
                        fontWeight    = FontWeight.ExtraBold,
                        fontSize      = 17.sp,
                        color         = Color.White,
                        letterSpacing = (-0.3f).sp
                    )
                    Text(
                        "padhleyrr.com",
                        fontFamily = DMSans,
                        fontSize   = 11.sp,
                        color      = Color.White.copy(alpha = 0.45f),
                        modifier   = Modifier.padding(top = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontSize = 14.sp, color = Color.White)
                }
            }

            HorizontalDivider(
                color    = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            sidebarSections.forEach { section ->
                Text(
                    section.title.uppercase(),
                    fontFamily    = DMSans,
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White.copy(alpha = 0.3f),
                    letterSpacing = 1.4.sp,
                    modifier      = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 6.dp)
                )
                section.items.forEach { item ->
                    SidebarNavItem(
                        item       = item,
                        isActive   = currentRoute == item.route,
                        srsDue     = if (item.route == Route.REVIEW) srsDue else null,
                        onNavigate = onNavigate
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(
                color    = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🔥", fontSize = 20.sp)
                Column {
                    Text(
                        "$streak",
                        fontFamily = Syne,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp,
                        color      = Color(0xFFFFD54F)
                    )
                    Text(
                        "day streak",
                        fontFamily = DMSans,
                        fontSize   = 10.sp,
                        color      = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SidebarNavItem(
    item:       NavItem,
    isActive:   Boolean,
    srsDue:     Int?,
    onNavigate: (String) -> Unit
) {
    val c = gkkColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) c.saff else Color.Transparent)
            .clickable { onNavigate(item.route) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector        = item.icon,
            contentDescription = item.label,
            tint               = if (isActive) Color.White else Color.White.copy(alpha = 0.65f),
            modifier           = Modifier.size(18.dp)
        )
        Text(
            item.label,
            fontFamily = DMSans,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isActive) Color.White else Color.White.copy(alpha = 0.65f),
            modifier   = Modifier.weight(1f)
        )
        val badgeText = when {
            srsDue != null && srsDue > 0 -> "$srsDue"
            item.isNew                   -> "NEW"
            item.badgeText != null       -> item.badgeText
            else                         -> null
        }
        if (badgeText != null) {
            val isUrgent = item.isNew || (srsDue != null && srsDue > 0)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isUrgent) Color(0xFFDC2626) else Color(0xFFF9A825))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    badgeText,
                    fontFamily = DMSans,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (isUrgent) Color.White else Color.Black
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  NAV HOST
// ══════════════════════════════════════════════════════════════
@Composable
fun AppNavHost(navController: NavHostController, vm: MainViewModel, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Route.DASHBOARD, modifier = modifier) {
        composable(Route.DASHBOARD)       { DashboardScreen(vm = vm, nav = navController) }
        composable(Route.TEST)            { TestHomeScreen(vm = vm, nav = navController) }
        composable(Route.TEST_SESSION)    { TestSessionScreen(vm = vm, nav = navController) }
        composable(Route.TEST_RESULT)     { TestResultScreen(vm = vm, nav = navController) }
        composable(Route.DAILY)           { DailyScreen(vm = vm, nav = navController) }
        composable(Route.TIMED)           { TimedScreen(vm = vm, nav = navController) }
        composable(Route.NOTES)           { NotesScreen(vm = vm) }
        composable(Route.FLASHCARDS)      { FlashcardsScreen(vm = vm) }
        composable(Route.PYQ)             { PYQScreen(vm = vm, nav = navController) }
        composable(Route.CURRENT_AFFAIRS) { CurrentAffairsScreen(vm = vm) }
        composable(Route.BOOKMARKS)       { BookmarksScreen(vm = vm) }
        composable(Route.WEAK_AREAS)      { WeakAreasScreen(vm = vm) }
        composable(Route.PROGRESS)        { ProgressScreen(vm = vm) }
        composable(Route.REVIEW)          { ReviewScreen(vm = vm) }
        composable(Route.DONATE)          { DonateScreen(vm = vm) }
        composable(Route.SETTINGS)        { SettingsScreen(vm = vm) }
        composable(Route.SYLLABUS)        { SyllabusScreen(vm = vm) }
    }
}
