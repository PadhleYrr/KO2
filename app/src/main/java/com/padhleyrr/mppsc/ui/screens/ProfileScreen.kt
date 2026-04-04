package com.padhleyrr.mppsc.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.padhleyrr.mppsc.data.repository.SubscriptionRepository
import com.padhleyrr.mppsc.data.repository.SubscriptionState
import com.padhleyrr.mppsc.data.repository.UserRecord
import com.padhleyrr.mppsc.ui.theme.DMSans
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors
import com.padhleyrr.mppsc.viewmodel.MainViewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════
//  PROFILE SCREEN — identical UX to GKK showAccountModal
//  • Shows user avatar, name, email, status badge
//  • Features list (locked/unlocked)
//  • Upgrade card for trial/expired users
//  • Admin button for admin accounts
// ═══════════════════════════════════════════════════════

@Composable
fun ProfileScreen(
    vm: MainViewModel,
    onNavigateToDonate: () -> Unit,
    onSignOut: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenSubscription: () -> Unit
) {
    val c    = gkkColors
    val sub  by SubscriptionRepository.state.collectAsStateWithLifecycle()
    val user by SubscriptionRepository.userRecord.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header gradient ──────────────────────────────────────
        val headerBrush = when {
            sub.isAdmin   -> Brush.verticalGradient(listOf(Color(0xFF4527A0), Color(0xFF6A1B9A)))
            sub.isPremium -> Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))
            sub.isTrialActive -> Brush.verticalGradient(listOf(Color(0xFF4527A0), Color(0xFF7B1FA2)))
            else          -> Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFFC62828)))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = (user.name.firstOrNull() ?: user.email.firstOrNull() ?: 'U')
                        .uppercaseChar()
                    Text(
                        "$initial",
                        fontFamily = Syne,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 28.sp,
                        color      = Color.White
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    user.name.ifEmpty { user.email.substringBefore('@') },
                    fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp, color = Color.White
                )
                Text(
                    user.email,
                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 3.dp)
                )

                Spacer(Modifier.height(14.dp))

                // Status badge
                val (badgeText, badgeBg) = statusBadge(sub)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    Text(badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Countdown timer for trial
                if (sub.isTrialActive && !sub.isAdmin && !sub.isPremium) {
                    TrialCountdown(trialMsLeft = sub.trialMsLeft)
                }
            }
        }

        // ── Body ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Features list
            FeaturesSection(sub = sub)

            // Upgrade card
            if (!sub.isAdmin && !sub.isPremium) {
                UpgradeCard(onOpenSubscription = onOpenSubscription)
            }

            // Admin button
            if (sub.isAdmin) {
                AdminButton(onClick = onOpenAdmin)
            }

            // Subscription details for premium users
            if (sub.isPremium) {
                PremiumInfoCard(sub = sub)
            }

            // Coupon redeem
            CouponSection(vm = vm)

            // Donate button
            OutlinedButton(
                onClick  = onNavigateToDonate,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC2410C)),
                border   = BorderStroke(1.dp, Color(0xFFFED7AA))
            ) {
                Text("❤️  Support Us — Donate", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            // Sign out
            OutlinedButton(
                onClick  = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                border   = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Text("Sign Out", fontSize = 14.sp)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ── Features list ────────────────────────────────────────────────────
@Composable
private fun FeaturesSection(sub: SubscriptionState) {
    val c = gkkColors
    val locked = !sub.isAdmin && !sub.isPremium && !sub.isTrialActive

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = c.card),
        border   = BorderStroke(1.dp, c.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "FEATURES",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = c.muted, letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SubscriptionRepository.FEATURES.forEach { (icon, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(icon, fontSize = 16.sp)
                    Text(
                        label, fontSize = 13.sp, modifier = Modifier.weight(1f),
                        color = if (locked) c.muted else c.text
                    )
                    Text(if (locked) "🔒" else "✅", fontSize = 14.sp)
                }
                HorizontalDivider(color = c.border, thickness = 0.5.dp)
            }
        }
    }
}

// ── Upgrade card ──────────────────────────────────────────────────────
@Composable
private fun UpgradeCard(onOpenSubscription: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF283593))),
                    RoundedCornerShape(16.dp)
                )
                .padding(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "₹100",
                        fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp, color = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "/ 6 months",
                        fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    "less than ₹17/month — less than 1 chai ☕",
                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                    textAlign = TextAlign.Center
                )
                // Pill grid
                val pills = listOf("No subscription", "One-time payment", "Instant activation", "All 8 features")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pills.take(2).forEach { pill ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pill, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = Color.White, textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pills.takeLast(2).forEach { pill ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pill, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = Color.White, textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick  = onOpenSubscription,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        "🔓  Get Premium — ₹100",
                        fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp, color = Color(0xFF1A237E)
                    )
                }
            }
        }
    }
}

