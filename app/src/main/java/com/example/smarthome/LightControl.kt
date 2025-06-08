package com.example.smarthome

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthome.databinding.ActivityLightControlBinding
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class LightControl : AppCompatActivity() {

    private lateinit var binding: ActivityLightControlBinding

    private val client = NetworkClient.instance

    private var valueRed = 0
    private var valueGreen = 0
    private var valueBlue = 0

    private fun map(x: Int, inMin: Int, inMax: Int, outMin: Int, outMax: Int): Int
    {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
    }

    private fun getJSON(lightState: Int): String
    {
        return "{\"light_state\": $lightState}"
    }

    private fun getJSON(rgbR: Int, rgbG: Int, rgbB: Int): String
    {
        return "{\"rgb_state_r\": $rgbR, \"rgb_state_g\": $rgbG, \"rgb_state_b\": $rgbB}"
    }


    private fun postToServer(client: OkHttpClient, address: String, json: String)
    {
        try{

            val request = Request.Builder()
                .url(address)
                .post(json.toRequestBody())
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                    runOnUiThread {
                        Toast.makeText(this@LightControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            runOnUiThread {
                                Toast.makeText(this@LightControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })

        }catch (e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(this@LightControl, "Nieprawidłowy adres.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@LightControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            runOnUiThread {
                                Toast.makeText(this@LightControl, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val state = Json.decodeFromString<State>(response.body!!.string())


                        runOnUiThread {
                            binding.seekBar.progress = state.light_state
                            binding.seekBarRed.progress = state.rgb_state_r
                            binding.seekBarGreen.progress = state.rgb_state_g
                            binding.seekBarBlue.progress = state.rgb_state_b

                            binding.textViewBrightness.text = "Jasność: " + map(state.light_state, 0, 255, 0, 100).toString() + "%"
                            binding.textViewRed.text = "Czerwony: " + map(state.rgb_state_r, 0, 255, 0, 100).toString() + "%"
                            binding.textViewGreen.text = "Zielony: " + map(state.rgb_state_g, 0, 255, 0, 100).toString() + "%"
                            binding.textViewBlue.text = "Niebieski: " + map(state.rgb_state_b, 0, 255, 0, 100).toString() + "%"
                        }

                        valueRed = state.rgb_state_r
                        valueGreen = state.rgb_state_g
                        valueBlue = state.rgb_state_b

                    }
                }
            })

        }catch (e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(this@LightControl, "Nieprawidłowy adres.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityLightControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val address = intent.getStringExtra("address")

        if (address != null) {

            getFromServer(client, address)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {

                if (address != null) {
                    postToServer(client, address, getJSON(binding.seekBar.progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                binding.textViewBrightness.text = "Jasność: " + map(progress, 0, 255, 0, 100).toString() + "%"

            }
        });

        binding.seekBarRed.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (address != null) {
                    postToServer(client, address, getJSON(valueRed, valueGreen, valueBlue))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                valueRed = progress
                binding.textViewRed.text = "Czerwony: " + map(progress, 0, 255, 0, 100).toString() + "%"

            }
        });

        binding.seekBarGreen.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {

                if (address != null) {
                    postToServer(client, address, getJSON(valueRed, valueGreen, valueBlue))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                valueGreen = progress
                binding.textViewGreen.text = "Zielony: " + map(progress, 0, 255, 0, 100).toString() + "%"

            }
        });

        binding.seekBarBlue.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (address != null) {
                    postToServer(client, address, getJSON(valueRed, valueGreen, valueBlue))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                valueBlue = progress
                binding.textViewBlue.text = "Niebieski: " + map(progress, 0, 255, 0, 100).toString() + "%"

            }
        });
    }
}