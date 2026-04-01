package com.padhleyrr.mppsc.data.models

import com.google.gson.annotations.SerializedName

data class SyllabusPaper(
    @SerializedName("paper")    val paper:    String           = "",
    @SerializedName("icon")     val icon:     String           = "",
    @SerializedName("color")    val color:    String           = "#1A237E",
    @SerializedName("subjects") val subjects: List<SyllabusSubject> = emptyList()
)

data class SyllabusSubject(
    @SerializedName("name")   val name:   String       = "",
    @SerializedName("icon")   val icon:   String       = "",
    @SerializedName("topics") val topics: List<String> = emptyList()
)
