@file:OptIn(ExperimentalLayoutApi::class)

package com.gkk.mppsc.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gkk.mppsc.ui.components.*
import com.gkk.mppsc.ui.theme.Syne
import com.gkk.mppsc.ui.theme.gkkColors
import com.gkk.mppsc.viewmodel.MainViewModel

@Composable
fun FlashcardsScreen(
    vm:       MainViewModel,
    modifier: Modifier = Modifier
) {
    val c          = gkkColors
    val categories by vm.categories.collectAsStateWithLifecycle()

    var selectedCats    by remember { mutableStateOf(emptySet<String>()) }
    var pool            by remember { mutableStateOf(emptyList<com.gkk.mppsc.data.models.Question>()) }
    var currentIndex    by remember { mutableStateOf(0) }
    var isFlipped       by remember(currentIndex) { mutableStateOf(false) }
    var sessionStarted  by remember { mutableStateOf(false) }
    var correctCount    by remember { mutableStateOf(0) }
    var incorrectCount  by remember { mutableStateOf(0) }

    // Seed all categories once loaded
    LaunchedEffect(categories) {
        if (selectedCats.isEmpty() && categories.isNotEmpty())
            selectedCats = categories.toSet()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (!sessionStarted) {
            // ── Category picker ──────────────────────────────────────
            GKKCard(modifier = Modifier.padding(bottom = 14.dp)) {
                SectionHeader(
                    title       = "🃏 Flashcard Mode",
                    actionLabel = "All",
                    onAction    = { selectedCats = categories.toSet() }
                )
                Text(
                    "Tap a card to reveal the answer. Rate yourself to track weak spots.",
                    fontSize = 12.sp, color = c.muted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                FlowRow(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val clean = cat.replace(Regex("^Ch\\.\\d+\\s*"), "")
                        FilterChip(
                            label    = clean,
                            selected = cat in selectedCats,
                            onClick  = {
                                selectedCats = if (cat in selectedCats)
                                    selectedCats - cat else selectedCats + cat
                            }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                GKKButton("Start Flashcards →", onClick = {
                    if (selectedCats.isNotEmpty()) {
                        pool         = vm.getFlashcardPool(selectedCats.toList())
                        currentIndex = 0
                        correctCount = 0
                        incorrectCount = 0
                        sessionStarted = true
                    }
                })
            }
        } else if (currentIndex >= pool.size) {
            // ── Session complete ─────────────────────────────────────
            GKKCard {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Session Complete!",
                        fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp, color = c.text
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("$correctCount", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp, color = c.success)
                            Text("Knew it", fontSize = 12.sp, color = c.success)
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("$incorrectCount", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp, color = c.danger)
                            Text("Review again", fontSize = 12.sp, color = c.danger)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    GKKButton("New Session →", onClick = {
                        pool         = vm.getFlashcardPool(selectedCats.toList())
                        currentIndex = 0
                        correctCount = 0
                        incorrectCount = 0
                        isFlipped    = false
                    })
                    Spacer(Modifier.height(8.dp))
                    GKKOutlineButton("Change Categories", onClick = {
                        sessionStarted = false
                    })
                }
            }
        } else {
            // ── Active flashcard ─────────────────────────────────────
            val q = pool[currentIndex]

            // Progress
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${currentIndex + 1} / ${pool.size}",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.muted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✅ $correctCount", fontSize = 12.sp, color = c.success, fontWeight = FontWeight.Bold)
                    Text("❌ $incorrectCount", fontSize = 12.sp, color = c.danger, fontWeight = FontWeight.Bold)
                }
            }

            // Progress bar
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(c.border)
                    .padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((currentIndex + 1).toFloat() / pool.size)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(c.navy)
                )
            }
            Spacer(Modifier.height(12.dp))

            // Chapter badge
            Text(
                q.category.replace(Regex("^Ch\\.\\d+\\s*"), "").uppercase(),
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = c.saff, letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Flip card
            FlipCard(
                isFlipped = isFlipped,
                front = {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("❓", fontSize = 36.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            q.question,
                            fontSize  = 15.sp, fontWeight = FontWeight.SemiBold,
                            color     = c.text, lineHeight = 23.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Tap to reveal answer",
                            fontSize = 12.sp, color = c.muted
                        )
                    }
                },
                back = {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✅", fontSize = 30.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Answer: ${listOf("A","B","C","D").getOrElse(q.answer){"?"}})",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = c.saff
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            q.options.getOrElse(q.answer) { "" },
                            fontSize  = 15.sp, fontWeight = FontWeight.SemiBold,
                            color     = c.text, lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        if (q.explanation.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                q.explanation,
                                fontSize  = 11.5.sp, color = c.muted,
                                lineHeight = 18.sp, textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                onClick = { isFlipped = !isFlipped }
            )

            // Rate buttons (only after flip)
            if (isFlipped) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Didn't know
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(2.dp, c.danger, RoundedCornerShape(12.dp))
                            .clickable {
                                incorrectCount++
                                isFlipped = false
                                currentIndex++
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("❌  Didn't Know", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = c.danger)
                    }
                    // Knew it
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFECFDF5))
                            .border(2.dp, c.success, RoundedCornerShape(12.dp))
                            .clickable {
                                correctCount++
                                isFlipped = false
                                currentIndex++
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✅  Knew It", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = c.success)
                    }
                }
            } else if (currentIndex < pool.size) {
                Spacer(Modifier.height(16.dp))
                GKKOutlineButton("Skip →", onClick = {
                    isFlipped = false
                    currentIndex++
                })
            }
        }
    }
}

@Composable
private fun FlipCard(
    isFlipped: Boolean,
    front:     @Composable () -> Unit,
    back:      @Composable () -> Unit,
    onClick:   () -> Unit
) {
    val c = gkkColors
    val rotation by animateFloatAsState(
        targetValue    = if (isFlipped) 180f else 0f,
        animationSpec  = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label          = "cardFlip"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 220.dp)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clip(RoundedCornerShape(16.dp))
            .background(c.card)
            .border(1.dp, c.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}
