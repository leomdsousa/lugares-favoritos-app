package com.example.lugaresfavoritos.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getDoubleOrNull
import com.example.lugaresfavoritos.models.LugarFavorito

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "DB_LUGARES_FAVORITOS"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "TB_LUGARES_FAVORITOS"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_IMAGE = "image"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val stringCreateTable =
            ("CREATE TABLE ${TABLE_NAME} ( " +
                "${COLUMN_ID} INTEGER PRIMARY KEY, " +
                "${COLUMN_TITLE} TEXT, " +
                "${COLUMN_DESCRIPTION} TEXT, " +
                "${COLUMN_DATE} TEXT, " +
                "${COLUMN_LOCATION} TEXT, " +
                "${COLUMN_LATITUDE} TEXT, " +
                "${COLUMN_LONGITUDE} TEXT, " +
                "${COLUMN_IMAGE} TEXT ) ")

        db?.execSQL(stringCreateTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, key1: Int, key2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS ${TABLE_NAME}")
        onCreate(db)
    }

    fun addLugarFavorito(model: LugarFavorito): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(COLUMN_TITLE, model.title)
        contentValues.put(COLUMN_DESCRIPTION, model.description)
        contentValues.put(COLUMN_IMAGE, model.image)
        contentValues.put(COLUMN_DATE, model.date)
        contentValues.put(COLUMN_LOCATION, model.location)
        contentValues.put(COLUMN_LATITUDE, model.latitude)
        contentValues.put(COLUMN_LONGITUDE, model.longitude)

        val result = db.insert(TABLE_NAME, null, contentValues)

        db.close()
        return result
    }

    fun editLugarFavorito(model: LugarFavorito): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(COLUMN_TITLE, model.title)
        contentValues.put(COLUMN_DESCRIPTION, model.description)
        contentValues.put(COLUMN_IMAGE, model.image)
        contentValues.put(COLUMN_DATE, model.date)
        contentValues.put(COLUMN_LOCATION, model.location)
        contentValues.put(COLUMN_LATITUDE, model.latitude)
        contentValues.put(COLUMN_LONGITUDE, model.longitude)

        val whereClause = "$COLUMN_ID = ${model.id}"

        val result = db.update(TABLE_NAME, contentValues, whereClause, null)

        db.close()
        return result
    }

    fun deleteLugarFavorito(model: LugarFavorito): Int {
        val db = this.writableDatabase

        val whereClause = "$COLUMN_ID = ${model.id}"

        val result = db.delete(TABLE_NAME, whereClause, null)

        db.close()
        return result
    }

    fun getAllLugaresFavoritos(): ArrayList<LugarFavorito> {
        val lugaresFavoritos = ArrayList<LugarFavorito>()
        val query = "SELECT * FROM ${TABLE_NAME}"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(query, null)

            if(cursor.moveToFirst()) {
                do {
                    val linha = LugarFavorito(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
                    )

                    lugaresFavoritos.add(linha)
                } while(cursor.moveToNext())
            }
        } catch (e: SQLiteException) {
            db.execSQL(query)
            return ArrayList()
        }

        return lugaresFavoritos
    }
}