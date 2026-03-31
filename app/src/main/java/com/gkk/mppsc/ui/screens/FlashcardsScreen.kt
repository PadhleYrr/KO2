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
import com.gkk.mppsc.data.models.Question
import com.gkk.mppsc.ui.components.*
import com.gkk.mppsc.ui.theme.gkkColors
import com.gkk.mppsc.viewmodel.MainViewModel

@Composable
fun FlashcardsScreen(vm: MainViewModel) {
    val c          = gkkColors
    val categories by vm.categories.collectAsStateWithLifecycle()
    var selectedCats by remember { mutableStateOf(emptySet<String>()) }
    var sessionPool  by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentIdx   by remember { mutableStateOf(0) }
    var flipped      by remember { mutableStateOf(false) }
    var known        by remember { mutableStateOf(0) }
    var learning     by remember { mutableStateOf(0) }
    var sessionActive by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (selectedCats.isEmpty()) selectedCats = categories.toSet()
    }

    if (!sessionActive) {
        // ── Setup screen ──────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize().background(c.bg)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            GKKCard {
                SectionHeader("Flashcard Mode")
                // Chapter chips
                FlowRow(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    // All chip
                    val allOn = selectedCats.size == categories.size
                    FilterChip("All Chapters", allOn) {
                        selectedCats = if (allOn) emptySet() else categories.toSet()
                    }
                    categories.forEach { cat ->
                        FilterChip(
                            cat.replace(Regex("^Ch\\.\\d+\\s*"), ""),
                            cat in selectedCats
                        ) {
                            selectedCats = if (cat in selectedCats) selectedCats - cat else selectedCats + cat
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                val qs = vm.questions.collectAsStateWithLifecycle().value
                val count = qs.count { it.category in selectedCats }
                Text("$count cards selected", fontSize = 13.sp, color = c.muted)
                Spacer(Modifier.height(14.dp))
                GKKButton("Start Flashcards →") {
                    val pool = vm.getFlashcardPool(selectedCats.toList())
                    if (pool.isNotEmpty()) {
                        sessionPool   = pool
                        currentIdx    = 0
                        flipped       = false
                        known         = 0
                        learning      = 0
                        sessionActive = true
                    }
                }
            }
        }
    } else {
        // ── Active session ────────────────────────────────────────────
        val currentCard = sessionPool.getOrNull(currentIdx)

        if (currentCard == null) {
            // Done
            Column(
                modifier          = Modifier.fillMaxSize().background(c.bg).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 56.sp)
                Spacer(Modifier.height(16.dp))
                Text("All Cards Done!", fontFamily = com.gkk.mppsc.ui.theme.Syne,
                    fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = c.text)
                Spacer(Modifier.height(8.dp))
                Text("Known: $known  ·  Learning: $learning", fontSize = 14.sp, color = c.muted)
                Spacer(Modifier.height(24.dp))
                GKKButton("Try Again") {
                    sessionPool = sessionPool.shuffled()
                    currentIdx  = 0; flipped = false; known = 0; learning = 0
                }
                Spacer(Modifier.height(12.dp))
                GKKOutlineButton("Back to Setup") { sessionActive = false }
            }
            return
        }

        Column(
            modifier          = Modifier.fillMaxSize().background(c.bg).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Counter
            Text(
                "Card ${currentIdx + 1} of ${sessionPool.size}",
                fontSize = 12.sp, color = c.muted, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Flip card
            FlipCard(
                question    = currentCard.question,
                answer      = currentCard.options.getOrElse(currentCard.answer) { "" },
                explanation = currentCard.explanation,
                isFlipped   = flipped,
                onClick     = { if (!flipped) flipped = true },
                modifier    = Modifier.fillMaxWidth().weight(1f).padding(bottom = 16.dp)
            )

            // Known / Learning
            if (flipped) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { known++; currentIdx++; flipped = false },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7)),
                        shape    = RoundedCornerShape(9.dp)
                    ) {
                        Text("✓ I Know This", fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D), fontSize = 13.sp)
                    }
                    Button(
                        onClick = { learning++; currentIdx++; flipped = false },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        shape    = RoundedCornerShape(9.dp)
                    ) {
                        Text("✗ Study More", fontWeight = FontWeight.Bold,
                            color = c.danger, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Progress
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("✓ Known: $known",    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.success)
                Text("✗ Learning: $learning", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.danger)
            }
        }
    }
}

@Composable
private fun FlipCard(
    question:    String,
    answer:      String,
    explanation: String,
    isFlipped:   Boolean,
    onClick:     () -> Unit,
    modifier:    Modifier = Modifier
) {
    val c = gkkColors
    val rotation by animateFloatAsState(
        targetValue    = if (isFlipped) 180f else 0f,
        animationSpec  = tween(400)
    )

    Box(
        modifier = modifier
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clip(RoundedCornerShape(18.dp))
            .background(c.card)
            .border(1.dp, c.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Front — question
            Column(
                modifier          = Modifier.padding(28.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("TAP TO REVEAL ANSWER", fontSize = 11.sp, color = c.muted,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 14.dp))
                Text(question, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    color = c.text, textAlign = TextAlign.Center, lineHeight = 26.sp)
            }
        } else {
            // Back — answer (counter-rotated)
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .padding(28.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("✔ $answer", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = c.success, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp))
                if (explanation.isNotEmpty()) {
                    Text(explanation, fontSize = 12.sp, color = c.muted,
                        textAlign = TextAlign.Center, lineHeight = 18.sp)
                }
            }
        }
    }
}
