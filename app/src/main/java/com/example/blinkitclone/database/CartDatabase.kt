package com.example.blinkitclone.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.blinkitclone.models.CartModel

@Database(
    entities = [CartModel::class],
    version = 1,
    exportSchema = false
)
abstract class CartDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        var INSTANCE: CartDatabase? = null

        fun getDatabaseInstance(context: Context): CartDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance

            synchronized(this) {
                val roomDb =
                    Room.databaseBuilder(context, CartDatabase::class.java, "cart_database")
                        .allowMainThreadQueries().build()
                INSTANCE = roomDb
                return roomDb
            }
        }
    }

}