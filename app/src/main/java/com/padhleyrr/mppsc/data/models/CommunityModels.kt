package com.padhleyrr.mppsc.data.models

// ── Post (community feed + current affairs discussion) ───────────────
data class CommunityPost(
    val id:           String       = "",
    val authorUid:    String       = "",
    val authorName:   String       = "",
    val authorAvatar: String       = "",   // initials fallback
    val type:         String       = "post", // "post" | "ca"
    val title:        String       = "",
    val body:         String       = "",
    val imageUrl:     String       = "",   // optional attached image URL
    val tag:          String       = "",   // "doubt" | "discussion" | "resource" | "mp" | "national"
    val likes:        Int          = 0,
    val upvotes:      Int          = 0,
    val commentCount: Int          = 0,
    val likedBy:      List<String> = emptyList(),
    val upvotedBy:    List<String> = emptyList(),
    val createdAt:    Long         = 0L,
    val caRef:        String       = ""    // date key e.g. "2026-04-04"
)

// ── Comment ──────────────────────────────────────────────────────────
data class Comment(
    val id:         String       = "",
    val postId:     String       = "",
    val authorUid:  String       = "",
    val authorName: String       = "",
    val body:       String       = "",
    val likes:      Int          = 0,
    val likedBy:    List<String> = emptyList(),
    val createdAt:  Long         = 0L
)

// ── Chat message ──────────────────────────────────────────────────────
data class ChatMessage(
    val id:         String = "",
    val authorUid:  String = "",
    val authorName: String = "",
    val body:       String = "",
    val createdAt:  Long   = 0L
)
