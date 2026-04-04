package com.padhleyrr.mppsc.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.padhleyrr.mppsc.data.models.TestMode
import com.padhleyrr.mppsc.ui.components.*
import com.padhleyrr.mppsc.ui.navigation.Route
import com.padhleyrr.mppsc.ui.theme.GKKTheme
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColorsFor
import com.padhleyrr.mppsc.ui.theme.gkkColors
import com.padhleyrr.mppsc.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

// ═══════════════════════════════════════════════
//  DAILY 10
// ═══════════════════════════════════════════════
@Composable
fun DailyScreen(vm: MainViewModel, nav: NavHostController) {
    val c      = gkkColors
    val streak by vm.streak.collectAsStateWithLifecycle()
    val today  = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    Column(
        modifier          = Modifier.fillMaxSize().background(c.bg).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GKKCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("📅", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Daily 10", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp, color = c.text)
                Text(today, fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF8E1))
                        .padding(16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🔥", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("$streak", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp, color = Color(0xFFE65100))
                        Text("day streak", fontSize = 12.sp, color = c.muted)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("10 random questions across all chapters.\nBuilds daily habit and boosts retention.",
                    fontSize = 13.sp, color = c.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp)
                Spacer(Modifier.height(24.dp))
                GKKButton("Start Daily 10 →", onClick = {
                    vm.startTest(TestMode.DAILY, emptyList())
                    nav.navigate(Route.TEST_SESSION)
                })
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  TIMED MOCK
// ═══════════════════════════════════════════════
@Composable
fun TimedScreen(vm: MainViewModel, nav: NavHostController) {
    val c = gkkColors
    var selectedMode by remember { mutableStateOf(0) }
    val modes = listOf(
        Triple("⚡", "Quick Mock",   "25 Qs · 30 min"),
        Triple("📋", "Full Mock",    "100 Qs · 90 min"),
        Triple("🎯", "MPPSC Sim",   "100 Qs · 120 min")
    )
    val testModes = listOf(TestMode.QUICK, TestMode.LONG, TestMode.LONG)

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        GKKCard {
            SectionHeader("Timed Mock Test")
            Text("Simulate real exam conditions with a countdown timer.",
                fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(bottom = 14.dp))
            modes.forEachIndexed { idx, (emoji, title, desc) ->
                ModeCard(
                    emoji    = emoji, title = title, desc = desc,
                    selected = selectedMode == idx,
                    onClick  = { selectedMode = idx },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            GKKButton("Start Timed Test →", onClick = {
                vm.startTest(testModes[selectedMode], emptyList())
                nav.navigate(Route.TEST_SESSION)
            })
        }
    }
}

// ═══════════════════════════════════════════════
//  PYQ PAPERS
// ═══════════════════════════════════════════════
@Composable
fun PYQScreen(vm: MainViewModel, nav: NavHostController) {
    val c      = gkkColors
    val papers by vm.pyqPapers.collectAsStateWithLifecycle()
    var selectedYear by remember { mutableStateOf("all") }
    val years  = listOf("all") + papers.map { it.year }.distinct().sortedDescending()

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Year tabs
        ScrollableTabRow(
            selectedTabIndex  = years.indexOf(selectedYear).coerceAtLeast(0),
            containerColor    = Color.Transparent,
            contentColor      = c.navy,
            edgePadding       = 0.dp,
            modifier          = Modifier.padding(bottom = 14.dp)
        ) {
            years.forEach { year ->
                Tab(
                    selected = selectedYear == year,
                    onClick  = { selectedYear = year },
                    text     = { Text(if (year == "all") "All Years" else year,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        val filtered = if (selectedYear == "all") papers
                       else papers.filter { it.year == selectedYear }

        filtered.forEach { paper ->
            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.card)
                    .border(1.dp, c.border, RoundedCornerShape(14.dp))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEEF0FF)),
                    contentAlignment = Alignment.Center
                ) { Text("📄", fontSize = 22.sp) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(paper.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                    Text("${paper.questions.size} questions · ${paper.duration}",
                        fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 3.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .clickable {
                                vm.startPYQTest(paper)
                                nav.navigate(Route.TEST_SESSION)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text("Practice", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.saff) }
                    if (paper.pdfUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEEF0FF))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paper.pdfUrl))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("⬇ PDF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.navy) }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  CURRENT AFFAIRS
// ═══════════════════════════════════════════════
@Composable
fun CurrentAffairsScreen(vm: MainViewModel) {
    val c  = gkkColors
    val ca by vm.currentAffairs.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("all") }

    val tags = listOf("all" to "All", "national" to "🏛 National",
                      "mp" to "🏙 MP Special", "international" to "🌍 International")

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Header banner
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFFE65100), Color(0xFFFF6D00))
                    )
                ).padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📰", fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Current Affairs", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp, color = Color.White)
                    Text("March 2026 · National · MP · International",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Filter tabs — horizontally scrollable so long labels never wrap
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { (tag, label) ->
                FilterChip(label, filter == tag) { filter = tag }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Items
        val filtered = if (filter == "all") ca else ca.filter { it.tag == filter }
        filtered.forEach { item ->
            val (tagBg, tagText) = when (item.tag) {
                "national"      -> Color(0xFFEEF2FF) to Color(0xFF3730A3)
                "mp"            -> Color(0xFFFFF7ED) to Color(0xFFC2410C)
                "international" -> Color(0xFFF0FDF4) to Color(0xFF166534)
                else            -> Color(0xFFEEF2FF) to Color(0xFF3730A3)
            }
            val tagLabel = when (item.tag) {
                "national"      -> "🏛 National"
                "mp"            -> "🏙 MP Special"
                "international" -> "🌍 International"
                else            -> item.tag
            }
            GKKCard(modifier = Modifier.padding(bottom = 10.dp)) {
                GKKBadge(tagLabel, tagBg, tagText)
                Spacer(Modifier.height(6.dp))
                Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = c.text, lineHeight = 20.sp, modifier = Modifier.padding(bottom = 5.dp))
                Text(item.desc, fontSize = 12.sp, color = c.muted, lineHeight = 18.sp)
                Text(item.date, fontSize = 10.sp, color = c.muted,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  SYLLABUS
// ═══════════════════════════════════════════════
@Composable
fun SyllabusScreen(vm: MainViewModel) {
    val c        = gkkColors
    val syllabus by vm.syllabus.collectAsStateWithLifecycle()
    val expandedPapers   = remember { mutableStateMapOf<Int, Boolean>() }
    val expandedSubjects = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Header
        GKKCard(modifier = Modifier.padding(bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋", fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Complete MPPSC Syllabus", fontFamily = Syne,
                        fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = c.text)
                    Text("Paper 1 (GS) + Paper 2 (CSAT) + Paper 3 (MP GK)",
                        fontSize = 12.sp, color = c.muted)
                }
            }
        }

        syllabus.forEachIndexed { pi, paper ->
            val paperExpanded = expandedPapers[pi] ?: true

            // Paper header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.navy)
                    .clickable { expandedPapers[pi] = !paperExpanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(paper.paper, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text(if (paperExpanded) "▼" else "▶", fontSize = 12.sp, color = Color.White)
            }

            if (paperExpanded) {
                paper.subjects.forEachIndexed { si, subject ->
                    val key     = "$pi-$si"
                    val subjExp = expandedSubjects[key] ?: false

                    // Subject row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(c.card)
                            .border(1.dp, c.border, RoundedCornerShape(8.dp))
                            .clickable { expandedSubjects[key] = !subjExp }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(subject.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                        Text(if (subjExp) "▼" else "▶", fontSize = 12.sp, color = c.muted)
                    }

                    if (subjExp) {
                        Column(modifier = Modifier.padding(start = 24.dp, bottom = 6.dp)) {
                            subject.topics.forEach { topic ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(c.bg)
                                        .border(1.dp, c.border, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("▸ ", fontSize = 12.sp, color = c.saff, fontWeight = FontWeight.Bold)
                                    Text(topic, fontSize = 12.sp, color = c.muted, lineHeight = 17.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  BOOKMARKS
// ═══════════════════════════════════════════════
@Composable
fun BookmarksScreen(vm: MainViewModel) {
    val c         = gkkColors
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title            = { Text("Clear All Bookmarks?") },
            confirmButton    = {
                TextButton(onClick = { vm.clearAllBookmarks(); showClearDialog = false }) {
                    Text("Clear", color = c.danger)
                }
            },
            dismissButton    = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg).padding(16.dp)
    ) {
        GKKCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔖 Bookmarked Questions", fontFamily = Syne,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = c.text)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${bookmarks.size} saved", fontSize = 13.sp, color = c.muted)
                    if (bookmarks.isNotEmpty()) {
                        TextButton(onClick = { showClearDialog = true }) {
                            Text("Clear All", fontSize = 11.sp, color = c.danger, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (bookmarks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No bookmarks yet.", fontSize = 13.sp, color = c.muted)
                    Text("Tap 🔖 while answering any question to save it here.",
                        fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 6.dp))
                }
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    bookmarks.forEachIndexed { idx, bm ->
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(bm.question, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = c.text, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                                IconButton(onClick = { vm.removeBookmark(idx) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = c.danger)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            bm.options.forEachIndexed { oi, opt ->
                                val isAnswer = oi == bm.answer
                                Text(
                                    "${OPTION_LABELS[oi]}) $opt ${if (isAnswer) "✓" else ""}",
                                    fontSize = 12.sp,
                                    color    = if (isAnswer) Color(0xFF166534) else c.muted,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(if (isAnswer) Color(0xFFECFDF5) else c.bg)
                                        .border(1.dp, if (isAnswer) Color(0xFF6EE7B7) else c.border, RoundedCornerShape(7.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            if (bm.explanation.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(bm.explanation, fontSize = 11.sp, color = c.muted,
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(c.bg)
                                        .border(start = BorderStroke(3.dp, c.navy))
                                        .padding(horizontal = 10.dp, vertical = 7.dp))
                            }
                            Text(bm.category, fontSize = 10.sp, color = c.muted,
                                modifier = Modifier.padding(top = 6.dp))
                        }
                        if (idx < bookmarks.size - 1) HorizontalDivider(color = c.border)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  WEAK AREAS
// ═══════════════════════════════════════════════
@Composable
fun WeakAreasScreen(vm: MainViewModel) {
    val c         = gkkColors
    val weakAreas by vm.weakAreas.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        GKKCard {
            SectionHeader("Weak Areas — Focus Here")
            if (weakAreas.isEmpty()) {
                Text("Complete some tests to see your weak areas.",
                    fontSize = 13.sp, color = c.muted,
                    modifier = Modifier.padding(vertical = 20.dp))
            } else {
                weakAreas.forEach { (ch, acc) ->
                    WeakAreaRow(ch, acc)
                    HorizontalDivider(color = c.border, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  PROGRESS
// ═══════════════════════════════════════════════
@Composable
fun ProgressScreen(vm: MainViewModel) {
    val c            = gkkColors
    val stats        by vm.stats.collectAsStateWithLifecycle()
    val chapterStats by vm.chapterStats.collectAsStateWithLifecycle()
    val categories   by vm.categories.collectAsStateWithLifecycle()
    val questions    by vm.questions.collectAsStateWithLifecycle()
    val attempted    = stats.first
    val correct      = stats.second
    val accuracy     = if (attempted > 0) correct * 100 / attempted else 0

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Overall stats
        GKKCard(modifier = Modifier.padding(bottom = 14.dp)) {
            SectionHeader("Overall Performance")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultStat2("$attempted", "Attempted", c.navy,    Modifier.weight(1f))
                ResultStat2("$correct",   "Correct",   c.success, Modifier.weight(1f))
                ResultStat2("$accuracy%", "Accuracy",  c.saff,    Modifier.weight(1f))
            }
        }

        // Chapter-wise breakdown
        GKKCard {
            SectionHeader("Chapter-wise Performance")
            categories.forEachIndexed { idx, cat ->
                val stat    = chapterStats[cat] ?: Pair(0, 0)
                val qCount  = questions.count { it.category == cat }
                val acc     = if (stat.first > 0) stat.second * 100 / stat.first else 0
                val progress = if (qCount > 0) stat.first.toFloat() / qCount else 0f
                val color   = when { acc >= 70 -> c.success; acc >= 40 -> c.warn; else -> c.danger }

                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            cat.replace(Regex("^Ch\\.\\d+\\s*"), ""),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.text,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${stat.first}/$qCount done · $acc%",
                            fontSize = 11.sp, color = if (stat.first > 0) color else c.muted)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(5.dp)
                            .clip(RoundedCornerShape(3.dp)).background(c.border)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp)).background(color)
                        )
                    }
                }
                if (idx < categories.size - 1) HorizontalDivider(color = c.border, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun ResultStat2(value: String, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier          = modifier.clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontFamily = Syne, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = color)
        Text(label, fontSize = 10.sp, color = color, modifier = Modifier.padding(top = 2.dp))
    }
}

// ═══════════════════════════════════════════════
//  SMART REVIEW (SRS)
// ═══════════════════════════════════════════════
@Composable
fun ReviewScreen(vm: MainViewModel) {
    val c       = gkkColors
    val entries by vm.srsEntries.collectAsStateWithLifecycle()
    val now     = System.currentTimeMillis()

    // Snapshot due list once per composition so index is stable
    val due = remember(entries) { entries.filter { it.nextReview <= now } }

    var currentDueIdx by remember(due) { mutableStateOf(0) }
    // Track answer per question index — avoids state bleeding across cards
    val answers = remember(due) { mutableStateMapOf<Int, Int>() }
    val chosen: Int? = answers[currentDueIdx]

    val totalInSRS = entries.size
    val mastered   = entries.count { it.level >= 4 }
    val allDone    = due.isNotEmpty() && currentDueIdx >= due.size

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Stats
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ResultStat2("${due.size}",     "Due Today",   c.danger,  Modifier.weight(1f))
            ResultStat2("$totalInSRS",     "In SRS",      c.navy,    Modifier.weight(1f))
            ResultStat2("$mastered",       "Mastered",    c.success, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        if (due.isEmpty() || allDone) {
            GKKCard {
                Column(
                    modifier          = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (allDone) "🎉" else "✅", fontSize = 40.sp)
                    Text(
                        if (allDone) "Session Complete!" else "All caught up!",
                        fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        if (allDone) "You reviewed all ${due.size} due cards."
                        else "No reviews due today. Answer questions incorrectly to add them here.",
                        fontSize = 13.sp, color = c.muted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                }
            }
            return
        }

        val entry = due[currentDueIdx]
        val q     = entry.question

        GKKCard {
            Text("REVIEW ${currentDueIdx + 1} of ${due.size} DUE",
                fontSize = 11.sp, color = c.muted, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 10.dp))
            Text(q.question, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = c.text, lineHeight = 23.sp, modifier = Modifier.padding(bottom = 14.dp))

            q.options.forEachIndexed { idx, opt ->
                val answered  = chosen != null
                val isCorrect = idx == q.answer
                val isChosen  = idx == chosen
                val bg     = when { !answered -> c.card; isCorrect -> Color(0xFFECFDF5); isChosen && !isCorrect -> Color(0xFFFEF2F2); else -> c.card }
                val border = when { !answered -> c.border; isCorrect -> Color(0xFF6EE7B7); isChosen && !isCorrect -> Color(0xFFFCA5A5); else -> c.border }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .border(1.5.dp, border, RoundedCornerShape(10.dp))
                        .then(if (!answered) Modifier.clickable {
                            answers[currentDueIdx] = idx          // store answer for THIS card only
                            vm.submitSrsAnswer(entry, idx == q.answer)
                        } else Modifier)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${OPTION_LABELS[idx]}) $opt", fontSize = 13.sp, color = c.text)
                }
            }

            if (chosen != null) {
                val isCorrect = chosen == q.answer
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
                        .padding(12.dp)
                ) {
                    Text(if (isCorrect) "✅ Correct!" else "❌ Wrong!",
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF166534) else Color(0xFF991B1B))
                    Text("Correct: ${OPTION_LABELS[q.answer]}) ${q.options.getOrElse(q.answer) { "" }}",
                        fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(Modifier.height(14.dp))
                GKKButton(
                    if (currentDueIdx < due.size - 1) "Next Review →" else "Finish Session ✓",
                    onClick = { currentDueIdx++ }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  SETTINGS
// ═══════════════════════════════════════════════
@Composable
fun SettingsScreen(vm: MainViewModel) {
    val c            = gkkColors
    val currentTheme by vm.theme.collectAsStateWithLifecycle()
    val notifEnabled by vm.notifEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Theme picker
        GKKCard(modifier = Modifier.padding(bottom = 14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎨 App Theme", fontFamily = Syne, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, color = c.text)
                GKKBadge(
                    "${currentTheme.emoji} ${currentTheme.displayName}",
                    bg        = Color(0xFFEEF0FF),
                    textColor = c.navy
                )
            }
            Text("Choose your look. Saved automatically.",
                fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))

            // 2-column theme grid
            val themes = GKKTheme.entries
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                themes.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { theme ->
                            val tc     = gkkColorsFor(theme)
                            val active = theme == currentTheme
                            Box(
                                modifier = Modifier.weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tc.card)
                                    .border(
                                        2.5.dp,
                                        if (active) c.navy else c.border,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { vm.setTheme(theme) }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        listOf(tc.navy, tc.saff, tc.bg).forEach { col ->
                                            Box(
                                                modifier = Modifier.size(20.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(col)
                                                    .border(1.dp, c.border, androidx.compose.foundation.shape.CircleShape)
                                            )
                                        }
                                    }
                                    Text("${theme.emoji} ${theme.displayName}",
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                        color = tc.navy)
                                }
                                if (active) {
                                    Box(
                                        modifier = Modifier.align(Alignment.TopEnd)
                                            .size(18.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(c.navy),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // General settings
        GKKCard {
            SectionHeader("General")
            SettingRow("🔔 Notifications", "Daily study reminders") {
                Switch(
                    checked  = notifEnabled,
                    onCheckedChange = { vm.setNotifEnabled(it) },
                    colors   = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = c.navy)
                )
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, desc: String, control: @Composable () -> Unit) {
    val c = gkkColors
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            Text(desc,  fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp))
        }
        control()
    }
    HorizontalDivider(color = c.border, thickness = 0.5.dp)
}

// ═══════════════════════════════════════════════
//  DONATE
// ═══════════════════════════════════════════════
@Composable
fun DonateScreen(vm: MainViewModel) {
    val c       = gkkColors
    val context = LocalContext.current
    Column(
        modifier            = Modifier.fillMaxSize().background(c.bg)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GKKCard {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("❤️", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
                Text(
                    "Support GKK MPPSC",
                    fontFamily  = Syne, fontWeight = FontWeight.ExtraBold,
                    fontSize    = 20.sp, color = c.text,
                    textAlign   = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier    = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "This app is free for all MPPSC aspirants. If it helped you, consider supporting us to keep it running and add more content.",
                    fontSize  = 13.sp, color = c.muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                )

                // QR code placeholder box
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEEF0FF))
                        .border(2.dp, c.navy, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▦", fontSize = 64.sp, color = c.navy)
                        Text(
                            "Scan to Pay",
                            fontSize   = 12.sp, fontWeight = FontWeight.SemiBold,
                            color      = c.navy,
                            modifier   = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))

                // UPI ID box
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFF6EE7B7), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.fillMaxWidth()
                    ) {
                        Text("UPI ID", fontSize = 12.sp, color = c.muted)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "padhleyrr@upi",
                            fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                            fontSize   = 18.sp, color = c.navy,
                            textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        // Copy-to-clipboard button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(c.navy)
                                .clickable {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                            as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(
                                        android.content.ClipData.newPlainText("UPI ID", "padhleyrr@upi")
                                    )
                                }
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "📋 Copy UPI ID",
                                fontSize   = 13.sp, fontWeight = FontWeight.SemiBold,
                                color      = Color.White
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Every contribution helps keep this app free 🙏",
                    fontSize  = 12.sp, color = c.muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Helper for Bookmarks screen
private val OPTION_LABELS = listOf("A", "B", "C", "D")

private fun Modifier.border(start: BorderStroke): Modifier = this.then(
    Modifier.border(start.width, start.brush, RoundedCornerShape(7.dp))
)
