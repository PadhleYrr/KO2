package com.padhleyrr.mppsc.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padhleyrr.mppsc.ui.theme.GKKColors
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors

// ── Card ─────────────────────────────────────────────────────────────
@Composable
fun GKKCard(
    modifier: Modifier = Modifier,
    padding:  Dp = 20.dp,
    content:  @Composable ColumnScope.() -> Unit
) {
    val c = gkkColors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.card)
            .border(1.dp, c.border, RoundedCornerShape(14.dp))
            .padding(padding),
        content = content
    )
}

// ── Section header ───────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title:       String,
    actionLabel: String? = null,
    onAction:    (() -> Unit)? = null
) {
    val c = gkkColors
    Row(
        modifier              = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(title, fontFamily = Syne, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = c.text)
        if (actionLabel != null && onAction != null) {
            Text(
                text     = actionLabel,
                fontSize = 12.sp,
                color    = c.saff,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

// ── Stat card (Dashboard grid) ───────────────────────────────────────
@Composable
fun StatCard(
    label:   String,
    value:   String,
    sub:     String,
    accent:  Color,
    modifier: Modifier = Modifier
) {
    val c = gkkColors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.card)
            .border(1.dp, c.border, RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accent)
                .align(Alignment.TopStart)
        )
        Column(modifier = Modifier.padding(18.dp).padding(top = 6.dp)) {
            Text(label, fontSize = 12.sp, color = c.muted, fontWeight = FontWeight.Medium)
            Text(
                value,
                fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp, color = c.text,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(sub, fontSize = 11.sp, color = c.muted)
        }
    }
}

// ── Chapter progress card ────────────────────────────────────────────
@Composable
fun ChapterCard(
    name:       String,
    count:      Int,
    done:       Int,
    accuracy:   Int,
    accentColor: Color,
    onClick:    () -> Unit,
    modifier:   Modifier = Modifier
) {
    val c       = gkkColors
    val progress = if (count > 0) done.toFloat() / count else 0f
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(c.card)
            .border(1.dp, c.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text     = name.replace(Regex("^Ch\\.\\d+\\s*"), ""),
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color    = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (accuracy > 0) {
                Text("$accuracy%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("$count qs | $done done", fontSize = 10.sp, color = c.muted)
        Spacer(Modifier.height(10.dp))
        // Progress bar
        Box(
            modifier = Modifier.fillMaxWidth().height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(c.border)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp)).background(accentColor)
            )
        }
    }
}

// ── Heatmap cell ─────────────────────────────────────────────────────
@Composable
fun HeatmapCell(level: Int, navy: Color, modifier: Modifier = Modifier) {
    val color = when (level) {
        0    -> Color(0xFFE2E8F0)
        1    -> Color(0xFFBFD3F7)
        2    -> Color(0xFF93B4F4)
        3    -> Color(0xFF5C8EEF)
        else -> navy
    }
    Box(modifier = modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
}

// ── Weak area row ────────────────────────────────────────────────────
@Composable
fun WeakAreaRow(name: String, accuracy: Float) {
    val c     = gkkColors
    val color = when {
        accuracy < 0.4f -> c.danger
        accuracy < 0.7f -> c.warn
        else            -> c.success
    }
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = name.replace(Regex("^Ch\\.\\d+\\s*"), "").take(16),
            fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
            color    = c.text, modifier = Modifier.width(110.dp)
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.weight(1f).height(6.dp)
                .clip(RoundedCornerShape(3.dp)).background(c.border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(maxOf(accuracy, 0.02f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text     = "${(accuracy * 100).toInt()}%",
            fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color    = color, modifier = Modifier.width(38.dp)
        )
    }
}

// ── Primary button ───────────────────────────────────────────────────
@Composable
fun GKKButton(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    enabled:  Boolean  = true,
    color:    Color?   = null
) {
    val c = gkkColors
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.fillMaxWidth().height(48.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = color ?: c.navy),
        shape    = RoundedCornerShape(10.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ── Ghost button ─────────────────────────────────────────────────────
@Composable
fun GKKOutlineButton(
    text:    String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = gkkColors
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(44.dp),
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = c.navy),
        border   = BorderStroke(2.dp, c.navy)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Badge chip ───────────────────────────────────────────────────────
@Composable
fun GKKBadge(text: String, bg: Color, textColor: Color = Color.White) {
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── Filter chip ──────────────────────────────────────────────────────
@Composable
fun FilterChip(
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit
) {
    val c = gkkColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) c.navy else c.card)
            .border(1.5.dp, if (selected) c.navy else c.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (selected) Color.White else c.muted
        )
    }
}

// ── Mode card (test mode selector) ───────────────────────────────────
@Composable
fun ModeCard(
    emoji:    String,
    title:    String,
    desc:     String,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = gkkColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFEEF0FF) else c.card)
            .border(
                2.dp,
                if (selected) c.navy else c.border,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.text)
        Text(desc,  fontSize = 11.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp))
    }
}
