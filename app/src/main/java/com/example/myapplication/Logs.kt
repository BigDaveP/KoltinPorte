package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class Logs(
    @SerializedName("_id") val id: String,
    @SerializedName("31UID") val UID: String
)