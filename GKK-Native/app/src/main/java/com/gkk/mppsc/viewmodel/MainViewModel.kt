package com.gkk.mppsc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gkk.mppsc.data.models.*
import com.gkk.mppsc.data.repository.ContentRepository
import com.gkk.mppsc.data.repository.PrefsRepository
import com.gkk.mppsc.ui.theme.GKKTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val contentRepo = ContentRepository(application)
    private val prefsRepo   = PrefsRepository(application)

    // ── Theme ──────────────────────────────────────────────────────────
    val theme: StateFlow<GKKTheme> = prefsRepo.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, GKKTheme.DEFAULT)

    fun setTheme(t: GKKTheme) = viewModelScope.launch { prefsRepo.setTheme(t) }

    // ── Stats ──────────────────────────────────────────────────────────
    val stats: StateFlow<Pair<Int, Int>> = prefsRepo.statsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Pair(0, 0))

    val streak: StateFlow<Int> = prefsRepo.streakFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val heatmap: StateFlow<Map<String, Int>> = prefsRepo.heatmapFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val chapterStats: StateFlow<Map<String, Pair<Int, Int>>> = prefsRepo.chapterStatsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // ── Content loading ────────────────────────────────────────────────
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _pyqPapers = MutableStateFlow<List<PYQPaper>>(emptyList())
    val pyqPapers: StateFlow<List<PYQPaper>> = _pyqPapers.asStateFlow()

    private val _syllabus = MutableStateFlow<List<SyllabusPaper>>(emptyList())
    val syllabus: StateFlow<List<SyllabusPaper>> = _syllabus.asStateFlow()

    private val _currentAffairs = MutableStateFlow<List<CurrentAffair>>(emptyList())
    val currentAffairs: StateFlow<List<CurrentAffair>> = _currentAffairs.asStateFlow()

    init {
        loadAllContent()
        checkAndUpdateStreak()
    }

    private fun loadAllContent() = viewModelScope.launch {
        val qs = contentRepo.getQuestions()
        _questions.value  = qs
        _categories.value = qs.map { it.category }.distinct()
        _notes.value         = contentRepo.getNotes()
        _pyqPapers.value     = contentRepo.getPYQPapers()
        _syllabus.value      = contentRepo.getSyllabus()
        _currentAffairs.value= contentRepo.getCurrentAffairs()
    }

    // ── Quiz session ───────────────────────────────────────────────────
    private val _sessionPool    = MutableStateFlow<List<Question>>(emptyList())
    private val _currentIndex   = MutableStateFlow(0)
    private val _answers        = MutableStateFlow<List<AnsweredQuestion>>(emptyList())
    private val _sessionActive  = MutableStateFlow(false)

    val sessionPool:   StateFlow<List<Question>>       = _sessionPool.asStateFlow()
    val currentIndex:  StateFlow<Int>                  = _currentIndex.asStateFlow()
    val answers:       StateFlow<List<AnsweredQuestion>>= _answers.asStateFlow()
    val sessionActive: StateFlow<Boolean>              = _sessionActive.asStateFlow()

    val currentQuestion: StateFlow<Question?> = combine(_sessionPool, _currentIndex) { pool, idx ->
        pool.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun startTest(mode: TestMode, selectedCategories: List<String>) {
        val allQs = _questions.value.filter {
            selectedCategories.isEmpty() || it.category in selectedCategories
        }.shuffled()
        val pool = if (mode.count > 0) allQs.take(mode.count) else allQs
        _sessionPool.value   = pool
        _currentIndex.value  = 0
        _answers.value       = emptyList()
        _sessionActive.value = true
        recordStudyToday()
    }

    fun startPYQTest(paper: PYQPaper) {
        val pool = paper.questions.map { pyq ->
            Question(
                category    = "${paper.year} ${paper.paper}",
                question    = pyq.question,
                options     = pyq.options,
                answer      = pyq.answer,
                explanation = pyq.explanation
            )
        }
        _sessionPool.value   = pool
        _currentIndex.value  = 0
        _answers.value       = emptyList()
        _sessionActive.value = true
        recordStudyToday()
    }

    fun submitAnswer(chosenIndex: Int, isFlagged: Boolean = false, confidence: String = "") {
        val q = currentQuestion.value ?: return
        val answered = AnsweredQuestion(
            question      = q,
            chosenIndex   = chosenIndex,
            isCorrect     = chosenIndex == q.answer,
            isFlagged     = isFlagged,
            confidenceTag = confidence
        )
        _answers.value = _answers.value + answered
        viewModelScope.launch {
            prefsRepo.recordAnswer(answered.isCorrect)
            prefsRepo.recordChapterAnswer(q.category, answered.isCorrect)
            if (!answered.isCorrect) addToSrs(q)
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < _sessionPool.value.size - 1) {
            _currentIndex.value++
        } else {
            _sessionActive.value = false
        }
    }

    fun endSession() { _sessionActive.value = false }

    // ── Bookmarks ──────────────────────────────────────────────────────
    val bookmarks: StateFlow<List<Bookmark>> = prefsRepo.bookmarksFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleBookmark(q: Question) = viewModelScope.launch {
        val current = bookmarks.value.toMutableList()
        val idx = current.indexOfFirst { it.question == q.question }
        if (idx >= 0) current.removeAt(idx) else current.add(
            Bookmark(q.question, q.options, q.answer, q.category, q.explanation)
        )
        prefsRepo.saveBookmarks(current)
    }

    fun isBookmarked(q: Question) = bookmarks.value.any { it.question == q.question }

    fun removeBookmark(index: Int) = viewModelScope.launch {
        val current = bookmarks.value.toMutableList()
        if (index in current.indices) { current.removeAt(index); prefsRepo.saveBookmarks(current) }
    }

    fun clearAllBookmarks() = viewModelScope.launch { prefsRepo.saveBookmarks(emptyList()) }

    // ── SRS ────────────────────────────────────────────────────────────
    private val SRS_INTERVALS = listOf(1L, 3L, 7L, 14L, 30L)   // days

    val srsEntries: StateFlow<List<SrsEntry>> = prefsRepo.srsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val srsDueCount: StateFlow<Int> = srsEntries.map { entries ->
        entries.count { it.nextReview <= System.currentTimeMillis() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private fun addToSrs(q: Question) = viewModelScope.launch {
        val current = srsEntries.value.toMutableList()
        val exists  = current.any { it.question.question == q.question }
        if (!exists) {
            current.add(SrsEntry(question = q, level = 0, nextReview = System.currentTimeMillis()))
            prefsRepo.saveSrs(current)
        }
    }

    fun submitSrsAnswer(entry: SrsEntry, isCorrect: Boolean) = viewModelScope.launch {
        val current = srsEntries.value.toMutableList()
        val idx = current.indexOfFirst { it.question.question == entry.question.question }
        if (idx < 0) return@launch
        val newLevel = if (isCorrect) minOf(entry.level + 1, SRS_INTERVALS.size - 1) else 0
        val nextMs   = System.currentTimeMillis() + SRS_INTERVALS[newLevel] * 86_400_000L
        current[idx] = entry.copy(level = newLevel, nextReview = nextMs, lastReview = System.currentTimeMillis())
        prefsRepo.saveSrs(current)
    }

    // ── Streak & heatmap ───────────────────────────────────────────────
    private fun recordStudyToday() = viewModelScope.launch {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefsRepo.recordStudyDay(today)
        checkAndUpdateStreak()
    }

    private fun checkAndUpdateStreak() = viewModelScope.launch {
        val today     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate  = prefsRepo.getStreakLastDate()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time)
        }
        val current = streak.value
        val newStreak = when (lastDate) {
            today     -> current                    // already counted today
            yesterday -> current + 1                // consecutive day
            ""        -> 1                          // first ever
            else      -> 1                          // streak broken, reset to 1
        }
        prefsRepo.updateStreak(newStreak, today)
    }

    // ── Notification pref ──────────────────────────────────────────────
    val notifEnabled: StateFlow<Boolean> = prefsRepo.notifEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setNotifEnabled(v: Boolean) = viewModelScope.launch { prefsRepo.setNotifEnabled(v) }

    // ── Weak areas (chapters with lowest accuracy) ─────────────────────
    val weakAreas: StateFlow<List<Pair<String, Float>>> = chapterStats.map { stats ->
        stats.entries
            .filter { it.value.first > 0 }
            .map { (ch, v) -> Pair(ch, if (v.first > 0) v.second.toFloat() / v.first else 0f) }
            .sortedBy { it.second }
            .take(10)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Daily 10 ───────────────────────────────────────────────────────
    fun getDailyPool(): List<Question> =
        _questions.value.shuffled().take(10)

    // ── Flashcard pool ─────────────────────────────────────────────────
    fun getFlashcardPool(selectedCategories: List<String>): List<Question> {
        val all = _questions.value
        return if (selectedCategories.isEmpty()) all.shuffled()
        else all.filter { it.category in selectedCategories }.shuffled()
    }
}
