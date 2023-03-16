package com.example.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.beust.klaxon.Klaxon
import kotlinx.android.synthetic.main.activity_history.*
import okhttp3.*
import java.io.IOException
import kotlin.math.log

class HistoryActivity : AppCompatActivity() {
    private val klaxon = Klaxon()
    private var logs = ArrayList<Logs>()
    private var isCalled = false
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        btnRetour.setOnClickListener {
            finish()
        }
        getHistory()

        Thread.sleep(1000)
        if (isCalled){
            for (log in logs){
                textViewAPI.text = textViewAPI.text.toString() + "Tag ID : " +log.UID + "\n Date : " + log.date + "\n Utilisateur : " + log.username + "\n Serrure : " + log.serrure + "\n\n"
                textViewAPI.text = textViewAPI.text.toString() + "--------------------------------------------\n\n\n"
            }
        }
        else{
            textViewAPI.text = "Erreur de connexion"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getHistory() {
        val url = "http://167.114.96.59:2223/api/getLogs"
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                isCalled = false
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()!!.string()
                logs = klaxon.parseArray<Logs>(body) as ArrayList<Logs>
                isCalled = true

            }
        })

    }
}