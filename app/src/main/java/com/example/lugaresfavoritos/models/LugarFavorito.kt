package com.example.lugaresfavoritos.models

import android.os.Parcel
import android.os.Parcelable

data class LugarFavorito(
    val id: Int,
    val title: String?,
    val description: String?,
    val date: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double,
    val image: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LugarFavorito> {
        override fun createFromParcel(parcel: Parcel): LugarFavorito {
            return LugarFavorito(parcel)
        }

        override fun newArray(size: Int): Array<LugarFavorito?> {
            return arrayOfNulls(size)
        }
    }
}
