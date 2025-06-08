package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smarthome.databinding.ActivityMainBinding
import com.example.smarthome.databinding.ActivityMainControlBinding
import kotlinx.serialization.Serializable

class MainControl : AppCompatActivity() {

    private lateinit var binding: ActivityMainControlBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityMainControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val address = intent.getStringExtra("address")

        binding.buttonOutlets.setOnClickListener {

            val intent = Intent(this@MainControl, OutletControl::class.java)
            intent.putExtra("address", address)
            startActivity(intent)

        }

        binding.buttonLights.setOnClickListener {

            val intent = Intent(this@MainControl, LightControl::class.java)
            intent.putExtra("address", address)
            startActivity(intent)

        }
    }
}