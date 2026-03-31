package com.gkk.mppsc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gkk.mppsc.ui.navigation.*
import com.gkk.mppsc.ui.screens.*
import com.gkk.mppsc.ui.theme.GKKThemeWrapper
import com.gkk.mppsc.ui.theme.gkkColors
import com.gkk.mppsc.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: MainViewModel = viewModel()
            val theme by vm.theme.collectAsStateWithLifecycle()

            GKKThemeWrapper(theme = theme) {
                MainApp(vm = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(vm: MainViewModel) {
    val nav = rememberNavController()
    val c   = gkkColors
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = c.sidebar,
                modifier = Modifier.width(300.dp)
            ) {
                GKKSidebar(
                    nav         = nav,
                    vm          = vm,
                    onItemClick = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("GKK MPPSC") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor    = c.navy,
                        titleContentColor = androidx.compose.ui.graphics.Color.White,
                        navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        ) { innerPadding ->
            GKKNavHost(
                nav      = nav,
                vm       = vm,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun GKKNavHost(
    nav:      NavHostController,
    vm:       MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = nav,
        startDestination = Route.DASHBOARD,
        modifier         = modifier
    ) {
        composable(Route.DASHBOARD)       { DashboardScreen() }
        composable(Route.SYLLABUS)        { SyllabusScreen(vm) }
        composable(Route.NOTES)           { NotesScreen(vm) }
        composable(Route.FLASHCARDS)      { FlashcardsScreen() }
        composable(Route.TEST)            { TestHomeScreen(vm, nav) }
        composable(Route.TEST_SESSION)    { TestSessionScreen(vm, nav) }
        composable(Route.TEST_RESULT)     { TestResultScreen(vm, nav) }
        composable(Route.DAILY)           { DailyScreen(vm, nav) }
        composable(Route.TIMED)           { TimedScreen(vm, nav) }
        composable(Route.PYQ)             { PYQScreen(vm, nav) }
        composable(Route.CURRENT_AFFAIRS) { CurrentAffairsScreen(vm) }
        composable(Route.BOOKMARKS)       { BookmarksScreen(vm) }
        composable(Route.WEAK_AREAS)      { WeakAreasScreen(vm) }
        composable(Route.PROGRESS)        { ProgressScreen(vm) }
        composable(Route.REVIEW)          { ReviewScreen(vm) }
        composable(Route.DONATE)          { DonateScreen(vm) }
        composable(Route.SETTINGS)        { SettingsScreen(vm) }
    }
}

@Composable
fun GKKSidebar(
    nav:         NavHostController,
    vm:          MainViewModel,
    onItemClick: () -> Unit
) {
    val c        = gkkColors
    val srsDue   by vm.srsDueCount.collectAsStateWithLifecycle()
    val streak   by vm.streak.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.sidebar)
            .padding(vertical = 16.dp)
    ) {
        // App header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                "GKK MPPSC",
                style = MaterialTheme.typography.titleLarge,
                color = androidx.compose.ui.graphics.Color.White
            )
            Text(
                "🔥 $streak day streak",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        HorizontalDivider(
            color     = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f),
            modifier  = Modifier.padding(vertical = 10.dp)
        )

        // Nav sections
        androidx.compose.foundation.lazy.LazyColumn {
            sidebarSections.forEach { section ->
                item {
                    Text(
                        section.title.uppercase(),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                section.items.forEach { item ->
                    item {
                        val badge = when (item.route) {
                            Route.REVIEW -> if (srsDue > 0) "$srsDue" else item.badgeText
                            else         -> item.badgeText
                        }
                        NavigationDrawerItem(
                            icon   = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
                                )
                            },
                            label  = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        item.label,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (item.isNew) {
                                            Surface(
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                                color = c.saff
                                            ) {
                                                Text(
                                                    "NEW",
                                                    style    = MaterialTheme.typography.labelSmall,
                                                    color    = androidx.compose.ui.graphics.Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (badge != null) {
                                            Surface(
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    badge,
                                                    style    = MaterialTheme.typography.labelSmall,
                                                    color    = androidx.compose.ui.graphics.Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            selected = false,
                            onClick  = {
                                nav.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                                onItemClick()
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                        )
                    }
                }
                item {
                    HorizontalDivider(
                        color    = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}
