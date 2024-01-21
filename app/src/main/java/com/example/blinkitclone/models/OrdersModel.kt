package com.example.blinkitclone.models

data class OrdersModel(

    val orderId: String? = null,
    val ordersList: List<CartModel>? = null,
    val userAddress: String? = null,
    val orderStatus: Int? = 0,
    val orderDate: String? = null,
    val orderingUserId: String? = null

)
