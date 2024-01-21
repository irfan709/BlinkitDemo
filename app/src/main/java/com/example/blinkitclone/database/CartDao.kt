package com.example.blinkitclone.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.blinkitclone.models.CartModel

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartProduct(products: CartModel)

    @Update
    fun updateCartProduct(products: CartModel)

    @Query("DELETE FROM cart_table WHERE productId = :productId")
    fun deleteCartProduct(productId: String)

    @Query("SELECT * FROM cart_table")
    fun getAllProducts(): LiveData<List<CartModel>>

    @Query("DELETE FROM cart_table")
    suspend fun deleteCart()

}