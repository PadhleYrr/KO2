package com.gkk.mppsc.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gkk.mppsc.data.models.Question
import com.gkk.mppsc.data.models.TestMode
import com.gkk.mppsc.ui.components.*
import com.gkk.mppsc.ui.navigation.Route
import com.gkk.mppsc.ui.theme.Syne
import com.gkk.mppsc.ui.theme.gkkColors
import com.gkk.mppsc.viewmodel.MainViewModel

private val OPTION_LABELS = listOf("A", "B", "C", "D")

// ═══════════════════════════════════════════════
//  TEST HOME
// ═══════════════════════════════════════════════
@Composable
fun TestHomeScreen(vm: MainViewModel, nav: NavHostController) {
    val c          = gkkColors
    val categories by vm.categories.collectAsStateWithLifecycle()
    var selectedMode     by remember { mutableStateOf(TestMode.FULL) }
    var selectedCats     by remember { mutableStateOf(emptySet<String>()) }

    // Init — all selected
    LaunchedEffect(categories) {
        if (selectedCats.isEmpty() && categories.isNotEmpty())
            selectedCats = categories.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Mode selector
        GKKCard {
            SectionHeader("Select Test Mode")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeCard("📚", "Full Test",       "All selected qs",  selectedMode == TestMode.FULL,     { selectedMode = TestMode.FULL },     Modifier.weight(1f))
                ModeCard("📋", "Long — 100 Qs",  "Random · ~60 min", selectedMode == TestMode.LONG,     { selectedMode = TestMode.LONG },     Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeCard("⚡", "Practice — 50",  "Random · ~30 min", selectedMode == TestMode.PRACTICE, { selectedMode = TestMode.PRACTICE }, Modifier.weight(1f))
                ModeCard("🎯", "Quick — 25 Qs",  "Random · ~15 min", selectedMode == TestMode.QUICK,    { selectedMode = TestMode.QUICK },    Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(14.dp))

        // Chapter filter
        GKKCard {
            SectionHeader(
                title       = "Filter by Chapter",
                actionLabel = "Select All",
                onAction    = { selectedCats = categories.toSet() }
            )
            FlowRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val cleanName = cat.replace(Regex("^Ch\\.\\d+\\s*"), "")
                    FilterChip(
                        label    = cleanName,
                        selected = cat in selectedCats,
                        onClick  = {
                            selectedCats = if (cat in selectedCats)
                                selectedCats - cat else selectedCats + cat
                        }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            val count = vm.questions.collectAsStateWithLifecycle().value
                .count { it.category in selectedCats }
            Text("$count questions selected", fontSize = 12.sp, color = c.muted)
            Spacer(Modifier.height(14.dp))
            GKKButton("Start Test →", onClick = {
                if (selectedCats.isNotEmpty()) {
                    vm.startTest(selectedMode, selectedCats.toList())
                    nav.navigate(Route.TEST_SESSION)
                }
            })
        }
        Spacer(Modifier.height(14.dp))

        // PYQ quick launch
        val pyqPapers by vm.pyqPapers.collectAsStateWithLifecycle()
        if (pyqPapers.isNotEmpty()) {
            GKKCard {
                SectionHeader("PYQ Papers — Quick Practice")
                pyqPapers.forEach { paper ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, c.border, RoundedCornerShape(10.dp))
                            .clickable {
                                vm.startPYQTest(paper)
                                nav.navigate(Route.TEST_SESSION)
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier          = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEF0FF)),
                            contentAlignment  = Alignment.Center
                        ) { Text("📄", fontSize = 22.sp) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(paper.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                            Text("${paper.questions.size} questions · ${paper.duration}", fontSize = 12.sp, color = c.muted)
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Practice", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.saff) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  TEST SESSION
// ═══════════════════════════════════════════════
@Composable
fun TestSessionScreen(vm: MainViewModel, nav: NavHostController) {
    val c            = gkkColors
    val pool         by vm.sessionPool.collectAsStateWithLifecycle()
    val currentIndex by vm.currentIndex.collectAsStateWithLifecycle()
    val sessionActive by vm.sessionActive.collectAsStateWithLifecycle()
    val currentQ     by vm.currentQuestion.collectAsStateWithLifecycle()
    val bookmarks    by vm.bookmarks.collectAsStateWithLifecycle()

    var chosenAnswer   by remember(currentIndex) { mutableStateOf<Int?>(null) }
    var revealed       by remember(currentIndex) { mutableStateOf(false) }
    var confidence     by remember(currentIndex) { mutableStateOf("") }
    var flagged        by remember(currentIndex) { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }

    // Navigate away when session ends
    LaunchedEffect(sessionActive) {
        if (!sessionActive && pool.isNotEmpty()) {
            nav.navigate(Route.TEST_RESULT) {
                popUpTo(Route.TEST_SESSION) { inclusive = true }
            }
        }
    }

    BackHandler { showQuitDialog = true }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title    = { Text("Quit Test?") },
            text     = { Text("Your progress will be saved but the test will end.") },
            confirmButton = {
                TextButton(onClick = { vm.endSession(); nav.popBackStack(Route.TEST, false) }) {
                    Text("Quit", color = c.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) { Text("Continue") }
            }
        )
    }

    val q = currentQ ?: return

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
    ) {
        // Progress bar + header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showQuitDialog = true }) {
                    Icon(Icons.Default.Close, contentDescription = "Quit", tint = c.muted)
                }
                Text(
                    "${currentIndex + 1} / ${pool.size}",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.muted
                )
                IconButton(onClick = { vm.toggleBookmark(q) }) {
                    Icon(
                        if (bookmarks.any { it.question == q.question }) Icons.Default.Bookmark
                        else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (bookmarks.any { it.question == q.question }) c.saff else c.muted
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // Progress bar
            Box(
                modifier = Modifier.fillMaxWidth().height(5.dp)
                    .clip(RoundedCornerShape(3.dp)).background(c.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((currentIndex + 1).toFloat() / pool.size)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(c.navy, c.saff)
                            )
                        )
                )
            }
        }

        // Question + options — scrollable
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Chapter badge
            Text(
                q.category.replace(Regex("^Ch\\.\\d+\\s*"), "").uppercase(),
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = c.saff, letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Question text
            GKKCard {
                Text(
                    q.question,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = c.text, lineHeight = 23.sp
                )
            }
            Spacer(Modifier.height(12.dp))

            // Options
            q.options.forEachIndexed { idx, opt ->
                val answered = chosenAnswer != null || revealed
                val isCorrect = idx == q.answer
                val isChosen  = idx == chosenAnswer

                val bgColor = when {
                    !answered           -> c.card
                    isCorrect           -> Color(0xFFF0FDF4)
                    isChosen && !isCorrect -> Color(0xFFFEF2F2)
                    else                -> c.card
                }
                val borderColor = when {
                    !answered           -> c.border
                    isCorrect           -> Color(0xFF16A34A)
                    isChosen && !isCorrect -> c.danger
                    else                -> c.border
                }
                val lblBg = when {
                    !answered           -> Color(0xFFEEF0FF)
                    isCorrect           -> Color(0xFF16A34A)
                    isChosen && !isCorrect -> c.danger
                    else                -> Color(0xFFEEF0FF)
                }
                val lblText = when {
                    answered && isCorrect           -> Color.White
                    answered && isChosen && !isCorrect -> Color.White
                    else                            -> c.navy
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .then(
                            if (!answered) Modifier.clickable {
                                chosenAnswer = idx
                                vm.submitAnswer(idx, flagged, confidence)
                            } else Modifier
                        )
                        .padding(13.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(26.dp).clip(CircleShape).background(lblBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(OPTION_LABELS[idx], fontSize = 11.sp, fontWeight = FontWeight.Bold, color = lblText)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(opt, fontSize = 13.5.sp, color = c.text, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                }
            }

            // Answer explanation
            AnimatedVisibility(visible = chosenAnswer != null || revealed) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        "✔ Sahi Jawab: ${OPTION_LABELS[q.answer]}) ${q.options.getOrElse(q.answer) { "" }}",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D)
                    )
                    if (q.explanation.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(q.explanation, fontSize = 12.5.sp, color = Color(0xFF374151), lineHeight = 19.sp)
                    }
                }
            }

            // Confidence buttons
            AnimatedVisibility(visible = chosenAnswer != null || revealed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("sure" to "✓ Sure", "unsure" to "? Unsure").forEach { (tag, label) ->
                        val sel = confidence == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (sel && tag == "sure")   Color(0xFF16A34A)
                                    else if (sel)               c.warn
                                    else                        c.card
                                )
                                .border(
                                    1.5.dp,
                                    if (tag == "sure") Color(0xFF16A34A) else c.warn,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { confidence = tag }
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = if (sel) Color.White
                                        else if (tag == "sure") Color(0xFF16A34A) else c.warn
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Bottom action bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.card)
                .border(width = 1.dp, color = c.border,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val answered = chosenAnswer != null || revealed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!answered) {
                    GKKOutlineButton("Reveal", onClick = { revealed = true }, modifier = Modifier.weight(1f))
                    GKKOutlineButton("Skip →",  onClick = { vm.submitAnswer(-1); vm.nextQuestion() }, modifier = Modifier.weight(1f))
                } else {
                    GKKButton(
                        if (currentIndex < pool.size - 1) "Next Question →" else "See Results →",
                        onClick  = { vm.nextQuestion() },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Flag button
                IconButton(
                    onClick = { flagged = !flagged },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (flagged) c.danger else Color(0xFFFEF2F2))
                ) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Flag",
                        tint = if (flagged) Color.White else c.danger
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  TEST RESULT
// ═══════════════════════════════════════════════
@Composable
fun TestResultScreen(vm: MainViewModel, nav: NavHostController) {
    val c       = gkkColors
    val answers by vm.answers.collectAsStateWithLifecycle()
    val pool    by vm.sessionPool.collectAsStateWithLifecycle()

    val correct  = answers.count { it.isCorrect }
    val wrong    = answers.count { !it.isCorrect && it.chosenIndex >= 0 }
    val skipped  = answers.count { it.chosenIndex < 0 }
    val total    = pool.size
    val accuracy = if (correct + wrong > 0) correct * 100 / (correct + wrong) else 0
    val pct      = if (total > 0) correct * 100 / total else 0

    val grade = when {
        pct >= 80 -> "Excellent! 🎉"
        pct >= 65 -> "Good work 👍"
        pct >= 50 -> "Keep practicing 📚"
        else      -> "More revision needed 💪"
    }
    val circleColor = when {
        pct >= 70 -> c.success
        pct >= 40 -> c.warn
        else      -> c.danger
    }

    var showReview by remember { mutableStateOf(false) }
    var reviewFilter by remember { mutableStateOf("all") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Score circle
        Column(
            modifier          = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier         = Modifier.size(128.dp)
                    .clip(CircleShape)
                    .border(6.dp, circleColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$pct%", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp, color = c.navy)
                    Text(grade, fontSize = 11.sp, color = c.muted, textAlign = TextAlign.Center)
                }
            }
        }

        // Stats grid
        GKKCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultStat("$correct",  "Correct",  Color(0xFF15803D), Color(0xFFF0FDF4), Modifier.weight(1f))
                ResultStat("$wrong",    "Wrong",    c.danger,          Color(0xFFFEF2F2), Modifier.weight(1f))
                ResultStat("$skipped",  "Skipped",  c.warn,            Color(0xFFFFF7ED), Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultStat("$total",     "Total",     c.navy,    Color(0xFFEEF0FF), Modifier.weight(1f))
                ResultStat("$accuracy%", "Accuracy",  c.success, Color(0xFFF0FDF4), Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(16.dp))

        // Action buttons
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GKKButton("Review Answers", onClick = { showReview = !showReview })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GKKOutlineButton("Try Again", onClick = { nav.popBackStack(Route.TEST, false) }, modifier = Modifier.weight(1f))
                GKKOutlineButton("Dashboard", onClick = { nav.popBackStack(Route.DASHBOARD, false) }, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(16.dp))

        // Review answers list
        if (showReview) {
            GKKCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Review Answers")
                // Filter tabs
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "All", "wrong" to "Wrong", "unsure" to "Unsure").forEach { (f, label) ->
                        FilterChip(label, reviewFilter == f) { reviewFilter = f }
                    }
                }
                Spacer(Modifier.height(14.dp))
                val filtered = answers.filter { ans ->
                    when (reviewFilter) {
                        "wrong"  -> !ans.isCorrect && ans.chosenIndex >= 0
                        "unsure" -> ans.confidenceTag == "unsure"
                        else     -> true
                    }
                }
                if (filtered.isEmpty()) {
                    Text("No questions match this filter.", fontSize = 13.sp, color = c.muted,
                        modifier = Modifier.padding(vertical = 12.dp))
                }
                filtered.forEachIndexed { i, ans ->
                    val status = when {
                        ans.isCorrect           -> "correct"
                        ans.chosenIndex < 0     -> "skipped"
                        else                    -> "wrong"
                    }
                    val leftColor = when (status) {
                        "correct" -> Color(0xFF16A34A)
                        "wrong"   -> c.danger
                        else      -> c.warn
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(c.card)
                            .border(1.dp, c.border, RoundedCornerShape(10.dp))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(4.dp)
                                .background(leftColor)
                        )
                        Column(modifier = Modifier.padding(13.dp)) {
                            Text("Q${i + 1}: ${ans.question.question}",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                GKKBadge(
                                    text = status.replaceFirstChar { it.uppercase() },
                                    bg   = leftColor
                                )
                                GKKBadge(
                                    text      = "Correct: ${OPTION_LABELS.getOrElse(ans.question.answer) { "?" }}) ${ans.question.options.getOrElse(ans.question.answer) { "" }}",
                                    bg        = Color(0xFFDCFCE7),
                                    textColor = Color(0xFF15803D)
                                )
                            }
                            if (ans.question.explanation.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(ans.question.explanation,
                                    fontSize = 11.5.sp, color = c.muted, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResultStat(value: String, label: String, color: Color, bg: Color, modifier: Modifier) {
    Column(
        modifier          = modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontFamily = Syne, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = color)
        Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.7f), modifier = Modifier.padding(top = 2.dp))
    }
}
