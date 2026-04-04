package com.padhleyrr.mppsc.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

// ═══════════════════════════════════════════════════════
//  SUBSCRIPTION REPOSITORY
//  Mirrors GKK auth.js: trial (7 days) → premium (₹100/6 months)
//  Admin check via Firestore config/admins.emails[]
// ═══════════════════════════════════════════════════════

data class UserRecord(
    val trialStart: Long = 0L,
    val premiumExpiry: Long = 0L,
    val name: String = "",
    val email: String = "",
    val registeredAt: Long = 0L,
    val lastSeen: Long = 0L,
    val banned: Boolean = false,
    val adminNote: String = ""
)

data class SubscriptionState(
    val isAdmin: Boolean = false,
    val isPremium: Boolean = false,
    val isTrialActive: Boolean = false,
    val trialMsLeft: Long = 0L,
    val premiumDaysLeft: Int = 0,
    val isLoaded: Boolean = false
)

object SubscriptionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    private val _userRecord = MutableStateFlow(UserRecord())
    val userRecord: StateFlow<UserRecord> = _userRecord.asStateFlow()

    private val ADMIN_EMAILS = setOf<String>() // Loaded from Firestore

    // ── CONSTANTS ────────────────────────────────────────────────
    const val TRIAL_DAYS       = 7
    const val PREMIUM_PRICE    = 10000          // paise (₹100)
    const val PREMIUM_MONTHS   = 6

    val FEATURES = listOf(
        "📚" to "All 421+ MCQ Questions",
        "📄" to "PYQ Papers 2021–2024",
        "⏱️" to "Unlimited Timed Mocks",
        "🔁" to "Smart Revision Mode",
        "📖" to "Complete Study Notes",
        "🃏" to "Flashcard Mode",
        "📊" to "Progress Analytics",
        "🗺️" to "Map Quiz — MP & India"
    )

    // ── LOAD USER STATE FROM FIRESTORE ───────────────────────────
    suspend fun loadUser(uid: String, email: String, displayName: String?) {
        try {
            val ref  = db.collection("users").doc(uid)
            val snap = ref.get().await()
            val now  = System.currentTimeMillis()

            if (snap.exists() && snap.getLong("trialStart") != null) {
                val trialStart     = snap.getLong("trialStart") ?: now
                val premiumExpiry  = snap.getLong("premiumExpiry") ?: 0L
                val name           = snap.getString("name") ?: displayName ?: ""
                val banned         = snap.getBoolean("banned") ?: false
                val adminNote      = snap.getString("adminNote") ?: ""
                val registeredAt   = snap.getLong("registeredAt") ?: trialStart
                _userRecord.value  = UserRecord(trialStart, premiumExpiry, name,
                    email, registeredAt, now, banned, adminNote)
                ref.update(mapOf("lastSeen" to now, "sessionCount" to FieldValue.increment(1)))
                    .addOnFailureListener { }
            } else {
                // New user — start trial
                val trialStart = now
                val record = UserRecord(trialStart, 0L, displayName ?: "", email,
                    now, now, false, "")
                _userRecord.value = record
                ref.set(mapOf(
                    "trialStart"   to trialStart,
                    "premiumExpiry" to 0L,
                    "email"        to email,
                    "name"         to (displayName ?: ""),
                    "registeredAt" to now,
                    "lastSeen"     to now
                )).addOnFailureListener { }
            }

            // Write email index for admin lookup
            writeEmailIndex(email, uid)

            // Check admin status
            val isAdmin = checkAdmin(email)

            // Update subscription state
            _state.value = computeState(isAdmin)

        } catch (e: Exception) {
            Log.w("SubscriptionRepo", "Load failed: ${e.message}")
            _state.value = _state.value.copy(isLoaded = true)
        }
    }

    private suspend fun checkAdmin(email: String): Boolean {
        return try {
            val snap = db.collection("config").doc("admins").get().await()
            if (snap.exists()) {
                val emails = snap.get("emails") as? List<*> ?: emptyList<Any>()
                emails.any { it.toString().trim().lowercase() == email.trim().lowercase() }
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private fun writeEmailIndex(email: String, uid: String) {
        val key = email.trim().lowercase().replace("[.@]".toRegex(), "_")
        db.collection("emailIndex").document(key)
            .set(mapOf("uid" to uid, "email" to email))
            .addOnFailureListener { }
    }

    private fun computeState(isAdmin: Boolean = _state.value.isAdmin): SubscriptionState {
        val record  = _userRecord.value
        val now     = System.currentTimeMillis()
        val trialMs = (record.trialStart + TRIAL_DAYS * 86_400_000L - now).coerceAtLeast(0)
        val isPrem  = record.premiumExpiry > now
        val premDays = if (isPrem) ((record.premiumExpiry - now) / 86_400_000L).toInt() else 0
        return SubscriptionState(
            isAdmin       = isAdmin,
            isPremium     = isPrem,
            isTrialActive = trialMs > 0,
            trialMsLeft   = trialMs,
            premiumDaysLeft = premDays,
            isLoaded      = true
        )
    }

    fun hasAccess(): Boolean {
        val s = _state.value
        return s.isAdmin || s.isPremium || s.isTrialActive
    }

    fun trialDaysLeft(): Int = (_state.value.trialMsLeft / 86_400_000L).toInt().coerceAtLeast(0) +
            if (_state.value.trialMsLeft % 86_400_000L > 0) 1 else 0

    // ── GRANT PREMIUM (called after payment success) ──────────────
    suspend fun grantPremium(uid: String, months: Int) {
        val now    = System.currentTimeMillis()
        val expiry = now + months * 30L * 86_400_000L
        val record = _userRecord.value.copy(premiumExpiry = expiry)
        _userRecord.value = record
        _state.value = computeState()
        try {
            db.collection("users").document(uid)
                .update("premiumExpiry", expiry).await()
        } catch (e: Exception) { Log.w("SubscriptionRepo", "Firestore update failed") }
    }

    // ── REDEEM COUPON ─────────────────────────────────────────────
    suspend fun redeemCoupon(uid: String, code: String): Result<Int> {
        return try {
            val ref  = db.collection("config").document("coupons")
            val snap = ref.get().await()
            @Suppress("UNCHECKED_CAST")
            val codes = (snap.get("codes") as? List<Map<String, Any>>) ?: emptyList()
            val idx = codes.indexOfFirst { it["code"].toString() == code.trim().uppercase() }
            if (idx == -1) return Result.failure(Exception("Invalid coupon code."))
            val entry = codes[idx]
            if (entry["usedBy"] != null) return Result.failure(Exception("Coupon already used."))
            val days = (entry["days"] as? Long)?.toInt() ?: 0
            val now = System.currentTimeMillis()
            val expiry = maxOf(_userRecord.value.premiumExpiry, now) + days * 86_400_000L
            // Mark used
            val updated = codes.toMutableList()
            updated[idx] = entry.toMutableMap().apply { put("usedBy", uid) }
            ref.update("codes", updated).await()
            // Grant
            _userRecord.value = _userRecord.value.copy(premiumExpiry = expiry)
            _state.value = computeState()
            db.collection("users").document(uid).update("premiumExpiry", expiry).await()
            Result.success(days)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun reset() {
        _state.value = SubscriptionState()
        _userRecord.value = UserRecord()
    }
}
