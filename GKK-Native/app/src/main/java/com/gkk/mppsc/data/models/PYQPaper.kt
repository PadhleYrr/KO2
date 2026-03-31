package com.gkk.mppsc.data.models

import com.google.gson.annotations.SerializedName

data class PYQPaper(
    @SerializedName("year")      val year:      String           = "",
    @SerializedName("paper")     val paper:     String           = "",
    @SerializedName("paperId")   val paperId:   String           = "",
    @SerializedName("title")     val title:     String           = "",
    @SerializedName("totalQ")    val totalQ:    Int              = 0,
    @SerializedName("duration")  val duration:  String           = "",
    @SerializedName("pdfUrl")    val pdfUrl:    String           = "",
    @SerializedName("questions") val questions: List<PYQQuestion> = emptyList()
)

data class PYQQuestion(
    @SerializedName("q")    val question:    String       = "",
    @SerializedName("opts") val options:     List<String> = emptyList(),
    @SerializedName("ans")  val answer:      Int          = 0,
    @SerializedName("exp")  val explanation: String       = ""
)
