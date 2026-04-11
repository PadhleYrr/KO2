package com.padhleyrr.mppsc.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.padhleyrr.mppsc.data.models.Comment
import com.padhleyrr.mppsc.data.models.CommunityPost
import com.padhleyrr.mppsc.ui.theme.DMSans
import com.padhleyrr.mppsc.ui.theme.Syne
import com.padhleyrr.mppsc.ui.theme.gkkColors
import com.padhleyrr.mppsc.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.*

// ═══════════════════════════════════════════════════════════════════
//  COMMUNITY SCREEN — Reddit-style with inner bottom nav
//  Tabs:  Feed (0)  |  Post Detail (1)  |  Chat (2)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun CommunityScreen(vm: CommunityViewModel = viewModel()) {
    var tab          by remember { mutableIntStateOf(0) }
    var selectedPost by remember { mutableStateOf<CommunityPost?>(null) }
    var showCompose  by remember { mutableStateOf(false) }
    val c = gkkColors

    // Handle system back button when in detail tab
    BackHandler(enabled = tab != 0) {
        tab = 0
        selectedPost = null
        vm.selectPost(null)
    }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        when (tab) {
            0 -> FeedTab(
                vm         = vm,
                onOpenPost = { post ->
                    selectedPost = post
                    vm.selectPost(post.id)
                    tab = 1
                },
                onCompose  = { showCompose = true }
            )
            1 -> PostDetailTab(
                vm     = vm,
                post   = selectedPost,
                onBack = {
                    tab = 0
                    selectedPost = null
                    vm.selectPost(null)
                }
            )
        }

        // Compose FAB (only on Feed tab)
        if (tab == 0) {
            FloatingActionButton(
                onClick        = { showCompose = true },
                containerColor = c.saff,
                contentColor   = Color.White,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(52.dp)
            ) {
                Icon(Icons.Default.Edit, "New Post", Modifier.size(20.dp))
            }
        }

        // Compose post sheet
        if (showCompose) {
            ComposePostSheet(vm = vm, onDismiss = { showCompose = false })
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  FEED TAB
// ═══════════════════════════════════════════════════════════════════

@Composable
fun FeedTab(
    vm:         CommunityViewModel,
    onOpenPost: (CommunityPost) -> Unit,
    onCompose:  () -> Unit
) {
    val c     = gkkColors
    val posts by vm.posts.collectAsStateWithLifecycle()
    val tag   by vm.feedTag.collectAsStateWithLifecycle()

    val filterTags = listOf(
        "all"        to "🌐 All",
        "doubt"      to "❓ Doubts",
        "discussion" to "💬 Discussion",
        "resource"   to "📚 Resources",
        "mp"         to "🏙 MP GK",
        "national"   to "🏛 National"
    )

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(c.navy, c.navy.copy(alpha = 0.85f)))
                    )
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        "GKK Community",
                        fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp, color = Color.White
                    )
                    Text(
                        "Ask doubts · Share resources · Discuss MPPSC",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }

        // Filter chips
        item {
            Row(
                modifier              = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTags.forEach { (key, label) ->
                    val selected = tag == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) c.saff else c.card)
                            .border(1.dp, if (selected) c.saff else c.border, RoundedCornerShape(20.dp))
                            .clickable { vm.setFeedTag(key) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            label,
                            fontSize   = 12.sp,
                            color      = if (selected) Color.White else c.text,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Empty state
        if (posts.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 80.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗣️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No posts yet — be the first!", fontSize = 14.sp, color = c.muted)
                    }
                }
            }
        }

        // Post cards
        items(posts, key = { it.id }) { post ->
            PostCard(
                post       = post,
                currentUid = vm.currentUid,
                onOpen     = { onOpenPost(post) },
                onLike     = { vm.toggleLike(post.id, post.likedBy.contains(vm.currentUid)) },
                onUpvote   = { vm.toggleUpvote(post.id, post.upvotedBy.contains(vm.currentUid)) },
                onDelete   = { vm.deletePost(post.id) }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  POST CARD
// ═══════════════════════════════════════════════════════════════════

@Composable
fun PostCard(
    post:       CommunityPost,
    currentUid: String,
    onOpen:     () -> Unit,
    onLike:     () -> Unit,
    onUpvote:   () -> Unit,
    onDelete:   () -> Unit
) {
    val c       = gkkColors
    val liked   = post.likedBy.contains(currentUid)
    val upvoted = post.upvotedBy.contains(currentUid)
    val isOwn   = post.authorUid == currentUid && currentUid.isNotEmpty()

    val tagColor = when (post.tag) {
        "doubt"      -> Color(0xFFDC2626)
        "discussion" -> Color(0xFF7C3AED)
        "resource"   -> Color(0xFF059669)
        "mp"         -> Color(0xFFEA580C)
        "national"   -> Color(0xFF1D4ED8)
        else         -> c.muted
    }

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .clickable(onClick = onOpen),
        shape           = RoundedCornerShape(12.dp),
        color           = c.card,
        tonalElevation  = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(14.dp)) {

            // Author + tag row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier         = Modifier.size(30.dp).clip(CircleShape).background(c.navy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            // BUG FIX: safe take(1) on potentially empty authorName
                            post.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize   = 13.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(post.authorName, fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold, color = c.text)
                        Text(relativeTime(post.createdAt), fontSize = 10.sp, color = c.muted)
                    }
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (post.tag.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tagColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                post.tag.replaceFirstChar { it.uppercase() },
                                fontSize = 10.sp, color = tagColor, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isOwn) {
                        // BUG FIX: Wrap delete icon click in its own Box to prevent
                        // the card click from firing simultaneously
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(onClick = onDelete),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Delete, null,
                                tint = c.muted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                post.title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = c.text,
                lineHeight = 20.sp,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )

            if (post.body.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    post.body,
                    fontSize   = 12.sp,
                    color      = c.muted,
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
            }

            if (post.imageUrl.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                AsyncImage(
                    model              = post.imageUrl,
                    contentDescription = null,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = c.border.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionButton(
                    icon    = Icons.Default.ThumbUp,
                    label   = "${post.upvotes}",
                    active  = upvoted,
                    color   = Color(0xFF7C3AED),
                    onClick = onUpvote
                )
                ActionButton(
                    icon    = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label   = "${post.likes}",
                    active  = liked,
                    color   = Color(0xFFDC2626),
                    onClick = onLike
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.clickable(onClick = onOpen)
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, null,
                        tint = c.muted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${post.commentCount}", fontSize = 12.sp, color = c.muted)
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon:    androidx.compose.ui.graphics.vector.ImageVector,
    label:   String,
    active:  Boolean,
    color:   Color,
    onClick: () -> Unit
) {
    val c = gkkColors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, null,
            tint     = if (active) color else c.muted,
            modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            label, fontSize = 12.sp,
            color      = if (active) color else c.muted,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  POST DETAIL TAB — full post + threaded comments
// ═══════════════════════════════════════════════════════════════════

@Composable
fun PostDetailTab(
    vm:     CommunityViewModel,
    post:   CommunityPost?,
    onBack: () -> Unit
) {
    // BUG FIX: Use LaunchedEffect instead of calling onBack() during composition
    if (post == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val c         = gkkColors
    val comments  by vm.comments.collectAsStateWithLifecycle()
    val liked     = post.likedBy.contains(vm.currentUid)
    val upvoted   = post.upvotedBy.contains(vm.currentUid)
    var input     by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // BUG FIX: scroll to bottom when comments load (not just new ones)
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.size - 1)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(c.navy)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack, null,
                tint     = Color.White,
                modifier = Modifier.size(22.dp).clickable(onClick = onBack)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                post.title,
                fontFamily = Syne, fontWeight = FontWeight.Bold,
                fontSize = 15.sp, color = Color.White,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            state          = listState,
            modifier       = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            // Full post body
            item {
                Surface(
                    modifier        = Modifier.fillMaxWidth().padding(12.dp),
                    shape           = RoundedCornerShape(12.dp),
                    color           = c.card,
                    shadowElevation = 1.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier         = Modifier.size(36.dp).clip(CircleShape).background(c.navy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    post.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(post.authorName, fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold, color = c.text)
                                Text(relativeTime(post.createdAt), fontSize = 11.sp, color = c.muted)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(post.title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = c.text, lineHeight = 22.sp)
                        if (post.body.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(post.body, fontSize = 13.sp, color = c.muted, lineHeight = 19.sp)
                        }
                        if (post.imageUrl.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            AsyncImage(
                                model              = post.imageUrl,
                                contentDescription = null,
                                modifier           = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = c.border.copy(alpha = 0.5f))
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            ActionButton(Icons.Default.ThumbUp, "${post.upvotes}", upvoted,
                                Color(0xFF7C3AED)) { vm.toggleUpvote(post.id, upvoted) }
                            ActionButton(
                                if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                "${post.likes}", liked, Color(0xFFDC2626)
                            ) { vm.toggleLike(post.id, liked) }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ChatBubbleOutline, null,
                                    tint = c.muted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${comments.size} comments", fontSize = 12.sp, color = c.muted)
                            }
                        }
                    }
                }
            }

            // Comments header
            item {
                Text(
                    "💬 Comments",
                    fontFamily = Syne, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = c.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            if (comments.isEmpty()) {
                item {
                    Text(
                        "No comments yet. Be the first!",
                        fontSize = 13.sp, color = c.muted,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            items(comments, key = { it.id }) { comment ->
                CommentCard(
                    comment    = comment,
                    currentUid = vm.currentUid,
                    onLike     = {
                        vm.toggleCommentLike(post.id, comment.id,
                            comment.likedBy.contains(vm.currentUid))
                    }
                )
            }
        }

        // Comment input bar
        Surface(
            color           = c.card,
            shadowElevation = 8.dp,
            modifier        = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = input,
                    onValueChange = { input = it },
                    placeholder   = { Text("Add a comment…", fontSize = 13.sp) },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(20.dp),
                    maxLines      = 3,
                    textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val trimmed = input.trim()
                        if (trimmed.isNotEmpty()) {
                            vm.addComment(post.id, trimmed)
                            input = ""
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = c.saff,
                        unfocusedBorderColor = c.border
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick  = {
                        val trimmed = input.trim()
                        if (trimmed.isNotEmpty()) {
                            vm.addComment(post.id, trimmed)
                            input = ""
                        }
                    },
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(c.saff)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CommentCard(comment: Comment, currentUid: String, onLike: () -> Unit) {
    val c     = gkkColors
    val liked = comment.likedBy.contains(currentUid)

    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier         = Modifier.size(28.dp).clip(CircleShape).background(c.border),
            contentAlignment = Alignment.Center
        ) {
            Text(
                comment.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontSize = 11.sp, color = c.text, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(comment.authorName, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold, color = c.text)
                Text(relativeTime(comment.createdAt), fontSize = 10.sp, color = c.muted)
            }
            Spacer(Modifier.height(3.dp))
            Text(comment.body, fontSize = 13.sp, color = c.text, lineHeight = 18.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.clickable(onClick = onLike)
            ) {
                Icon(
                    if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint     = if (liked) Color(0xFFDC2626) else c.muted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("${comment.likes}", fontSize = 11.sp,
                    color = if (liked) Color(0xFFDC2626) else c.muted)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  COMPOSE POST SHEET
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostSheet(vm: CommunityViewModel, onDismiss: () -> Unit) {
    val c       = gkkColors
    val posting by vm.posting.collectAsStateWithLifecycle()
    val error   by vm.postError.collectAsStateWithLifecycle()
    var title   by remember { mutableStateOf("") }
    var body    by remember { mutableStateOf("") }
    var tag     by remember { mutableStateOf("discussion") }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var caption  by remember { mutableStateOf("") }

    val tags = listOf("discussion", "doubt", "resource", "mp", "national")

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    // Auto-clear error after 4s
    LaunchedEffect(error) {
        if (error != null) {
            kotlinx.coroutines.delay(4_000)
            vm.clearPostError()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!posting) onDismiss()
        },
        containerColor = c.card,
        sheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("New Post", fontFamily = Syne, fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp, color = c.text)
            Spacer(Modifier.height(16.dp))

            // Tag selector
            Text("Category", fontSize = 12.sp, color = c.muted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier              = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { t ->
                    val sel = tag == t
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sel) c.saff else c.bg)
                            .border(1.dp, if (sel) c.saff else c.border, RoundedCornerShape(20.dp))
                            .clickable(enabled = !posting) { tag = t }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            t.replaceFirstChar { it.uppercase() },
                            fontSize   = 12.sp,
                            color      = if (sel) Color.White else c.text,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value         = title,
                onValueChange = { if (it.length <= 300) title = it },
                label         = { Text("Title * (${title.length}/300)") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                enabled       = !posting,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = c.saff,
                    unfocusedBorderColor = c.border
                )
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value         = body,
                onValueChange = { body = it },
                label         = { Text("Details (optional)") },
                modifier      = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                maxLines      = 6,
                enabled       = !posting,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = c.saff,
                    unfocusedBorderColor = c.border
                )
            )

            Spacer(Modifier.height(14.dp))

            // ── Image Upload Section ──────────────────────────────────
            Text("Image (optional)", fontSize = 12.sp, color = c.muted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            if (imageUri != null) {
                // Preview selected image
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model              = imageUri,
                        contentDescription = null,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    // Remove button
                    IconButton(
                        onClick  = { imageUri = null; caption = "" },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = caption,
                    onValueChange = { if (it.length <= 200) caption = it },
                    label         = { Text("Caption (${caption.length}/200)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    enabled       = !posting,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = c.saff,
                        unfocusedBorderColor = c.border
                    )
                )
            } else {
                // Pick image button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.5.dp, c.border, RoundedCornerShape(10.dp))
                        .clickable(enabled = !posting) { imagePickerLauncher.launch("image/*") }
                        .padding(14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Image, null, tint = c.muted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tap to add image", fontSize = 13.sp, color = c.muted)
                }
            }

            // Error message
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text("⚠️ $error", fontSize = 12.sp, color = Color(0xFFDC2626))
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick  = {
                    if (title.isNotBlank() && !posting) {
                        vm.createPost(
                            title    = title.trim(),
                            body     = if (caption.isNotBlank()) "${body.trim()}\n\n📷 $caption".trim() else body.trim(),
                            tag      = tag,
                            imageUri = imageUri,
                            onDone   = { onDismiss() }
                        )
                    }
                },
                enabled  = title.isNotBlank() && !posting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = c.saff)
            ) {
                if (posting) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Post", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ── Utility ───────────────────────────────────────────────────────────
private fun relativeTime(ms: Long): String {
    if (ms == 0L) return ""
    val diff = System.currentTimeMillis() - ms
    return when {
        diff < 0L                   -> "just now" // BUG FIX: handle clock skew (negative diff)
        diff < 60_000L              -> "just now"
        diff < 3_600_000L           -> "${diff / 60_000}m ago"
        diff < 86_400_000L          -> "${diff / 3_600_000}h ago"
        diff < 7 * 86_400_000L      -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ms))
    }
}
