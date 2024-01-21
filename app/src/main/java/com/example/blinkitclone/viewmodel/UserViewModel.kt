package com.example.blinkitclone.viewmodel

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blinkitclone.database.CartDao
import com.example.blinkitclone.database.CartDatabase
import com.example.blinkitclone.models.CartModel
import com.example.blinkitclone.models.OrdersModel
import com.example.blinkitclone.models.ProductModel
import com.example.blinkitclone.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("My_Pref", MODE_PRIVATE)
    val cartDao: CartDao = CartDatabase.getDatabaseInstance(application).cartDao()

    fun insertCartProducts(products: CartModel) {
        cartDao.insertCartProduct(products)
    }

    fun updateCartProduct(products: CartModel) {
        cartDao.updateCartProduct(products)
    }

    fun deleteCartProduct(productId: String) {
        cartDao.deleteCartProduct(productId)
    }

    fun getAllProducts(): LiveData<List<CartModel>> {
        return cartDao.getAllProducts()
    }

    suspend fun deleteCart() {
        cartDao.deleteCart()
    }

    fun updateCartItemCount(product: ProductModel, itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("All Products/${product.productId}")
            .child("itemCount")
            .setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Category/${product.productCategory}/${product.productId}")
            .child("itemCount")
            .setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Type/${product.productType}/${product.productId}")
            .child("itemCount")
            .setValue(itemCount)
    }

    fun fetchAllProducts(): Flow<List<ProductModel>> = callbackFlow {
        val database = FirebaseDatabase.getInstance().getReference("Admins").child("All Products")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = ArrayList<ProductModel>()
                for (product in snapshot.children) {
                    val products = product.getValue(ProductModel::class.java)
                    productsList.add(products!!)
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }

        database.addValueEventListener(eventListener)
        awaitClose { database.removeEventListener(eventListener) }
    }

    fun fetchAllCategoryProducts(category: String): Flow<List<ProductModel>> = callbackFlow {
        val databaseReference = if (category == "All") {
            FirebaseDatabase.getInstance().getReference("Admins").child("All Products")
        } else {
            FirebaseDatabase.getInstance().getReference("Admins")
                .child("Product Category/${category}")
        }

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = ArrayList<ProductModel>()
                for (product in snapshot.children) {
                    val productModel = product.getValue(ProductModel::class.java)
                    productModel?.let {
                        productsList.add(it)
                    }
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }

        databaseReference.addValueEventListener(eventListener)

        awaitClose { databaseReference.removeEventListener(eventListener) }
    }

    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalCartItemsCount(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun saveAddressStatus() {
        sharedPreferences.edit().putBoolean("addressStatus", true).apply()
    }

    fun getUserAddress(callback: (String?) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users")
            .child(Utils.getCurrentUserId()).child("userAddress")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val address = snapshot.getValue(String::class.java)
                    callback(address)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }

        })
    }

    fun saveOrderedProducts(orders: OrdersModel) {
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders")
            .child(orders.orderId!!).setValue(orders)
    }

    fun saveProductsAfterOrder(stock: Int, product: CartModel) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("All Products/${product.productId}")
            .child("itemCount")
            .setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Category/${product.productCategory}/${product.productId}")
            .child("itemCount")
            .setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Type/${product.productType}/${product.productId}")
            .child("itemCount")
            .setValue(0)


        FirebaseDatabase.getInstance().getReference("Admins")
            .child("All Products/${product.productId}")
            .child("productStock")
            .setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Category/${product.productCategory}/${product.productId}")
            .child("productStock")
            .setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Type/${product.productType}/${product.productId}")
            .child("productStock")
            .setValue(stock)


    }

    fun getAddressStatus(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }

    fun saveUserAddress(address: String) {
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users")
            .child(Utils.getCurrentUserId())
            .child("userAddress").setValue(address)
    }
}