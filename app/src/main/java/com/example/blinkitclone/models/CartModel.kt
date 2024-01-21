package com.example.blinkitclone.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartModel(

    @PrimaryKey
    val productId: String = "random",
    val productTitle: String? = null,
    val productQuantity: String? = null,
    val productPrice: String? = null,
    var productCount: Int? = null,
    var productStock: Int? = null,
    var productImage: String? = null,
    var productCategory: String? = null,
    var adminId: String? = null,
    var productType: String? = null

)
