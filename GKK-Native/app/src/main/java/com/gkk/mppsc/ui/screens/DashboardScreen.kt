package com.gkk.mppsc.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gkk.mppsc.ui.components.*
import com.gkk.mppsc.ui.navigation.Route
import com.gkk.mppsc.ui.theme.Syne
import com.gkk.mppsc.ui.theme.gkkColors
import com.gkk.mppsc.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

private val CHAPTER_COLORS = listOf(
    Color(0xFF1A237E), Color(0xFF006064), Color(0xFF4A148C), Color(0xFF1B5E20),
    Color(0xFFB71C1C), Color(0xFFE65100), Color(0xFF880E4F), Color(0xFF0D47A1),
    Color(0xFF33691E), Color(0xFF4E342E), Color(0xFF37474F), Color(0xFF1565C0),
    Color(0xFF6A1B9A), Color(0xFF00695C), Color(0xFFC62828)
)

@Composable
fun DashboardScreen(vm: MainViewModel, nav: NavHostController) {
    val c            = gkkColors
    val stats        by vm.stats.collectAsStateWithLifecycle()
    val streak       by vm.streak.collectAsStateWithLifecycle()
    val questions    by vm.questions.collectAsStateWithLifecycle()
    val categories   by vm.categories.collectAsStateWithLifecycle()
    val chapterStats by vm.chapterStats.collectAsStateWithLifecycle()
    val heatmap      by vm.heatmap.collectAsStateWithLifecycle()
    val weakAreas    by vm.weakAreas.collectAsStateWithLifecycle()

    val attempted = stats.first
    val correct   = stats.second
    val accuracy  = if (attempted > 0) (correct * 100 / attempted) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        // ── Stat cards grid ───────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Questions Attempted", "$attempted", "of ${questions.size} total", c.navy, Modifier.weight(1f))
            StatCard("Overall Accuracy",    "$accuracy%", "correct answer rate",         c.success, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Study Streak", "$streak 🔥", "days in a row", c.saff,   Modifier.weight(1f))
            StatCard("SRS Due",      "${vm.srsDueCount.collectAsStateWithLifecycle().value}", "review now",  c.danger, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        // ── Daily 10 banner ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(c.navy)
                .clickable { nav.navigate(Route.DAILY) }
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Daily 10 🎯", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp, color = Color.White)
                    Text("10 random questions — builds habit, boosts retention",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 3.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text("Start →", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.navy)
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        // ── Chapter progress ──────────────────────────────────────────
        GKKCard {
            SectionHeader("Chapter Progress", "Practice All →") { nav.navigate(Route.TEST) }
            LazyVerticalGrid(
                columns          = GridCells.Fixed(2),
                modifier         = Modifier.height(
                    // dynamic height based on row count
                    ((categories.size / 2 + categories.size % 2) * 110).dp.coerceAtMost(440.dp)
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement   = Arrangement.spacedBy(10.dp),
                userScrollEnabled     = false
            ) {
                itemsIndexed(categories) { idx, cat ->
                    val stat    = chapterStats[cat] ?: Pair(0, 0)
                    val qCount  = questions.count { it.category == cat }
                    val acc     = if (stat.first > 0) stat.second * 100 / stat.first else 0
                    val color   = CHAPTER_COLORS[idx % CHAPTER_COLORS.size]
                    ChapterCard(
                        name        = cat,
                        count       = qCount,
                        done        = stat.first,
                        accuracy    = acc,
                        accentColor = color,
                        onClick     = { nav.navigate(Route.TEST) }
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // ── Heatmap + Weak Areas ──────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {

            // Heatmap
            GKKCard(modifier = Modifier.weight(1.2f)) {
                SectionHeader("Study Heatmap")
                HeatmapGrid(heatmap = heatmap, navy = c.navy)
                Spacer(Modifier.height(6.dp))
                HeatmapLegend(navy = c.navy)
            }

            // Weak Areas
            GKKCard(modifier = Modifier.weight(1f)) {
                SectionHeader("Weak Areas", "See All →") { nav.navigate(Route.WEAK_AREAS) }
                if (weakAreas.isEmpty()) {
                    Text(
                        "Complete some tests to see weak areas",
                        fontSize = 13.sp, color = c.muted,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    weakAreas.take(5).forEach { (ch, acc) ->
                        WeakAreaRow(ch, acc)
                        HorizontalDivider(color = c.border, thickness = 0.5.dp)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HeatmapGrid(heatmap: Map<String, Int>, navy: Color) {
    val sdf   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = Calendar.getInstance()
    val cells = (181 downTo 0).map { daysAgo ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
        val key = sdf.format(cal.time)
        val cnt = heatmap[key] ?: 0
        val lvl = when { cnt == 0 -> 0; cnt < 3 -> 1; cnt < 7 -> 2; cnt < 12 -> 3; else -> 4 }
        lvl
    }
    FlowRow(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement   = Arrangement.spacedBy(3.dp),
        maxItemsInEachRow     = 26
    ) {
        cells.forEach { level ->
            HeatmapCell(level = level, navy = navy)
        }
    }
}

@Composable
private fun HeatmapLegend(navy: Color) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Less", fontSize = 11.sp, color = Color(0xFF64748B))
        listOf(0, 1, 2, 3, 4).forEach { HeatmapCell(it, navy) }
        Text("More", fontSize = 11.sp, color = Color(0xFF64748B))
    }
}
