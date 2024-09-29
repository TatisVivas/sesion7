package com.example.sesion7

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sesion7.databinding.ActivityGoogleMapsBinding
import com.example.sesion7.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.googleMaps.setOnClickListener {
            //this es listener y basecontext actividad
            startActivity(Intent(baseContext, GoogleMapsActivity::class.java))
        }
        binding.osmaps.setOnClickListener {
            startActivity(Intent(baseContext, OSMapsActivity::class.java))
        }
    }
}