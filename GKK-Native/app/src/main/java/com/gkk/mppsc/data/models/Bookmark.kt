package com.gkk.mppsc.data.models

data class Bookmark(
    val question:    String       = "",
    val options:     List<String> = emptyList(),
    val answer:      Int          = 0,
    val category:    String       = "",
    val explanation: String       = "",
    val savedAt:     Long         = System.currentTimeMillis()
)
