package com.example.blinkitclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.example.blinkitclone.adapters.CartAdapter
import com.example.blinkitclone.databinding.ActivityUserBinding
import com.example.blinkitclone.databinding.CartBottomLayoutBinding
import com.example.blinkitclone.models.CartModel
import com.example.blinkitclone.utils.CartListener
import com.example.blinkitclone.viewmodel.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class UserActivity : AppCompatActivity(), CartListener {

    private lateinit var binding: ActivityUserBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var cartModel: List<CartModel>
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.proceedCheckOut.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))

//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left)
        }

        getTotalItemsInCart()
        onCartClicked()
        getAllProducts()
    }

    private fun getAllProducts() {
        userViewModel.getAllProducts().observe(this) {
            cartModel = it
        }
    }

    private fun onCartClicked() {
        binding.itemsLayout.setOnClickListener {
            try {
                val bottomSheetBinding = CartBottomLayoutBinding.inflate(LayoutInflater.from(this))
                val bottomSheet = BottomSheetDialog(this)
                bottomSheet.setContentView(bottomSheetBinding.root)

                bottomSheetBinding.proceedCheckOut.setOnClickListener {
                    startActivity(Intent(this, OrdersActivity::class.java))
                }

                bottomSheetBinding.cartItemsCount.text = binding.noOfProducts.text
                cartAdapter = CartAdapter()
                bottomSheetBinding.rvCartProducts.adapter = cartAdapter
                cartAdapter.differ.submitList(cartModel)
                bottomSheet.show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("InflationError", "Error inflating CartBottomLayoutBinding: ${e.message}")
            }
        }
    }

    private fun getTotalItemsInCart() {
        userViewModel.fetchTotalCartItemsCount().observe(this) {
            if (it > 0) {
                binding.itemAndCheckoutLayout.visibility = View.VISIBLE
                binding.noOfProducts.text = it.toString()
            } else {
                binding.itemAndCheckoutLayout.visibility = View.GONE
            }
        }
    }

    override fun showCartLayout(itemCount: Int) {
        val previousCount = binding.noOfProducts.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if (updatedCount > 0) {
            binding.itemAndCheckoutLayout.visibility = View.VISIBLE
            binding.noOfProducts.text = updatedCount.toString()
        } else {
            binding.itemAndCheckoutLayout.visibility = View.GONE
            binding.noOfProducts.text = "0"
        }

    }

    override fun savingCartItemCount(itemCount: Int) {
        userViewModel.fetchTotalCartItemsCount().observe(this) {
            userViewModel.savingCartItemCount(it + itemCount)
        }
    }

    override fun hideCartLayout() {
        binding.itemAndCheckoutLayout.visibility = View.GONE
        binding.noOfProducts.text = "0"
    }
}