// ── Admin button ─────────────────────────────────────────────────────
@Composable
private fun AdminButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFEEF2FF),
            contentColor   = Color(0xFF1A237E)
        ),
        border   = BorderStroke(1.dp, Color(0xFFC7D7FD))
    ) {
        Text("🔧  Admin Panel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Premium info card ─────────────────────────────────────────────────
@Composable
private fun PremiumInfoCard(sub: SubscriptionState) {
    val c = gkkColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
        border   = BorderStroke(1.dp, Color(0xFF6EE7B7))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("💎", fontSize = 28.sp)
            Column {
                Text("Premium Active", fontFamily = Syne, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, color = Color(0xFF065F46))
                Text("${sub.premiumDaysLeft} days remaining",
                    fontSize = 12.sp, color = Color(0xFF059669),
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// ── Coupon section ─────────────────────────────────────────────────
@Composable
private fun CouponSection(vm: MainViewModel) {
    val c = gkkColors
    var showDialog by remember { mutableStateOf(false) }
    var couponCode by remember { mutableStateOf("") }
    var message    by remember { mutableStateOf<String?>(null) }
    var loading    by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick  = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7C3AED)),
        border   = BorderStroke(1.dp, Color(0xFFC4B5FD))
    ) {
        Text("🎟  Redeem Coupon Code", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; message = null; couponCode = "" },
            title = { Text("Redeem Coupon", fontFamily = Syne, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = couponCode, onValueChange = { couponCode = it.uppercase() },
                        label = { Text("Coupon Code") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    message?.let { msg ->
                        Spacer(Modifier.height(8.dp))
                        val isOk = msg.startsWith("✅")
                        Text(msg, fontSize = 12.sp,
                            color = if (isOk) Color(0xFF059669) else Color(0xFFDC2626))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (couponCode.isBlank()) return@TextButton
                    loading = true
                    message = null
                    vm.redeemCoupon(couponCode) { result ->
                        loading = false
                        result.onSuccess { days ->
                            message = "✅ Coupon applied! $days days premium unlocked."
                        }.onFailure { e ->
                            message = "❌ ${e.message}"
                        }
                    }
                }, enabled = !loading) {
                    Text(if (loading) "Applying…" else "Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; message = null; couponCode = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Trial countdown ───────────────────────────────────────────────────
@Composable
private fun TrialCountdown(trialMsLeft: Long) {
    var ms by remember { mutableStateOf(trialMsLeft) }
    LaunchedEffect(trialMsLeft) {
        while (ms > 0) {
            delay(1000L)
            ms -= 1000L
        }
    }

    val d = (ms / 86_400_000L).toInt()
    val h = ((ms % 86_400_000L) / 3_600_000L).toInt()
    val m = ((ms % 3_600_000L) / 60_000L).toInt()
    val s = ((ms % 60_000L) / 1_000L).toInt()

    Spacer(Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${d}d ${h.toString().padStart(2,'0')}h ${m.toString().padStart(2,'0')}m ${s.toString().padStart(2,'0')}s",
                fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp, color = Color.White
            )
            Text(
                "time remaining on trial",
                fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ── Status badge helper ───────────────────────────────────────────────
private fun statusBadge(sub: SubscriptionState): Pair<String, Color> = when {
    sub.isAdmin       -> "🛡️ ADMIN ACCOUNT — FULL ACCESS" to Color(0xFF5E35B1)
    sub.isPremium     -> "✅ PREMIUM ACTIVE — ${sub.premiumDaysLeft} DAYS LEFT" to Color(0xFF15803D)
    sub.isTrialActive -> "⏳ TRIAL ACTIVE" to Color(0xFFD97706)
    else              -> "🔴 TRIAL EXPIRED — UPGRADE TO CONTINUE" to Color(0xFFDC2626)
}
