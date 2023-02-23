package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.mqtt.MqttClientHelper
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage


import java.io.IOException
import java.lang.reflect.Type
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    var valueJson = "";
    var isScanned = false
    private val client = OkHttpClient()
    private val mqttClient by lazy {
        MqttClientHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        textViewMsgPayload.movementMethod = ScrollingMovementMethod()

        setMqttCallBack()

        // initialize 'num msgs received' field in the view
        textViewNumMsgs.text = "0"
        run()
        Log.w("Value", valueJson)
        // pub button
        btnPub.setOnClickListener { view ->
            var snackbarMsg : String
            val topic = "porte_sub"
            snackbarMsg = "Cannot publish to empty topic!"
            if (topic.isNotEmpty()) {
                snackbarMsg = try {
                    mqttClient.publish(topic, "true")
                    "Published to topic '$topic'"
                } catch (ex: MqttException) {
                    "Error publishing to topic: $topic"
                }
            }
            Snackbar.make(view, snackbarMsg, 300)
                .setAction("Action", null).show()

        }

        // sub button
        btnSub.setOnClickListener { view ->
            var snackbarMsg : String
            val topic = "porte_sub"
            snackbarMsg = "Cannot subscribe to empty topic!"
            if (topic.isNotEmpty()) {
                snackbarMsg = try {
                    mqttClient.subscribe(topic)
                    "Subscribed to topic '$topic'"
                } catch (ex: MqttException) {
                    "Error subscribing to topic: $topic"
                }
                CompareParseValueToSub(textViewMsgPayload.text.toString())
                if (isScanned) {

                    isScanned = false
                }
            }
            Snackbar.make(view, snackbarMsg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }



        Timer("CheckMqttConnection", false).schedule(3000) {
            if (!mqttClient.isConnected()) {
                Snackbar.make(textViewNumMsgs, "Failed to connect to: '$SOLACE_MQTT_HOST' within 3 seconds", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show()
            }
        }

    }

    private fun setMqttCallBack() {
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                val snackbarMsg = "Connected to host:\n'$SOLACE_MQTT_HOST'."
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            override fun connectionLost(throwable: Throwable) {
                val snackbarMsg = "Connection to host lost:\n'$SOLACE_MQTT_HOST'"
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Debug", "Message received from host '$SOLACE_MQTT_HOST': $mqttMessage")
                textViewNumMsgs.text = ("${textViewNumMsgs.text.toString().toInt() + 1}")
                val str: String = "------------"+ Calendar.getInstance().time +"-------------\n$mqttMessage\n${textViewMsgPayload.text}"
                textViewMsgPayload.text = str
                isScanned = true
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$SOLACE_MQTT_HOST'")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        mqttClient.destroy()
        super.onDestroy()
    }


    //Permet de récupérer la liste des utilisateurs et de les afficher dans la liste "userList"
    @SuppressLint("SetTextI18n")
    fun CompareParseValueToSub (tagScan: String){
        var correctTag = false;
        println("CompareParseValueToSub")
        val arrayAdapter: ArrayAdapter<*>
        val gson = Gson()
        val log: Type = object : TypeToken<List<Logs?>?>() {}.type
        Thread.sleep(1000)
        if (valueJson != ""){
            val logs: List<Logs> = gson.fromJson(valueJson, log)
            var tagsList: List<String> = listOf()
            // Filtre les logs pour avoir l'utilisateur et la date convertie en YYYY-MM-DD-HH-mm-SS
            for (log in logs){
                if (log.UID == tagScan){
                    correctTag = true
                }
            }
            var topic = "porte_sub"
            if (correctTag){
                var snackbarMsg : String
                snackbarMsg = "Cannot publish to empty topic!"
                if (topic.isNotEmpty()) {
                    snackbarMsg = try {
                        mqttClient.publish(topic, "true")
                        "Published to topic '$topic'"
                    } catch (ex: MqttException) {
                        "Error publishing to topic: $topic"
                    }
                }
                textViewMsgPayload.text = "Accès autorisé"
            }
            else{
                textViewMsgPayload.text = "Accès refusé"
                mqttClient.publish(topic, "false")
            }
            Log.d("Debug", tagsList.toString())

        }
        else{
            Log.d("Debug", "valueJson is empty")
        }

    }
    // Permet de récupérer la liste des utilisateurs dans l'API et de les stocker dans la variable "valueJson"
    fun run() {
        val request = Request.Builder()
            .url("http://167.114.96.59:2223/getTag")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    valueJson = response.body()!!.string()
                    Log.d("Debug", valueJson)
                }
            }
        })
    }

}
