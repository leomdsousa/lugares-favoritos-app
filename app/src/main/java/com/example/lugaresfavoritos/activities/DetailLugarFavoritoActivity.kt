package com.example.lugaresfavoritos.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lugaresfavoritos.R
import com.example.lugaresfavoritos.databinding.ActivityDetailLugarFavoritoMainBinding
import com.example.lugaresfavoritos.databinding.ActivityMainBinding
import com.example.lugaresfavoritos.models.LugarFavorito

class DetailLugarFavoritoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailLugarFavoritoMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLugarFavoritoMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var lugarFavoritoDetails: LugarFavorito? = null

        if(intent.hasExtra(MainActivity.LUGAR_FAVORITO_DETAILS)) {
            lugarFavoritoDetails = intent.getParcelableExtra(MainActivity.LUGAR_FAVORITO_DETAILS)!!
        }

        if(lugarFavoritoDetails != null) {
            setSupportActionBar(binding.toolbarDetailLugarFavorito)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = lugarFavoritoDetails.title

            binding.toolbarDetailLugarFavorito.setOnClickListener {
                onBackPressed()
            }

            binding.ivImage.setImageURI(Uri.parse(lugarFavoritoDetails.image))
            binding.tvDescription.setText(lugarFavoritoDetails.description)
            binding.tvLocation.setText(lugarFavoritoDetails.location)

            binding.btnViewOnMap.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_LUGAR_FAVORITO_PLACE, lugarFavoritoDetails)
            }
        }
    }
}