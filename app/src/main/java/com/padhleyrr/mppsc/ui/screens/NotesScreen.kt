package com.padhleyrr.mppsc.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.padhleyrr.mppsc.ui.components.GKKCard
import com.padhleyrr.mppsc.ui.theme.gkkColors
import com.padhleyrr.mppsc.viewmodel.MainViewModel

@Composable
fun NotesScreen(vm: MainViewModel) {
    val c     = gkkColors
    val notes by vm.notes.collectAsStateWithLifecycle()
    var selectedIndex by remember { mutableStateOf(0) }

    if (notes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(c.bg), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(color = c.navy)
        }
        return
    }

    // Build the CSS that matches your existing style.css for notes
    val notesCss = """
        body { font-family: 'sans-serif'; font-size: 13px; color: #1E293B; background: ${colorToHex(c.card)}; padding: 0; margin: 0; }
        .nsh { background: ${colorToHex(c.navy)}; color: #fff; padding: 9px 16px; border-radius: 9px; font-size: 13px; font-weight: 700; margin-bottom: 10px; }
        .nssh { background: ${colorToHex(c.saff)}; color: #fff; padding: 5px 13px; border-radius: 6px; font-size: 11px; font-weight: 700; margin: 10px 0 7px; display: inline-block; }
        .nkv { width: 100%; border-collapse: collapse; font-size: 12.5px; margin-bottom: 8px; }
        .nkv td { padding: 6px 10px; border: 1px solid #E2E8F0; vertical-align: top; line-height: 1.5; }
        .nkv tr:nth-child(odd) td { background: #F8F9FF; }
        .nkv td:first-child { font-weight: 600; color: ${colorToHex(c.navy)}; width: 32%; background: #EEF0FF !important; }
        .nkv th { background: ${colorToHex(c.navy)}; color: #fff; padding: 6px 10px; text-align: left; }
        .ntip { padding: 9px 13px; border-radius: 7px; font-size: 12px; margin: 8px 0; border-left: 4px solid; }
        .ntip-w { background: #FFFBEB; color: #92400E; border-color: #F9A825; }
        .ntip-r { background: #FEF2F2; color: #991B1B; border-color: #DC2626; }
        .nbl { background: #FFF7ED; border-radius: 8px; padding: 10px 14px; margin-bottom: 9px; }
        .nbi { font-size: 12.5px; padding: 3px 0 3px 16px; position: relative; border-bottom: 1px solid #FED7AA; line-height: 1.5; }
        .nbi::before { content: "▸"; position: absolute; left: 2px; color: ${colorToHex(c.saff)}; }
        b { color: ${colorToHex(c.navy)}; }
    """.trimIndent()

    Column(
        modifier = Modifier.fillMaxSize().background(c.bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Left nav ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                notes.forEachIndexed { idx, note ->
                    val active = idx == selectedIndex
                    Text(
                        text = note.name,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) Color.White else c.muted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 3.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (active) c.navy else Color.Transparent)
                            .border(
                                1.dp,
                                if (active) Color.Transparent else c.border,
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { selectedIndex = idx }
                            .padding(8.dp)
                    )
                }
            }

            // ── Content area ─────────────────────────────────────────────
            val currentNote = notes.getOrNull(selectedIndex)
            if (currentNote != null) {
                GKKCard(modifier = Modifier.weight(1f).fillMaxHeight(), padding = 0.dp) {
                    AndroidView(
                        factory  = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = false
                            }
                        },
                        update   = { webView ->
                            val html = """
                                <!DOCTYPE html><html><head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
                                <style>$notesCss</style>
                                </head><body>${currentNote.content}</body></html>
                            """.trimIndent()
                            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

private fun colorToHex(color: Color): String {
    val r = (color.red   * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue  * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}
