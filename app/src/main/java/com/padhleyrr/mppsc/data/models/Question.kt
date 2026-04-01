package com.padhleyrr.mppsc.data.models

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("c")   val category:    String  = "",
    @SerializedName("q")   val question:    String  = "",
    @SerializedName("o")   val options:     List<String> = emptyList(),
    @SerializedName("a")   val answer:      Int     = 0,
    @SerializedName("n")   val explanation: String  = "",
    @SerializedName("upd") val updated:     String? = null   // optional correction note
)
