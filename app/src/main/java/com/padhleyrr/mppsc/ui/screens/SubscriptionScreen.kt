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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.padhleyrr.mppsc.data.repository.SubscriptionRepository
import com.padhleyrr.mppsc.ui.theme.DMSans
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors
import com.padhleyrr.mppsc.viewmodel.MainViewModel

// ═══════════════════════════════════════════════════════
//  SUBSCRIPTION SCREEN
//  100% same layout & UX as GKK "showPaywall" + "upgradeCard"
//  Price: ₹100 / 6 months  |  UPI payment flow
// ═══════════════════════════════════════════════════════

@Composable
fun SubscriptionScreen(vm: MainViewModel, onClose: () -> Unit) {
    val c     = gkkColors
    val sub   by SubscriptionRepository.state.collectAsStateWithLifecycle()
    val user  by SubscriptionRepository.userRecord.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var paying by remember { mutableStateOf(false) }

    // If already premium/admin — show premium screen
    if (sub.isPremium || sub.isAdmin) {
        PremiumActiveScreen(sub = sub, onClose = onClose)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Lock Hero ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF283593)))
                )
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔒", fontSize = 44.sp, modifier = Modifier.padding(bottom = 12.dp))
                Text(
                    "Premium Required",
                    fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp, color = Color.White
                )
                Text(
                    if (user.email.isNotBlank()) "Your 7-day free trial has ended."
                    else "Sign in to start your free trial.",
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Price card ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                border   = BorderStroke(1.dp, Color(0xFF93B4F4))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "₹100",
                            fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp, color = Color(0xFF1A237E)
                        )
                        Text(
                            "  / 6 months",
                            fontSize = 14.sp, color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        "That's just ₹17/month!",
                        fontSize = 12.sp, color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Features checklist
                    val checkItems = listOf(
                        "All 421+ MCQ questions",
                        "All PYQ papers 2021–2024",
                        "Unlimited timed mocks",
                        "Smart revision mode",
                        "Full notes & flashcards"
                    )
                    checkItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✅", fontSize = 14.sp)
                            Text(item, fontSize = 13.sp, color = Color(0xFF374151))
                        }
                    }
                }
            }

            // ── Benefit pills ────────────────────────────────────
            val pills = listOf("No subscription", "One-time payment", "Instant activation", "All 8 features")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pills.take(2).forEach { pill ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF0FF))
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(pill, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A237E), textAlign = TextAlign.Center)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pills.takeLast(2).forEach { pill ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF0FF))
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(pill, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A237E), textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── UPI Payment button ───────────────────────────────
            // Note: Razorpay SDK needs to be integrated in build.gradle
            // For now we use UPI deep link / QR code flow like GKK donate page
            Button(
                onClick = {
                    // TODO: Integrate Razorpay SDK or UPI intent
                    // val uri = Uri.parse("upi://pay?pa=padhleyrr@upi&pn=GKK+MPPSC&am=100&cu=INR")
                    // context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    paying = true
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
            ) {
                Text(
                    "🔓  Unlock for ₹100",
                    fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp, color = Color.White
                )
            }

            // UPI QR / Copy ID section
            UpiPaymentSection()

            TextButton(
                onClick  = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Maybe later", color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }
        }
    }

    // After payment confirmation dialog
    if (paying) {
        PaymentConfirmDialog(
            vm      = vm,
            onDismiss = { paying = false },
            onSuccess = onClose
        )
    }
}

// ── UPI section (same as GKK donate page) ────────────────────────────
@Composable
private fun UpiPaymentSection() {
    val c = gkkColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = c.card),
        border   = BorderStroke(1.dp, c.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pay via UPI", fontFamily = Syne, fontWeight = FontWeight.Bold,
                fontSize = 14.sp, color = c.text, modifier = Modifier.padding(bottom = 12.dp))

            // UPI ID box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F4FF))
                    .border(BorderStroke(2.dp, Color(0xFF93B4F4)), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "padhleyrr@upi",
                        fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp, color = Color(0xFF1A237E)
                    )
                    Text("MP GK Portal — PadhleYrr",
                        fontSize = 11.sp, color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 3.dp))
                    Text("📋 Tap to copy", fontSize = 12.sp, color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 5.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Open any UPI app → Send ₹100 → Then confirm below",
                fontSize = 12.sp, color = c.muted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Works with GPay, PhonePe, Paytm, BHIM",
                fontSize = 11.sp, color = c.muted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Payment confirmation dialog ───────────────────────────────────────
@Composable
private fun PaymentConfirmDialog(vm: MainViewModel, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Payment", fontFamily = Syne, fontWeight = FontWeight.Bold) },
        text  = {
            Column {
                Text("After sending ₹100 to padhleyrr@upi, click \"I've Paid\" below.",
                    fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("Your premium will be activated within minutes by the admin.",
                    fontSize = 12.sp, color = Color(0xFF64748B))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Record payment intent — admin will verify and activate
                    vm.logPaymentIntent()
                    onSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
            ) {
                Text("I've Paid ✓", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Premium already active screen ────────────────────────────────────
@Composable
private fun PremiumActiveScreen(
    sub: com.padhleyrr.mppsc.data.repository.SubscriptionState,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(gkkColors.bg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (sub.isAdmin) "🛡️" else "💎", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            if (sub.isAdmin) "Admin Account" else "Premium Active",
            fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp, color = gkkColors.navy
        )
        Text(
            if (sub.isAdmin) "Full access to all features."
            else "${sub.premiumDaysLeft} days remaining",
            fontSize = 14.sp, color = gkkColors.muted,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )
        Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = gkkColors.navy)) {
            Text("Continue →", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
