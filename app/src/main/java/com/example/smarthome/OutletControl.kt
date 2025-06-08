package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smarthome.databinding.ActivityMainControlBinding
import com.example.smarthome.databinding.ActivityOutletControlBinding
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


class OutletControl : AppCompatActivity() {

    private lateinit var binding: ActivityOutletControlBinding

    private val client = NetworkClient.instance

    private fun getJSON(swState: Boolean): String
    {
        if(swState)
        {
            return  "{\"outlet_state\": 1}"
        }
        else
        {
            return "{\"outlet_state\": 0}"
        }
    }


    private fun postToServer(client: OkHttpClient, address: String, swState: Boolean)
    {
        val postBody = getJSON(swState)

        try{

            val request = Request.Builder()
                .url(address)
                .post(postBody.toRequestBody())
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                    runOnUiThread {
                        Toast.makeText(this@OutletControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            runOnUiThread {
                                Toast.makeText(this@OutletControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })

        }catch (e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(this@OutletControl, "Nieprawidłowy adres.", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getFromServer(client: OkHttpClient, address: String)
    {

        try{

            val request = Request.Builder()
                .url(address)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                    runOnUiThread {
                        Toast.makeText(this@OutletControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            runOnUiThread {
                                Toast.makeText(this@OutletControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val state = Json.decodeFromString<State>(response.body!!.string())

                        if(state.outlet_state == 1)
                        {
                            runOnUiThread {
                                binding.switch1.isChecked = true
                            }
                        }
                        else
                        {
                            runOnUiThread {
                                binding.switch1.isChecked = false
                            }
                        }

                    }
                }
            })

        }catch (e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(this@OutletControl, "Nieprawidłowy adres.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityOutletControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val address = intent.getStringExtra("address")

        if (address != null) {
            getFromServer(client, address)
        }

        binding.switch1.setOnClickListener {

            if (address != null) {
                postToServer(client, address, binding.switch1.isChecked)
            }

        }
    }
}