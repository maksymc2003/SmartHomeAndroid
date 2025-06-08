package com.example.smarthome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smarthome.databinding.ActivityMainBinding
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

@Serializable
data class State(val outlet_state: Int, val light_state: Int, val rgb_state_r: Int, val rgb_state_g: Int, val rgb_state_b: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @OptIn(ExperimentalSerializationApi::class)
    private fun connectToServer()
    {
        val inputStr = "http://" + binding.textInputLayout.editText?.text.toString() + "/control"
        val client = NetworkClient.instance

        Toast.makeText(this@MainActivity, "Łączenie...", Toast.LENGTH_SHORT).show()

        try{

            val request = Request.Builder()
                .url(inputStr)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Błąd połączenia!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        var connectionOK = false

                        try
                        {
                            val state = Json.decodeFromString<State>(response.body!!.string())
                            println(state.toString())
                            connectionOK = true
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Nieprawidłowy adres.", Toast.LENGTH_SHORT).show()
                        }

                        if(connectionOK)
                        {
                            val intent = Intent(this@MainActivity, MainControl::class.java)
                            intent.putExtra("address", inputStr)
                            startActivity(intent)

                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Połączono!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })

        }catch (e: Exception)
        {
           e.printStackTrace()
            Toast.makeText(this@MainActivity, "Nieprawidłowy adres.", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding.button.setOnClickListener {

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.textInputLayout.editText?.windowToken, 0)
            binding.textInputLayout.editText?.clearFocus()
            connectToServer()
        }

        binding.textInputLayout.editText?.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
            {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(textView.windowToken, 0)
                binding.textInputLayout.editText?.clearFocus()

                connectToServer()

                true
            }
            else
            {
                false
            }
        }
    }
}