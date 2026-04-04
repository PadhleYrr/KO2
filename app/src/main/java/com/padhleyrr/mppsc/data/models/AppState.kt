package com.padhleyrr.mppsc.data.models

/** Represents one answered question during a test session */
data class AnsweredQuestion(
    val question:      Question,
    val chosenIndex:   Int,          // -1 = skipped
    val isCorrect:     Boolean,
    val isFlagged:     Boolean       = false,
    val confidenceTag: String        = ""  // "sure" | "unsure" | ""
)

/** Test mode selected from the test home screen */
enum class TestMode(val label: String, val count: Int) {
    FULL("Full Test", 0),
    LONG("Long — 100 Qs", 100),
    PRACTICE("Practice — 50 Qs", 50),
    QUICK("Quick — 25 Qs", 25),
    DAILY("Daily 10", 10)
}

/** SRS (Spaced Repetition) entry for smart review */
data class SrsEntry(
    val question:   Question,
    val level:      Int  = 0,           // 0–4 (interval index)
    val nextReview: Long = System.currentTimeMillis(),
    val lastReview: Long? = null
)
