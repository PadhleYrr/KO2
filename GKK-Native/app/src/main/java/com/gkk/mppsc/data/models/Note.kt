package com.gkk.mppsc.data.models

import com.google.gson.annotations.SerializedName

data class Note(
    @SerializedName("id")      val id:      String = "",
    @SerializedName("name")    val name:    String = "",
    @SerializedName("content") val content: String = ""   // HTML string rendered in a WebView
)
