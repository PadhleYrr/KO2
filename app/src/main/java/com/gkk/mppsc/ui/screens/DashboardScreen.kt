package com.gkk.mppsc.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gkk.mppsc.ui.components.*
import com.gkk.mppsc.ui.navigation.Route
import com.gkk.mppsc.ui.theme.Syne
import com.gkk.mppsc.ui.theme.gkkColors
import com.gkk.mppsc.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    vm: MainViewModel = viewModel(),
    nav: NavHostController? = null
) {
    val c            = gkkColors
    val stats        by vm.stats.collectAsStateWithLifecycle()
    val streak       by vm.streak.collectAsStateWithLifecycle()
    val srsDue       by vm.srsDueCount.collectAsStateWithLifecycle()
    val weakAreas    by vm.weakAreas.collectAsStateWithLifecycle()
    val questions    by vm.questions.collectAsStateWithLifecycle()
    val chapterStats by vm.chapterStats.collectAsStateWithLifecycle()
    val categories   by vm.categories.collectAsStateWithLifecycle()

    val attempted = stats.first
    val correct   = stats.second
    val accuracy  = if (attempted > 0) correct * 100 / attempted else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Welcome banner ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(c.navy, c.navy.copy(alpha = 0.8f))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "GKK MPPSC", fontFamily = Syne,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp, color = Color.White
                    )
                    Text(
                        "Keep going! Every question counts.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 28.sp)
                    Text(
                        "$streak day${if (streak != 1) "s" else ""}",
                        fontSize = 11.sp, color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // ── Stats row ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label   = "Attempted",
                value   = "$attempted",
                sub     = "questions",
                accent  = c.navy,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label   = "Accuracy",
                value   = "$accuracy%",
                sub     = "$correct correct",
                accent  = if (accuracy >= 70) c.success else if (accuracy >= 40) c.warn else c.danger,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label   = "SRS Due",
                value   = "$srsDue",
                sub     = "to review",
                accent  = if (srsDue > 0) c.danger else c.success,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))

        // ── Quick actions ────────────────────────────────────────────
        GKKCard(modifier = Modifier.padding(bottom = 16.dp)) {
            SectionHeader("Quick Start")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    emoji   = "📅",
                    label   = "Daily 10",
                    color   = Color(0xFFEEF0FF),
                    onClick = { nav?.navigate(Route.DAILY) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    emoji   = "⚡",
                    label   = "Quick Test",
                    color   = Color(0xFFFFF7ED),
                    onClick = { nav?.navigate(Route.TEST) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    emoji   = "🧠",
                    label   = "SRS Review",
                    color   = if (srsDue > 0) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                    onClick = { nav?.navigate(Route.REVIEW) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    emoji   = "📖",
                    label   = "Notes",
                    color   = Color(0xFFF0FDF4),
                    onClick = { nav?.navigate(Route.NOTES) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Weak areas ───────────────────────────────────────────────
        if (weakAreas.isNotEmpty()) {
            GKKCard(modifier = Modifier.padding(bottom = 16.dp)) {
                SectionHeader(
                    title       = "⚠️ Focus Areas",
                    actionLabel = "See All",
                    onAction    = { nav?.navigate(Route.WEAK_AREAS) }
                )
                weakAreas.take(4).forEach { (ch, acc) ->
                    WeakAreaRow(ch, acc)
                }
            }
        }

        // ── Chapter progress grid ────────────────────────────────────
        if (categories.isNotEmpty()) {
            GKKCard {
                SectionHeader(
                    title       = "Chapter Progress",
                    actionLabel = "Full Report",
                    onAction    = { nav?.navigate(Route.PROGRESS) }
                )
                val chunked = categories.chunked(2)
                chunked.forEachIndexed { rowIdx, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (rowIdx < chunked.size - 1) 10.dp else 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { cat ->
                            val stat     = chapterStats[cat] ?: Pair(0, 0)
                            val qCount   = questions.count { it.category == cat }
                            val acc      = if (stat.first > 0) stat.second * 100 / stat.first else 0
                            val color    = when {
                                acc >= 70 -> c.success
                                acc >= 40 -> c.warn
                                stat.first > 0 -> c.danger
                                else -> c.muted
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
                        // Fill empty slot if odd number
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun QuickActionButton(
    emoji:    String,
    label:    String,
    color:    Color,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = gkkColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .border(1.dp, c.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = c.text)
    }
}
