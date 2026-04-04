package com.padhleyrr.mppsc.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.padhleyrr.mppsc.data.repository.SubscriptionRepository
import com.padhleyrr.mppsc.ui.theme.DMSans
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// ═══════════════════════════════════════════════════════
//  ADMIN SCREEN — 4 tabs identical to GKK showAdminPanel()
//  Tab 1: User Lookup  Tab 2: Stats  Tab 3: Tools  Tab 4: App Settings
// ═══════════════════════════════════════════════════════

private val db get() = FirebaseFirestore.getInstance()

enum class AdminTab { USER, STATS, TOOLS, SETTINGS }

@Composable
fun AdminScreen(onClose: () -> Unit) {
    val c = gkkColors
    var activeTab by remember { mutableStateOf(AdminTab.USER) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFF))) {

        // ── Header ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF3730A3), Color(0xFF6D28D9))))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("⚙️ Admin Panel", fontFamily = Syne,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                        Text("MP GK Portal — Control Centre",
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 2.dp))
                    }
                    Box(
                        modifier = Modifier.size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) { Text("✕", fontSize = 14.sp, color = Color.White) }
                }

                Spacer(Modifier.height(14.dp))

                // Tab bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AdminTab.entries.forEach { tab ->
                        val label = when (tab) {
                            AdminTab.USER     -> "👤 User"
                            AdminTab.STATS    -> "📊 Stats"
                            AdminTab.TOOLS    -> "🛠 Tools"
                            AdminTab.SETTINGS -> "⚙️ App"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (tab == activeTab) Color.White else Color.Transparent)
                                .clickable { activeTab = tab }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = if (tab == activeTab) Color(0xFF3730A3)
                                        else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // ── Tab content ─────────────────────────────────────────
        when (activeTab) {
            AdminTab.USER     -> AdminUserTab()
            AdminTab.STATS    -> AdminStatsTab()
            AdminTab.TOOLS    -> AdminToolsTab()
            AdminTab.SETTINGS -> AdminSettingsTab()
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TAB 1 — USER LOOKUP
// ══════════════════════════════════════════════════════════════
@Composable
private fun AdminUserTab() {
    val c = gkkColors
    val scope = rememberCoroutineScope()

    var emailQuery by remember { mutableStateOf("") }
    var lookupState by remember { mutableStateOf<LookupState>(LookupState.Idle) }
    var targetUid   by remember { mutableStateOf<String?>(null) }
    var targetData  by remember { mutableStateOf<Map<String, Any>?>(null) }
    var actionResult by remember { mutableStateOf<ActionResult?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search card
        AdminCard {
            Text("🔍 User Lookup", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF6D28D9), modifier = Modifier.padding(bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = emailQuery, onValueChange = { emailQuery = it },
                    placeholder = { Text("Email or name…", fontSize = 13.sp) },
                    singleLine = true, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Button(
                    onClick = {
                        scope.launch {
                            lookupState = LookupState.Loading
                            targetUid = null; targetData = null; actionResult = null
                            try {
                                val key = emailQuery.trim().lowercase()
                                    .replace("[.@]".toRegex(), "_")
                                val idx = db.collection("emailIndex").document(key).get().await()
                                if (!idx.exists()) { lookupState = LookupState.NotFound; return@launch }
                                val uid = idx.getString("uid") ?: run { lookupState = LookupState.NotFound; return@launch }
                                val userSnap = db.collection("users").document(uid).get().await()
                                targetUid  = uid
                                targetData = if (userSnap.exists()) userSnap.data else emptyMap()
                                lookupState = LookupState.Found
                            } catch (e: Exception) { lookupState = LookupState.Error(e.message ?: "Unknown error") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3730A3)),
                    shape  = RoundedCornerShape(10.dp)
                ) { Text("Look up", fontWeight = FontWeight.Bold) }
            }

            when (val s = lookupState) {
                LookupState.Loading  -> LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 10.dp))
                LookupState.NotFound -> Text("❌ No user found with that email.",
                    fontSize = 13.sp, color = Color(0xFFDC2626), modifier = Modifier.padding(top = 8.dp))
                is LookupState.Error -> Text("❌ ${s.msg}",
                    fontSize = 13.sp, color = Color(0xFFDC2626), modifier = Modifier.padding(top = 8.dp))
                else -> {}
            }
        }

        // Profile card + actions
        if (lookupState == LookupState.Found && targetData != null && targetUid != null) {
            val data = targetData!!
            val uid  = targetUid!!

            AdminProfileCard(uid = uid, email = emailQuery.trim().lowercase(), data = data)

            AdminActionsCard(uid = uid, data = data, actionResult = actionResult) { result ->
                actionResult = result
                scope.launch {
                    // Reload
                    val snap = db.collection("users").document(uid).get().await()
                    targetData = if (snap.exists()) snap.data else emptyMap()
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

sealed class LookupState {
    object Idle : LookupState()
    object Loading : LookupState()
    object NotFound : LookupState()
    object Found : LookupState()
    data class Error(val msg: String) : LookupState()
}

data class ActionResult(val ok: Boolean, val msg: String)

@Composable
private fun AdminProfileCard(uid: String, email: String, data: Map<String, Any>) {
    val now          = System.currentTimeMillis()
    val name         = data["name"]?.toString() ?: email.substringBefore('@')
    val trialStart   = (data["trialStart"] as? Long) ?: 0L
    val premiumExpiry= (data["premiumExpiry"] as? Long) ?: 0L
    val banned       = data["banned"] as? Boolean ?: false
    val adminNote    = data["adminNote"]?.toString() ?: "—"
    val registeredAt = (data["registeredAt"] as? Long) ?: trialStart
    val lastSeen     = (data["lastSeen"] as? Long) ?: 0L
    val trialDays    = if (trialStart > 0) ((trialStart + 7*86_400_000L - now) / 86_400_000L).coerceAtLeast(0) else 0L
    val premiumDays  = if (premiumExpiry > now) ((premiumExpiry - now) / 86_400_000L) else 0L
    val status       = when {
        banned       -> "BANNED"
        premiumDays > 0 -> "PREMIUM"
        trialDays > 0   -> "TRIAL"
        else            -> "EXPIRED"
    }
    val (statusColor, statusBg) = when (status) {
        "PREMIUM" -> Color.White to Color(0xFF059669)
        "TRIAL"   -> Color.White to Color(0xFFD97706)
        "BANNED"  -> Color.White to Color(0xFF1E293B)
        else      -> Color.White to Color(0xFFDC2626)
    }

    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        // Profile header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF3730A3), Color(0xFF6D28D9))),
                    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(name.first().uppercaseChar().toString(),
                        fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp, color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp, color = Color.White, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(email, fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(statusBg).padding(horizontal = 10.dp, vertical = 3.dp)
                ) { Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor) }
            }
        }

        // Stats grid
        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val stats = listOf(
            "Registered" to if (registeredAt > 0) df.format(Date(registeredAt)) else "—",
            "Last Seen"  to timeAgo(lastSeen),
            "Trial Left" to if (trialDays > 0) "${trialDays}d" else "Expired",
            "Premium"    to if (premiumDays > 0) "${premiumDays}d" else "None",
            "UID"        to uid.take(10) + "…",
            "Note"       to adminNote.take(30)
        )
        Column(modifier = Modifier.padding(12.dp)) {
            stats.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)) {
                    row.forEach { (label, value) ->
                        Column(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                                .background(Color(0xFFF8FAFF)).padding(9.dp)
                        ) {
                            Text(label.uppercase(), fontSize = 9.sp, color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AdminActionsCard(
    uid: String, data: Map<String, Any>,
    actionResult: ActionResult?,
    onAction: (ActionResult) -> Unit
) {
    val scope = rememberCoroutineScope()

    fun doAction(action: String, extra: Long = 0L) {
        scope.launch {
            try {
                val ref  = db.collection("users").document(uid)
                val snap = ref.get().await()
                val d    = snap.data ?: emptyMap()
                val now  = System.currentTimeMillis()
                val curPrem = (d["premiumExpiry"] as? Long) ?: 0L
                val curTrial= (d["trialStart"] as? Long) ?: now
                when (action) {
                    "p1"  -> ref.update("premiumExpiry", maxOf(curPrem, now) + 1*30*86_400_000L).await()
                    "p3"  -> ref.update("premiumExpiry", maxOf(curPrem, now) + 3*30*86_400_000L).await()
                    "p6"  -> ref.update("premiumExpiry", maxOf(curPrem, now) + 6*30*86_400_000L).await()
                    "p12" -> ref.update("premiumExpiry", maxOf(curPrem, now) + 12*30*86_400_000L).await()
                    "pCustom" -> ref.update("premiumExpiry", maxOf(curPrem, now) + extra*86_400_000L).await()
                    "pRevoke" -> ref.update("premiumExpiry", 0L).await()
                    "t3"  -> ref.update("trialStart", curTrial - 3*86_400_000L).await()
                    "t7"  -> ref.update("trialStart", curTrial - 7*86_400_000L).await()
                    "t30" -> ref.update("trialStart", curTrial - 30*86_400_000L).await()
                    "tReset"-> ref.update("trialStart", now).await()
                    "ban" -> ref.update("banned", true, "bannedAt", now).await()
                    "unban"-> ref.update("banned", false).await()
                }
                onAction(ActionResult(true, "✅ Done! User sees changes on next open."))
            } catch (e: Exception) {
                onAction(ActionResult(false, "❌ ${e.message}"))
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Premium actions
        AdminCard {
            Text("💎 PREMIUM ACCESS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF059669), letterSpacing = 0.6.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("+1 Month" to "p1", "+3 Months" to "p3").forEach { (label, act) ->
                    OutlinedButton(onClick = { doAction(act) }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF6EE7B7)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF065F46))
                    ) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("+6 Months" to "p6", "+12 Months" to "p12").forEach { (label, act) ->
                    OutlinedButton(onClick = { doAction(act) }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF6EE7B7)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF065F46))
                    ) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedButton(onClick = { doAction("p6") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF1E40AF))
                ) { Text("Custom Days…", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = { doAction("pRevoke") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFF991B1B))
                ) { Text("🚫 Revoke", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }

        // Trial control
        AdminCard {
            Text("⏳ TRIAL CONTROL", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706), letterSpacing = 0.6.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("+3 Days" to "t3", "+7 Days" to "t7", "+30 Days" to "t30").forEach { (label, act) ->
                    OutlinedButton(onClick = { doAction(act) }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFCD34D)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFFBEB), contentColor = Color(0xFF92400E))
                    ) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.height(7.dp))
            OutlinedButton(onClick = { doAction("tReset") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(1.5.dp, Color(0xFFA5B4FC)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFF0F4FF), contentColor = Color(0xFF3730A3))
            ) { Text("↺ Reset to Fresh 7-Day Trial", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        // Account actions
        AdminCard {
            Text("🔧 ACCOUNT ACTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B), letterSpacing = 0.6.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedButton(onClick = { doAction("ban") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFF991B1B))
                ) { Text("🚫 Ban User", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = { doAction("unban") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF0FDF4), contentColor = Color(0xFF166534))
                ) { Text("✅ Unban", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }

        // Action result
        actionResult?.let { res ->
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (res.ok) Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
                    .padding(12.dp)
            ) {
                Text(res.msg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (res.ok) Color(0xFF166534) else Color(0xFFDC2626))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TAB 2 — STATS
// ══════════════════════════════════════════════════════════════
@Composable
private fun AdminStatsTab() {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var stats   by remember { mutableStateOf<AdminStats?>(null) }

    LaunchedEffect(Unit) {
        try {
            val snap  = db.collection("users").get().await()
            val users = snap.documents.map { it.id to (it.data ?: emptyMap<String, Any>()) }
            val now   = System.currentTimeMillis()
            val total   = users.size
            val premium = users.count { (_, d) -> ((d["premiumExpiry"] as? Long) ?: 0L) > now }
            val trial   = users.count { (_, d) ->
                val ts = (d["trialStart"] as? Long) ?: 0L
                val pe = (d["premiumExpiry"] as? Long) ?: 0L
                ts > 0 && (ts + 7*86_400_000L) > now && pe <= now
            }
            val expired  = total - premium - trial
            val banned   = users.count { (_, d) -> d["banned"] as? Boolean == true }
            val today    = users.count { (_, d) -> ((d["lastSeen"] as? Long) ?: 0L) > now - 86_400_000L }
            val week     = users.count { (_, d) -> ((d["lastSeen"] as? Long) ?: 0L) > now - 7*86_400_000L }
            val newToday = users.count { (_, d) -> ((d["registeredAt"] as? Long) ?: 0L) > now - 86_400_000L }
            val newWeek  = users.count { (_, d) -> ((d["registeredAt"] as? Long) ?: 0L) > now - 7*86_400_000L }

            val dayValues = (6 downTo 0).map { i ->
                val d0 = now - i * 86_400_000L
                val d1 = d0 + 86_400_000L
                users.count { (_, d) -> ((d["lastSeen"] as? Long) ?: 0L) in d0 until d1 }
            }

            val recent = users
                .filter { (_, d) -> (d["registeredAt"] as? Long) != null }
                .sortedByDescending { (_, d) -> d["registeredAt"] as? Long ?: 0L }
                .take(8)
                .map { (uid, d) ->
                    val pe = (d["premiumExpiry"] as? Long) ?: 0L
                    val ts = (d["trialStart"] as? Long) ?: 0L
                    val dot = when {
                        pe > now -> "💎"
                        ts > 0 && (ts + 7*86_400_000L) > now -> "⏳"
                        else -> "🔴"
                    }
                    Triple(dot, d["name"]?.toString() ?: (d["email"]?.toString()?.substringBefore('@') ?: "User"),
                        timeAgo(d["registeredAt"] as? Long ?: 0L))
                }

            stats = AdminStats(total, premium, trial, expired, banned, today, week, newToday, newWeek, dayValues, recent)
        } catch (_: Exception) {}
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6D28D9))
        }
        return
    }

    val s = stats ?: return

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User overview
        SectionLabel("👥 USER OVERVIEW", Color(0xFF6D28D9))
        TwoColGrid(listOf(
            Triple("Total Users",  s.total.toString(),   Color(0xFF3730A3)),
            Triple("Active Today", s.today.toString(),   Color(0xFF059669)),
            Triple("💎 Premium",   s.premium.toString(), Color(0xFF059669)),
            Triple("⏳ On Trial",  s.trial.toString(),   Color(0xFFD97706)),
            Triple("🔴 Expired",   s.expired.toString(), Color(0xFFDC2626)),
            Triple("🚫 Banned",    s.banned.toString(),  Color(0xFF64748B))
        ))

        // Revenue & growth
        SectionLabel("💰 REVENUE & GROWTH", Color(0xFF6D28D9))
        TwoColGrid(listOf(
            Triple("New Today",    s.newToday.toString(), Color(0xFF3730A3)),
            Triple("New This Week",s.newWeek.toString(),  Color(0xFF3730A3)),
            Triple("Active (7d)",  s.week.toString(),     Color(0xFF059669)),
            Triple("Conversion",
                if (s.total > 0) "${s.premium * 100 / s.total}%" else "—",
                Color(0xFFD97706))
        ))

        // Bar chart
        SectionLabel("📈 DAILY ACTIVE USERS (7d)", Color(0xFF6D28D9))
        AdminCard {
            val maxVal = (s.dayValues.maxOrNull() ?: 1).coerceAtLeast(1)
            val dayLabels = (6 downTo 0).map { i ->
                SimpleDateFormat("EEE", Locale.getDefault())
                    .format(Date(System.currentTimeMillis() - i * 86_400_000L))
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(90.dp).padding(bottom = 6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                s.dayValues.forEachIndexed { i, v ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (v > 0) Text(v.toString(), fontSize = 9.sp, color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height((v.toFloat() / maxVal * 60f).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(if (v == maxVal) Color(0xFF6D28D9) else Color(0xFFC4B5FD))
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dayLabels.forEach { label ->
                    Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }

        // Recent signups
        SectionLabel("🕐 RECENT SIGNUPS", Color(0xFF6D28D9))
        AdminCard {
            s.recent.forEach { (dot, name, time) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(dot, fontSize = 14.sp)
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B), modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(time, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

data class AdminStats(
    val total: Int, val premium: Int, val trial: Int, val expired: Int, val banned: Int,
    val today: Int, val week: Int, val newToday: Int, val newWeek: Int,
    val dayValues: List<Int>, val recent: List<Triple<String, String, String>>
)

// ══════════════════════════════════════════════════════════════
//  TAB 3 — TOOLS
// ══════════════════════════════════════════════════════════════
@Composable
private fun AdminToolsTab() {
    val scope = rememberCoroutineScope()
    var couponCode  by remember { mutableStateOf("") }
    var couponDays  by remember { mutableStateOf("") }
    var coupons     by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var couponMsg   by remember { mutableStateOf("") }
    var revAmount   by remember { mutableStateOf("") }
    var revNote     by remember { mutableStateOf("") }
    var revenueMsg  by remember { mutableStateOf("") }
    var revTotal    by remember { mutableStateOf(0) }
    var revMonthly  by remember { mutableStateOf(0) }

    // Load coupons + revenue
    LaunchedEffect(Unit) {
        try {
            val snap = db.collection("config").document("coupons").get().await()
            @Suppress("UNCHECKED_CAST")
            coupons = (snap.get("codes") as? List<Map<String, Any>>) ?: emptyList()
        } catch (_: Exception) {}
        try {
            val snap = db.collection("config").document("revenue").get().await()
            @Suppress("UNCHECKED_CAST")
            val entries = (snap.get("entries") as? List<Map<String, Any>>) ?: emptyList()
            revTotal   = entries.sumOf { (it["amount"] as? Long)?.toInt() ?: 0 }
            val now = System.currentTimeMillis()
            revMonthly = entries.filter { ((it["date"] as? Long) ?: 0L) > now - 30*86_400_000L }
                .sumOf { (it["amount"] as? Long)?.toInt() ?: 0 }
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Coupons
        AdminCard {
            SectionLabel("🎟 COUPON CODES", Color(0xFF7C3AED))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = couponCode, onValueChange = { couponCode = it.uppercase() },
                    placeholder = { Text("Code e.g. DIWALI50", fontSize = 12.sp) },
                    singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp))
                OutlinedTextField(value = couponDays, onValueChange = { couponDays = it },
                    placeholder = { Text("Days", fontSize = 12.sp) },
                    singleLine = true, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(9.dp))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val code = couponCode.trim(); val days = couponDays.trim().toIntOrNull() ?: 0
                    if (code.isEmpty() || days <= 0) { couponMsg = "Enter code and days"; return@Button }
                    scope.launch {
                        try {
                            val ref  = db.collection("config").document("coupons")
                            val snap = ref.get().await()
                            @Suppress("UNCHECKED_CAST")
                            val existing = (snap.get("codes") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                            if (existing.any { it["code"] == code }) { couponMsg = "Code already exists!"; return@launch }
                            existing.add(mapOf<String, Any>("code" to code, "days" to days.toLong(), "createdAt" to System.currentTimeMillis()))
                            ref.set(mapOf("codes" to existing)).await()
                            coupons = existing; couponCode = ""; couponDays = ""
                            couponMsg = "✅ Coupon created!"
                        } catch (e: Exception) { couponMsg = "❌ ${e.message}" }
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) { Text("+ Create Coupon", fontWeight = FontWeight.Bold) }
            if (couponMsg.isNotEmpty()) {
                Text(couponMsg, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp),
                    color = if (couponMsg.startsWith("✅")) Color(0xFF059669) else Color(0xFFDC2626))
            }
            Spacer(Modifier.height(10.dp))
            if (coupons.isEmpty()) {
                Text("No coupons yet.", fontSize = 12.sp, color = Color(0xFF94A3B8))
            } else {
                coupons.forEach { entry ->
                    val code  = entry["code"]?.toString() ?: ""
                    val days  = (entry["days"] as? Long)?.toInt() ?: 0
                    val used  = entry["usedBy"] != null
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(code, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                            fontSize = 13.sp, color = Color(0xFF7C3AED), modifier = Modifier.weight(1f))
                        Text("→ ${days}d", fontSize = 11.sp, color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(end = 8.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                .background(if (used) Color(0xFFFEF2F2) else Color(0xFFECFDF5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) { Text(if (used) "Used" else "Active", fontSize = 10.sp,
                            color = if (used) Color(0xFFDC2626) else Color(0xFF059669),
                            fontWeight = FontWeight.Bold) }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }

        // Revenue tracker
        AdminCard {
            SectionLabel("💰 REVENUE TRACKER", Color(0xFF059669))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = revAmount, onValueChange = { revAmount = it },
                    placeholder = { Text("Amount ₹", fontSize = 12.sp) },
                    singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp))
                OutlinedTextField(value = revNote, onValueChange = { revNote = it },
                    placeholder = { Text("Note", fontSize = 12.sp) },
                    singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val amt = revAmount.trim().toIntOrNull() ?: 0
                    if (amt <= 0) { revenueMsg = "Enter an amount"; return@Button }
                    scope.launch {
                        try {
                            val ref  = db.collection("config").document("revenue")
                            val snap = ref.get().await()
                            @Suppress("UNCHECKED_CAST")
                            val entries = (snap.get("entries") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                            entries.add(mapOf("amount" to amt.toLong(), "note" to revNote, "date" to System.currentTimeMillis()))
                            ref.set(mapOf("entries" to entries)).await()
                            revTotal += amt; revAmount = ""; revNote = ""
                            revenueMsg = "✅ Payment logged!"
                        } catch (e: Exception) { revenueMsg = "❌ ${e.message}" }
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) { Text("+ Log Payment", fontWeight = FontWeight.Bold) }
            if (revenueMsg.isNotEmpty()) {
                Text(revenueMsg, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp),
                    color = if (revenueMsg.startsWith("✅")) Color(0xFF059669) else Color(0xFFDC2626))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBox("Total", "₹$revTotal", Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
                StatBox("This Month", "₹$revMonthly", Color(0xFF059669), Color(0xFFF0FDF4), Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  TAB 4 — APP SETTINGS
// ══════════════════════════════════════════════════════════════
@Composable
private fun AdminSettingsTab() {
    val scope = rememberCoroutineScope()
    var bannerEnabled by remember { mutableStateOf(false) }
    var bannerText    by remember { mutableStateOf("") }
    var maintMode     by remember { mutableStateOf(false) }
    var maintMsg      by remember { mutableStateOf("") }
    var minVersion    by remember { mutableStateOf("") }
    var updateUrl     by remember { mutableStateOf("") }
    var settingsMsg   by remember { mutableStateOf("") }
    var loading       by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snap = db.collection("config").document("appSettings").get().await()
            if (snap.exists()) {
                bannerEnabled = snap.getBoolean("bannerEnabled") ?: false
                bannerText    = snap.getString("bannerText") ?: ""
                maintMode     = snap.getBoolean("maintenanceMode") ?: false
                maintMsg      = snap.getString("maintenanceMessage") ?: ""
                minVersion    = snap.getString("minVersion") ?: ""
                updateUrl     = snap.getString("updateUrl") ?: ""
            }
        } catch (_: Exception) {}
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6D28D9))
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Announcement banner
        AdminCard {
            SectionLabel("📢 ANNOUNCEMENT BANNER", Color(0xFF0891B2))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Show banner", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Switch(checked = bannerEnabled, onCheckedChange = { bannerEnabled = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF3730A3)))
            }
            OutlinedTextField(value = bannerText, onValueChange = { bannerText = it },
                placeholder = { Text("Banner message…", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(9.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            db.collection("config").document("appSettings")
                                .set(mapOf("bannerEnabled" to bannerEnabled, "bannerText" to bannerText), com.google.firebase.firestore.SetOptions.merge()).await()
                            settingsMsg = "✅ Banner saved!"
                        } catch (e: Exception) { settingsMsg = "❌ ${e.message}" }
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0891B2))
            ) { Text("Save Banner", fontWeight = FontWeight.Bold) }
        }

        // Maintenance mode
        AdminCard {
            SectionLabel("🚧 MAINTENANCE MODE", Color(0xFFDC2626))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Enable maintenance", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Switch(checked = maintMode, onCheckedChange = { maintMode = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFDC2626)))
            }
            OutlinedTextField(value = maintMsg, onValueChange = { maintMsg = it },
                placeholder = { Text("Message shown to users…", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(9.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            db.collection("config").document("appSettings")
                                .set(mapOf("maintenanceMode" to maintMode, "maintenanceMessage" to maintMsg), com.google.firebase.firestore.SetOptions.merge()).await()
                            settingsMsg = "✅ Maintenance settings saved!"
                        } catch (e: Exception) { settingsMsg = "❌ ${e.message}" }
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        }

        // Force update
        AdminCard {
            SectionLabel("📦 FORCE UPDATE", Color(0xFFD97706))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = minVersion, onValueChange = { minVersion = it },
                placeholder = { Text("Min version e.g. 1.0.3", fontSize = 12.sp) },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(9.dp))
            OutlinedTextField(value = updateUrl, onValueChange = { updateUrl = it },
                placeholder = { Text("APK URL", fontSize = 12.sp) },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(9.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            db.collection("config").document("appSettings")
                                .set(mapOf("minVersion" to minVersion, "updateUrl" to updateUrl), com.google.firebase.firestore.SetOptions.merge()).await()
                            settingsMsg = "✅ Force update saved!"
                        } catch (e: Exception) { settingsMsg = "❌ ${e.message}" }
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) { Text("Save Force Update", fontWeight = FontWeight.Bold) }
        }

        if (settingsMsg.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (settingsMsg.startsWith("✅")) Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
                    .padding(12.dp)
            ) {
                Text(settingsMsg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (settingsMsg.startsWith("✅")) Color(0xFF166534) else Color(0xFFDC2626))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  SHARED HELPER COMPOSABLES
// ══════════════════════════════════════════════════════════════
@Composable
private fun AdminCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) { Column(modifier = Modifier.padding(14.dp), content = content) }
}

@Composable
private fun SectionLabel(label: String, color: Color) {
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, letterSpacing = 0.6.sp)
}

@Composable
private fun StatBox(
    label: String, value: String, color: Color, bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(12.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
            Text(value, fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp, color = color, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun TwoColGrid(items: List<Triple<String, String, Color>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value, color) ->
                    StatBox(label, value, color, color.copy(alpha = 0.08f), Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// Time ago helper
fun timeAgo(ts: Long): String {
    if (ts == 0L) return "—"
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000L        -> "Just now"
        diff < 3_600_000L     -> "${diff / 60_000L}m ago"
        diff < 86_400_000L    -> "${diff / 3_600_000L}h ago"
        diff < 2_592_000_000L -> "${diff / 86_400_000L}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(ts))
    }
}
