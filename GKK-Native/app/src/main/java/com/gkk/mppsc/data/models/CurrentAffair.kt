package com.gkk.mppsc.data.models

import com.google.gson.annotations.SerializedName

data class CurrentAffair(
    @SerializedName("title") val title: String = "",
    @SerializedName("desc")  val desc:  String = "",
    @SerializedName("tag")   val tag:   String = "",   // "national" | "mp" | "international"
    @SerializedName("date")  val date:  String = ""
)
