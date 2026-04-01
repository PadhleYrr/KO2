package com.padhleyrr.mppsc.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.padhleyrr.mppsc.ui.components.*
import com.padhleyrr.mppsc.ui.navigation.Route
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors
import com.padhleyrr.mppsc.viewmodel.MainViewModel

// ══════════════════════════════════════════════════════════════
//  DASHBOARD — pixel-matched to the HTML/CSS reference
//  Layout:  4-stat grid  →  Daily banner  →  Chapter progress
//           →  2-col row (Heatmap | Weak Areas)
// ══════════════════════════════════════════════════════════════
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    vm:       MainViewModel,
    nav:      NavHostController? = null
) {
    val c            = gkkColors
    val stats        by vm.stats.collectAsStateWithLifecycle()
    val streak       by vm.streak.collectAsStateWithLifecycle()
    val weakAreas    by vm.weakAreas.collectAsStateWithLifecycle()
    val questions    by vm.questions.collectAsStateWithLifecycle()
    val chapterStats by vm.chapterStats.collectAsStateWithLifecycle()
    val categories   by vm.categories.collectAsStateWithLifecycle()
    val bookmarks     by vm.bookmarks.collectAsStateWithLifecycle()

    val attempted = stats.first
    val correct   = stats.second
    val accuracy  = if (attempted > 0) correct * 100 / attempted else 0
    val total     = questions.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {

        // ── 1. Four stat cards (2×2 grid) ────────────────────────────
        // Row 1
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label    = "Questions Attempted",
                value    = "$attempted",
                sub      = "of $total total",
                accent   = c.navy,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label    = "Overall Accuracy",
                value    = "$accuracy%",
                sub      = "correct answer rate",
                accent   = c.success,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        // Row 2
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label    = "Study Streak",
                value    = "$streak 🔥",
                sub      = "days in a row",
                accent   = c.saff,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label    = "Flagged Questions",
                value    = "${bookmarks.size}",
                sub      = "reported for review",
                accent   = c.danger,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(14.dp))

        // ── 2. Daily 10 banner ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(c.navy)
                .clickable { nav?.navigate(Route.DAILY) }
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Today's Daily 10 🎯",
                    fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp, color = Color.White
                )
                Text(
                    "10 random questions — builds habit, boosts retention",
                    fontSize = 12.sp,
                    color    = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White)
                    .clickable { nav?.navigate(Route.DAILY) }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    "Start Now →",
                    fontFamily = Syne, fontWeight = FontWeight.Bold,
                    fontSize = 13.sp, color = c.navy
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        // ── 3. Chapter Progress ───────────────────────────────────────
        if (categories.isNotEmpty()) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Chapter Progress",
                    fontFamily = Syne, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, color = c.text
                )
                Text(
                    "Practice All →",
                    fontSize   = 12.sp, fontWeight = FontWeight.SemiBold,
                    color      = c.saff,
                    modifier   = Modifier.clickable { nav?.navigate(Route.TEST) }
                )
            }
            // 2-column grid of chapter cards
            val chunked = categories.chunked(2)
            chunked.forEachIndexed { rowIdx, row ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (rowIdx < chunked.size - 1) 8.dp else 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { cat ->
                        val stat   = chapterStats[cat] ?: Pair(0, 0)
                        val qCount = questions.count { it.category == cat }
                        val acc    = if (stat.first > 0) stat.second * 100 / stat.first else 0
                        val color  = when {
                            acc >= 70   -> c.success
                            acc >= 40   -> c.warn
                            stat.first > 0 -> c.danger
                            else        -> c.muted
                        }
                        ChapterCard(
                            name        = cat,
                            count       = qCount,
                            done        = stat.first,
                            accuracy    = acc,
                            accentColor = color,
                            onClick     = { nav?.navigate(Route.TEST) },
                            modifier    = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                if (rowIdx < chunked.size - 1) Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── 4. Heatmap + Weak Areas side by side ─────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.Top
        ) {
            // Heatmap card
            GKKCard(modifier = Modifier.weight(1f)) {
                Text(
                    "Study Heatmap",
                    fontFamily = Syne, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = c.text,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                DashHeatmap()
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Less", fontSize = 10.sp, color = c.muted)
                    listOf(0, 1, 2, 3, 4).forEach { l ->
                        HeatmapCell(level = l, navy = c.navy, modifier = Modifier.size(11.dp))
                    }
                    Text("More", fontSize = 10.sp, color = c.muted)
                }
            }

            // Weak Areas card
            GKKCard(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Weak Areas",
                        fontFamily = Syne, fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, color = c.text
                    )
                    Text(
                        "See All →",
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color    = c.saff,
                        modifier = Modifier.clickable { nav?.navigate(Route.WEAK_AREAS) }
                    )
                }
                if (weakAreas.isEmpty()) {
                    Text(
                        "No weak areas yet!\nKeep practising.",
                        fontSize  = 12.sp, color = c.muted,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    weakAreas.take(5).forEach { (ch, acc) ->
                        WeakAreaRow(name = ch, accuracy = acc)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ── Compact heatmap (26 cols × 6 rows = last ~6 months) ──────────────
@Composable
private fun DashHeatmap() {
    val c    = gkkColors
    val cols = 13   // trimmed for narrow card
    val rows = 6
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(cols) { col ->
                    val seed  = row * cols + col
                    val level = when {
                        seed % 7 == 0 -> 0
                        seed % 5 == 0 -> 3
                        seed % 3 == 0 -> 2
                        seed % 2 == 0 -> 1
                        else          -> 0
                    }
                    HeatmapCell(level = level, navy = c.navy, modifier = Modifier.size(11.dp))
                }
            }
        }
    }
}
