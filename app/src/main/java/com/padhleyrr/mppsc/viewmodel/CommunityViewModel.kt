package com.padhleyrr.mppsc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.padhleyrr.mppsc.data.models.ChatMessage
import com.padhleyrr.mppsc.data.models.Comment
import com.padhleyrr.mppsc.data.models.CommunityPost
import com.padhleyrr.mppsc.data.repository.CommunityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CommunityViewModel(app: Application) : AndroidViewModel(app) {

    // ── Feed ─────────────────────────────────────────────────────────
    private val _feedTag = MutableStateFlow("all")
    val feedTag: StateFlow<String> = _feedTag.asStateFlow()

    val posts: StateFlow<List<CommunityPost>> = _feedTag
        .flatMapLatest { CommunityRepository.postsFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFeedTag(tag: String) { _feedTag.value = tag }

    fun toggleLike(postId: String, currentlyLiked: Boolean) = viewModelScope.launch {
        try { CommunityRepository.toggleLike(postId, currentlyLiked) } catch (_: Exception) {}
    }

    fun toggleUpvote(postId: String, currentlyUpvoted: Boolean) = viewModelScope.launch {
        try { CommunityRepository.toggleUpvote(postId, currentlyUpvoted) } catch (_: Exception) {}
    }

    fun deletePost(postId: String) = viewModelScope.launch {
        try { CommunityRepository.deletePost(postId) } catch (_: Exception) {}
    }

    // ── Create post ───────────────────────────────────────────────────
    private val _posting = MutableStateFlow(false)
    val posting: StateFlow<Boolean> = _posting.asStateFlow()

    // BUG FIX: error state so UI can show failure feedback
    private val _postError = MutableStateFlow<String?>(null)
    val postError: StateFlow<String?> = _postError.asStateFlow()

    fun clearPostError() { _postError.value = null }

    fun createPost(
        title:    String,
        body:     String,
        tag:      String,
        imageUri: android.net.Uri? = null,
        onDone:   () -> Unit
    ) = viewModelScope.launch {
        if (_posting.value) return@launch
        _posting.value = true
        _postError.value = null
        try {
            // Upload image first if provided — pass context for ContentResolver
            val uploadedUrl = if (imageUri != null) {
                CommunityRepository.uploadPostImage(imageUri, getApplication())
            } else ""
            CommunityRepository.createPost(title, body, tag, uploadedUrl)
            onDone()
        } catch (e: Exception) {
            _postError.value = e.message ?: "Failed to post. Try again."
        } finally {
            _posting.value = false
        }
    }

    // ── Post detail & comments ────────────────────────────────────────
    private val _selectedPostId = MutableStateFlow<String?>(null)

    val comments: StateFlow<List<Comment>> = _selectedPostId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else CommunityRepository.commentsFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectPost(postId: String?) { _selectedPostId.value = postId }

    fun addComment(postId: String, body: String) = viewModelScope.launch {
        if (body.isBlank()) return@launch
        try { CommunityRepository.addComment(postId, body) } catch (_: Exception) {}
    }

    fun toggleCommentLike(
        postId:         String,
        commentId:      String,
        currentlyLiked: Boolean
    ) = viewModelScope.launch {
        try {
            CommunityRepository.toggleCommentLike(postId, commentId, currentlyLiked)
        } catch (_: Exception) {}
    }

    // ── Chat ──────────────────────────────────────────────────────────
    val chatMessages: StateFlow<List<ChatMessage>> = CommunityRepository.chatFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // BUG FIX: Prevent sending while a previous send is in flight
    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    fun sendMessage(body: String) = viewModelScope.launch {
        if (body.isBlank() || _sending.value) return@launch
        _sending.value = true
        try {
            CommunityRepository.sendChatMessage(body)
        } catch (_: Exception) {
        } finally {
            _sending.value = false
        }
    }

    // ── Current user ──────────────────────────────────────────────────
    val currentUid: String get() = CommunityRepository.currentUidPublic()

    // BUG FIX: clear selected post on VM cleared (avoids stale flows)
    override fun onCleared() {
        super.onCleared()
        _selectedPostId.value = null
    }
}
