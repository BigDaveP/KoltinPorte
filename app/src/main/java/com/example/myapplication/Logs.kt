package com.example.myapplication

import com.beust.klaxon.Json


data class Logs(
    @Json(name = "_id")
    val id: String,
    @Json(name = "username")
    val username: String,
    @Json(name = "31UID")
    val UID: String,
    @Json(name = "dateModified")
    val date: String,
    @Json(name = "serrure")
    val serrure: String
)