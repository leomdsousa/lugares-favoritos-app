package com.example.lugaresfavoritos.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lugaresfavoritos.activities.AddLugarFavoritoActivity
import com.example.lugaresfavoritos.activities.DetailLugarFavoritoActivity
import com.example.lugaresfavoritos.activities.MainActivity
import com.example.lugaresfavoritos.database.DatabaseHandler
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

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailLugarFavoritoActivity::class.java)
            intent.putExtra(MainActivity.LUGAR_FAVORITO_DETAILS, model)
            context.startActivity(intent)
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddLugarFavoritoActivity::class.java )
        intent.putExtra(MainActivity.LUGAR_FAVORITO_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) {
        val dbHandler = DatabaseHandler(context)
        val result = dbHandler.deleteLugarFavorito(list[position])

        if(result > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
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
        fun onClick(position: Int, model: LugarFavorito)
    }
}
