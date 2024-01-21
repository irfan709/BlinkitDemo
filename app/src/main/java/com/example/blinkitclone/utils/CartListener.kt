package com.example.blinkitclone.utils

interface CartListener {

    fun showCartLayout(itemCount: Int)
    fun savingCartItemCount(itemCount: Int)
    fun hideCartLayout()

}