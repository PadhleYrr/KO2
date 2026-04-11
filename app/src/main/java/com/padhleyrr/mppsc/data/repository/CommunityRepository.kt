package com.padhleyrr.mppsc.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.padhleyrr.mppsc.data.models.ChatMessage
import com.padhleyrr.mppsc.data.models.Comment
import com.padhleyrr.mppsc.data.models.CommunityPost
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object CommunityRepository {

    private val db      = FirebaseFirestore.getInstance()
    private val auth    = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val postsCol = db.collection("communityPosts")
    private val chatCol  = db.collection("globalChat")

    private fun currentUid()  = auth.currentUser?.uid ?: ""
    private fun currentName() = auth.currentUser?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: auth.currentUser?.email?.substringBefore("@")
        ?: "Aspirant"

    // ────────────────────────────────────────────────────────────────
    //  IMAGE UPLOAD — Firebase Storage
    // ────────────────────────────────────────────────────────────────
    // FIX: Use putStream via ContentResolver — putFile(uri) fails with
    // "object does not exist" on content:// URIs from the photo picker.
    suspend fun uploadPostImage(uri: Uri, context: Context): String {
        val uid = currentUid()
        require(uid.isNotEmpty()) { "Must be logged in to upload" }
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val ext      = when (mimeType) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
        val fileName = "post_images/${uid}_${System.currentTimeMillis()}.$ext"
        val ref      = storage.reference.child(fileName)
        val metadata = StorageMetadata.Builder().setContentType(mimeType).build()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            ref.putStream(stream, metadata).await()
        } ?: error("Could not open image stream — check permissions")
        return ref.downloadUrl.await().toString()
    }

    // ────────────────────────────────────────────────────────────────
    //  POSTS — realtime feed
    //  BUG FIX: tag filter query requires a Firestore composite index.
    //  We apply client-side filter as fallback to avoid index errors.
    // ────────────────────────────────────────────────────────────────

    fun postsFlow(tag: String = "all"): Flow<List<CommunityPost>> = callbackFlow {
        // Always query with createdAt ordering (only this index always exists).
        // Apply tag filter client-side to avoid requiring a composite index.
        val query = postsCol
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100) // Fetch more when filtering so client filter has enough data

        val reg = query.addSnapshotListener { snap, error ->
            if (error != null || snap == null) {
                // Don't crash — just emit empty list on error
                trySend(emptyList())
                return@addSnapshotListener
            }
            val posts = snap.documents.mapNotNull { doc ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    CommunityPost(
                        id           = doc.id,
                        authorUid    = doc.getString("authorUid")    ?: "",
                        authorName   = doc.getString("authorName")   ?: "Aspirant",
                        authorAvatar = doc.getString("authorAvatar") ?: "",
                        type         = doc.getString("type")         ?: "post",
                        title        = doc.getString("title")        ?: "",
                        body         = doc.getString("body")         ?: "",
                        imageUrl     = doc.getString("imageUrl")     ?: "",
                        tag          = doc.getString("tag")          ?: "",
                        likes        = (doc.getLong("likes")         ?: 0L).toInt(),
                        upvotes      = (doc.getLong("upvotes")       ?: 0L).toInt(),
                        commentCount = (doc.getLong("commentCount")  ?: 0L).toInt(),
                        likedBy      = (doc.get("likedBy")  as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        upvotedBy    = (doc.get("upvotedBy") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        createdAt    = doc.getLong("createdAt")      ?: 0L,
                        caRef        = doc.getString("caRef")        ?: ""
                    )
                } catch (e: Exception) {
                    null // Skip malformed documents
                }
            }
            // Client-side tag filter
            val filtered = if (tag == "all") posts else posts.filter { it.tag == tag }
            trySend(filtered.take(60))
        }
        awaitClose { reg.remove() }
    }

    suspend fun createPost(
        title: String,
        body: String,
        tag: String,
        imageUrl: String = ""
    ): String {
        val uid  = currentUid()
        // BUG FIX: Prevent unauthenticated post creation silently
        require(uid.isNotEmpty()) { "User must be logged in to post" }
        val name = currentName()
        val doc  = postsCol.document()
        doc.set(
            mapOf(
                "authorUid"    to uid,
                "authorName"   to name,
                "authorAvatar" to "",
                "type"         to "post",
                "title"        to title.trim(),
                "body"         to body.trim(),
                "imageUrl"     to imageUrl,
                "tag"          to tag,
                "likes"        to 0,
                "upvotes"      to 0,
                "commentCount" to 0,
                "likedBy"      to emptyList<String>(),
                "upvotedBy"    to emptyList<String>(),
                "createdAt"    to System.currentTimeMillis(),
                "caRef"        to ""
            )
        ).await()
        return doc.id
    }

    suspend fun toggleLike(postId: String, currentlyLiked: Boolean) {
        val uid = currentUid()
        if (uid.isEmpty()) return
        val ref = postsCol.document(postId)
        if (currentlyLiked) {
            ref.update(
                "likes",   FieldValue.increment(-1),
                "likedBy", FieldValue.arrayRemove(uid)
            ).await()
        } else {
            ref.update(
                "likes",   FieldValue.increment(1),
                "likedBy", FieldValue.arrayUnion(uid)
            ).await()
        }
    }

    suspend fun toggleUpvote(postId: String, currentlyUpvoted: Boolean) {
        val uid = currentUid()
        if (uid.isEmpty()) return
        val ref = postsCol.document(postId)
        if (currentlyUpvoted) {
            ref.update(
                "upvotes",   FieldValue.increment(-1),
                "upvotedBy", FieldValue.arrayRemove(uid)
            ).await()
        } else {
            ref.update(
                "upvotes",   FieldValue.increment(1),
                "upvotedBy", FieldValue.arrayUnion(uid)
            ).await()
        }
    }

    suspend fun deletePost(postId: String) {
        val uid = currentUid()
        if (uid.isEmpty()) return
        val doc = postsCol.document(postId).get().await()
        // BUG FIX: Check document existence before comparing authorUid
        if (doc.exists() && doc.getString("authorUid") == uid) {
            postsCol.document(postId).delete().await()
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  COMMENTS — realtime for a single post
    // ────────────────────────────────────────────────────────────────

    fun commentsFlow(postId: String): Flow<List<Comment>> = callbackFlow {
        // BUG FIX: Guard against blank postId
        if (postId.isBlank()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = postsCol.document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val comments = snap.documents.mapNotNull { doc ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        Comment(
                            id         = doc.id,
                            postId     = postId,
                            authorUid  = doc.getString("authorUid")  ?: "",
                            authorName = doc.getString("authorName") ?: "Aspirant",
                            body       = doc.getString("body")       ?: "",
                            likes      = (doc.getLong("likes")       ?: 0L).toInt(),
                            likedBy    = (doc.get("likedBy") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            createdAt  = doc.getLong("createdAt")    ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(comments)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addComment(postId: String, body: String) {
        val uid  = currentUid()
        if (uid.isEmpty() || body.isBlank()) return
        val name = currentName()
        val ref  = postsCol.document(postId).collection("comments").document()
        ref.set(
            mapOf(
                "authorUid"  to uid,
                "authorName" to name,
                "body"       to body.trim(),
                "likes"      to 0,
                "likedBy"    to emptyList<String>(),
                "createdAt"  to System.currentTimeMillis()
            )
        ).await()
        // BUG FIX: Use a transaction or batch to keep commentCount accurate;
        // at minimum, increment atomically as before
        postsCol.document(postId)
            .update("commentCount", FieldValue.increment(1))
            .await()
    }

    suspend fun toggleCommentLike(postId: String, commentId: String, currentlyLiked: Boolean) {
        val uid = currentUid()
        if (uid.isEmpty()) return
        val ref = postsCol.document(postId).collection("comments").document(commentId)
        if (currentlyLiked) {
            ref.update("likes", FieldValue.increment(-1), "likedBy", FieldValue.arrayRemove(uid)).await()
        } else {
            ref.update("likes", FieldValue.increment(1), "likedBy", FieldValue.arrayUnion(uid)).await()
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  GLOBAL CHAT — realtime
    // ────────────────────────────────────────────────────────────────

    fun chatFlow(): Flow<List<ChatMessage>> = callbackFlow {
        val reg = chatCol
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limitToLast(80)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val msgs = snap.documents.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id         = doc.id,
                            authorUid  = doc.getString("authorUid")  ?: "",
                            authorName = doc.getString("authorName") ?: "Aspirant",
                            body       = doc.getString("body")       ?: "",
                            createdAt  = doc.getLong("createdAt")    ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(msgs)
            }
        awaitClose { reg.remove() }
    }

    suspend fun sendChatMessage(body: String) {
        val uid  = currentUid()
        if (uid.isEmpty() || body.isBlank()) return
        val name = currentName()
        chatCol.add(
            mapOf(
                "authorUid"  to uid,
                "authorName" to name,
                "body"       to body.trim(),
                "createdAt"  to System.currentTimeMillis()
            )
        ).await()
    }

    fun currentUidPublic() = currentUid()
}
