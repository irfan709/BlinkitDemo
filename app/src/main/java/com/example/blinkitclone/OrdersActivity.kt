package com.example.blinkitclone

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.example.blinkitclone.adapters.CartAdapter
import com.example.blinkitclone.databinding.ActivityOrdersBinding
import com.example.blinkitclone.databinding.AddressLayoutBinding
import com.example.blinkitclone.models.OrdersModel
import com.example.blinkitclone.utils.CartListener
import com.example.blinkitclone.utils.Constants
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.viewmodel.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter
    private lateinit var b2BPGRequest: B2BPGRequest
    private var cartListener: CartListener? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializePhonePe()

        binding.toolbar.setNavigationOnClickListener {
            val slide = Slide().apply {
                slideEdge = Gravity.END
                duration = 1000
            }
            TransitionManager.beginDelayedTransition(window.decorView as ViewGroup, slide)
            startActivity(Intent(this, UserActivity::class.java))
            finishAfterTransition()
        }

        binding.checkoutBtn.setOnClickListener {
            userViewModel.getAddressStatus().observe(this) { status ->
                if (status) {
//                    getPaymentView()
                    placeOrder()
                    lifecycleScope.launch { userViewModel.deleteCart() }
                    userViewModel.savingCartItemCount(0)
                    cartListener?.hideCartLayout()
                    startActivity(Intent(this, UserActivity::class.java))
                } else {
                    val addressLayoutBinding =
                        AddressLayoutBinding.inflate(LayoutInflater.from(this))
                    val dialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()
                    dialog.show()

                    addressLayoutBinding.saveAddressBtn.setOnClickListener {
                        saveAddress(dialog, addressLayoutBinding)
                    }
                }
            }
        }

        getAllCartProducts()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun placeOrder() {
        Log.d("OrdersActivity", "Placing order...")

        userViewModel.getAllProducts().observe(this) { cartList ->
            Log.d("OrdersActivity", "Received cartList: $cartList")

            if (cartList.isNotEmpty()) {
                userViewModel.getUserAddress { address ->
                    Log.d("OrdersActivity", "User address: $address")

                    val order = OrdersModel(
                        orderId = Utils.getRandomId(),
                        userAddress = address,
                        orderStatus = 0,
                        orderDate = Utils.getCurrentDate(),
                        orderingUserId = Utils.getCurrentUserId()
                    )

                    userViewModel.saveOrderedProducts(order)

                    for (product in cartList) {
                        userViewModel.insertCartProducts(product)
                        val count = product.productCount
                        val stock = product.productStock?.minus(count ?: 0)

                        if (stock != null) {
                            userViewModel.saveProductsAfterOrder(stock, product)
                        }
                    }

                    Log.d("OrdersActivity", "Order placed successfully.")
                }
            }
        }
    }

    val phonePeView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == 1) {
            checkPaymentStatus()
        }
    }

    private fun checkPaymentStatus() {
        val xVerify =
            sha256("/pg/v1/status/${Constants.MERCHANT_ID}/${Constants.TXNID}${Constants.SALT_KEY}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X_VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constants.MERCHANT_ID
        )
    }

    private fun getPaymentView() {

        try {
            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator").let {
                phonePeView.launch(it)
            }
        } catch (e: PhonePeInitException) {
            Utils.showToast(this, e.message.toString())
        }
    }

    private fun initializePhonePe() {
        val data = JSONObject()
        PhonePe.init(this, PhonePeEnvironment.SANDBOX, Constants.MERCHANT_ID, "")

        data.put("merchantId", Constants.MERCHANT_ID)
        data.put("merchantTransactionId", Constants.TXNID)
        data.put("amount", 200)
        data.put("mobileNumber", "")
        data.put("callbackUrl", "https://webhook.site/callback-uri")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")

        data.put("paymentInstrument", paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOS", "ANDROID")
        data.put("deviceContext", deviceContext)

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), Base64.NO_WRAP
        )

        val checkSum = sha256(payloadBase64 + Constants.APIENDPOINT + Constants.SALT_KEY) + "###1"

        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checkSum)
            .setUrl(Constants.APIENDPOINT)
            .build()
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun saveAddress(dialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Saving address...")
        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etAddressLine.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhoneNumber"

        lifecycleScope.launch {
            userViewModel.apply {
                saveUserAddress(address)
                saveAddressStatus()
            }
        }

        Utils.showToast(this, "Address saved...")
        dialog.dismiss()
        Utils.hideDialog()
    }

    private fun getAllCartProducts() {
        userViewModel.getAllProducts().observe(this) { cartList ->

            cartAdapter = CartAdapter()
            binding.rvProductItems.adapter = cartAdapter
            cartAdapter.differ.submitList(cartList)

            var totalPrice = 0

            for (product in cartList) {
                val price = product.productPrice?.substring(1)?.toInt()
                val itemCount = product.productCount
                totalPrice += (itemCount?.let { price?.times(it) }!!)
            }

            binding.productSubTotal.text = totalPrice.toString()

            if (totalPrice < 200) {
                binding.productDeliveryCharge.text = "₹15"
                totalPrice += 15
            }

            binding.productPrice.text = totalPrice.toString()
        }
    }
}