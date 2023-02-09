package com.example.lugaresfavoritos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lugaresfavoritos.R
import com.example.lugaresfavoritos.databinding.ActivityAddLugarFavoritoBinding
import com.example.lugaresfavoritos.databinding.ActivityMapBinding
import com.example.lugaresfavoritos.models.LugarFavorito
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private var savedLugarFavorito: LugarFavorito? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if(intent.hasExtra(MainActivity.EXTRA_LUGAR_FAVORITO_PLACE)) {
            savedLugarFavorito =
                intent.getParcelableExtra(MainActivity.EXTRA_LUGAR_FAVORITO_PLACE) as LugarFavorito?

            if(savedLugarFavorito != null) {
                setSupportActionBar(binding.toolbarMap)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.title = savedLugarFavorito!!.title

                binding.toolbarMap.setNavigationOnClickListener {
                    onBackPressed()
                }

                val supportMapFragment: SupportMapFragment =
                    supportFragmentManager.findFragmentById(R.id.toolbarMap) as SupportMapFragment

                supportMapFragment.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(savedLugarFavorito!!.latitude, savedLugarFavorito!!.longitude)
        val titleLocation = savedLugarFavorito!!.location

        googleMap!!.addMarker(MarkerOptions().position(position).title(titleLocation))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)

    }
}