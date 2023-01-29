package com.example.lugaresfavoritos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lugaresfavoritos.database.DatabaseHandler
import com.example.lugaresfavoritos.databinding.ActivityMainBinding
import com.example.lugaresfavoritos.models.LugarFavorito

class MainActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.addFavPlace.setOnClickListener {
            val intent = Intent(this, AddLugarFavoritoActivity::class.java)
            startActivity(intent)
        }

        getAllLugaresFavoritos()
    }

    private fun getAllLugaresFavoritos() {
        val dbHandler = DatabaseHandler(this)
        val list = dbHandler.getAllLugaresFavoritos()
    }
}