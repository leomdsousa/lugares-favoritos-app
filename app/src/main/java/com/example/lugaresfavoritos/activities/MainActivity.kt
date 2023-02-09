package com.example.lugaresfavoritos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lugaresfavoritos.adapters.LugaresFavoritosAdapter
import com.example.lugaresfavoritos.database.DatabaseHandler
import com.example.lugaresfavoritos.databinding.ActivityMainBinding
import com.example.lugaresfavoritos.models.LugarFavorito
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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

        val editSwipeHandler = object: SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvMain.adapter as LugaresFavoritosAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, LUGAR_FAVORITO_ADD)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.rvMain)

        val deleteSwipeHandler = object: SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvMain.adapter as LugaresFavoritosAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getAllLugaresFavoritos()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvMain)
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

    companion object {
        var LUGAR_FAVORITO_ADD = 1
        var LUGAR_FAVORITO_UPDATE = 2
        var LUGAR_FAVORITO_DELETE = 3
        var LUGAR_FAVORITO_DETAILS = "LUGAR_FAVORITO_DETAILS"
        val EXTRA_LUGAR_FAVORITO_PLACE = ""
    }
}