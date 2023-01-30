package com.example.lugaresfavoritos.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lugaresfavoritos.databinding.ItemLugarFavoritoBinding
import com.example.lugaresfavoritos.models.LugarFavorito

open class LugaresFavoritosAdapter(
    private val context: Context,
    private var list: ArrayList<LugarFavorito>
) : RecyclerView.Adapter<LugaresFavoritosAdapter.ViewHolder>() {

    private var onClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemLugarFavoritoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: LugarFavorito = list[position]

        holder.tvTitle.text = model.title
        holder.tvDescription.text = model.description
        holder.ivPlaceImage.setImageURI(Uri.parse(model.image))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(binding: ItemLugarFavoritoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val ivPlaceImage = binding.ivItemList
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun oncClick(position: Int, model: LugarFavorito)
    }
}
