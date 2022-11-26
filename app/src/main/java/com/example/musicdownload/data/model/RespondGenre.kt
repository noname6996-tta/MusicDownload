package com.example.musicdownload.data.model

import com.google.gson.annotations.SerializedName

data class RespondGenre(
    @SerializedName("status")
    var status: String,
    @SerializedName("currentOffset")
    var currentOffset: String,
    @SerializedName("data")
    var data: List<Genre>
)