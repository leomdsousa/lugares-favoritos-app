package com.example.lugaresfavoritos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lugaresfavoritos.adapters.LugaresFavoritosAdapter
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

        val list = getAllLugaresFavoritos()
        showAllLugaresFavoritos(list)

        setupRecyclerView(list)
    }

    override fun onResume() {
        super.onResume()
        val list = getAllLugaresFavoritos()
        showAllLugaresFavoritos(list)
    }

    private fun setupRecyclerView(list: ArrayList<LugarFavorito>) {
        binding.rvMain.layoutManager = LinearLayoutManager(this)

        val adapter = LugaresFavoritosAdapter(this, list)
        binding.rvMain.adapter = adapter

        //adapter.setOnClickListener(object: LugaresFavoritosAdapter.OnClickListener {
        //    override fun onClick(position: Int, model: LugarFavorito) {

        //    }
        //})
    }

    private fun getAllLugaresFavoritos(): ArrayList<LugarFavorito> {
        val dbHandler = DatabaseHandler(this)
        val list = dbHandler.getAllLugaresFavoritos()

        return list
    }

    private fun showAllLugaresFavoritos(list: ArrayList<LugarFavorito>) {
        if(list.size > 0) {
            binding.rvMain.visibility = View.VISIBLE
            binding.textDefaultForEmptyList.visibility = View.GONE
            setupRecyclerView(list)
        } else {
            binding.rvMain.visibility = View.GONE
            binding.textDefaultForEmptyList.visibility = View.VISIBLE
        }
    }
}