package com.gkk.mppsc.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gkk.mppsc.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentRepository(private val context: Context) {

    private val gson = Gson()

    // ── Generic asset loader ─────────────────────────────────────────
    private suspend fun loadAsset(fileName: String): String =
        withContext(Dispatchers.IO) {
            context.assets.open("data/$fileName").bufferedReader().use { it.readText() }
        }

    // ── Questions (421 MCQs) ─────────────────────────────────────────
    private var _questions: List<Question>? = null

    suspend fun getQuestions(): List<Question> {
        if (_questions != null) return _questions!!
        val json = loadAsset("questions.json")
        val type = object : TypeToken<List<Question>>() {}.type
        _questions = gson.fromJson(json, type)
        return _questions!!
    }

    suspend fun getCategories(): List<String> =
        getQuestions().map { it.category }.distinct()

    suspend fun getQuestionsByCategory(category: String): List<Question> =
        getQuestions().filter { it.category == category }

    // ── PYQ Papers ───────────────────────────────────────────────────
    private var _pyq: List<PYQPaper>? = null

    suspend fun getPYQPapers(): List<PYQPaper> {
        if (_pyq != null) return _pyq!!
        val json = loadAsset("pyq.json")
        val type = object : TypeToken<List<PYQPaper>>() {}.type
        _pyq = gson.fromJson(json, type)
        return _pyq!!
    }

    // ── Notes ────────────────────────────────────────────────────────
    private var _notes: List<Note>? = null

    suspend fun getNotes(): List<Note> {
        if (_notes != null) return _notes!!
        val json = loadAsset("notes.json")
        val type = object : TypeToken<List<Note>>() {}.type
        _notes = gson.fromJson(json, type)
        return _notes!!
    }

    // ── Syllabus ─────────────────────────────────────────────────────
    private var _syllabus: List<SyllabusPaper>? = null

    suspend fun getSyllabus(): List<SyllabusPaper> {
        if (_syllabus != null) return _syllabus!!
        val json = loadAsset("syllabus.json")
        val type = object : TypeToken<List<SyllabusPaper>>() {}.type
        _syllabus = gson.fromJson(json, type)
        return _syllabus!!
    }

    // ── Current Affairs ──────────────────────────────────────────────
    private var _ca: List<CurrentAffair>? = null

    suspend fun getCurrentAffairs(): List<CurrentAffair> {
        if (_ca != null) return _ca!!
        val json = loadAsset("current_affairs.json")
        val type = object : TypeToken<List<CurrentAffair>>() {}.type
        _ca = gson.fromJson(json, type)
        return _ca!!
    }
}
