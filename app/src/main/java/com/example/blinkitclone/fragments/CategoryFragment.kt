package com.example.blinkitclone.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.adapters.ProductsAdapter
import com.example.blinkitclone.databinding.FragmentCategoryBinding
import com.example.blinkitclone.databinding.ItemProductBinding
import com.example.blinkitclone.models.CartModel
import com.example.blinkitclone.models.ProductModel
import com.example.blinkitclone.utils.CartListener
import com.example.blinkitclone.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.lang.ClassCastException

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var productsAdapter: ProductsAdapter
    private var cartListener: CartListener? = null
    private var category: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.searchMenu -> {
                    findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }

        val bundle = arguments
        category = bundle?.getString("category").toString()
        binding.toolbar.title = category

        fetchCategoryProducts()
    }

    private fun fetchCategoryProducts() {
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        lifecycleScope.launch {
            userViewModel.fetchAllCategoryProducts(category!!).collect {

                if (it.isEmpty()) {
                    binding.rvProductsCategory.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                } else {
                    binding.rvProductsCategory.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                productsAdapter = ProductsAdapter(
                    ::onAddButtonClicked,
                    ::onIncrementButtonClicked,
                    ::onDecrementButtonClicked
                )
                binding.rvProductsCategory.adapter = productsAdapter
                productsAdapter.differ.submitList(it)
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        }
    }

    private fun onAddButtonClicked(product: ProductModel, productBinding: ItemProductBinding) {
        productBinding.addProductBtn.visibility = View.GONE
        productBinding.productsAddOrRemoveLayout.visibility = View.VISIBLE

        var itemCount = productBinding.productsCount.text.toString().toInt()
        itemCount++
        productBinding.productsCount.text = itemCount.toString()

        cartListener?.showCartLayout(1)

        product.itemCount = itemCount
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductsInDb(product)
            userViewModel.updateCartItemCount(product, itemCount)
        }
    }

    private fun onIncrementButtonClicked(
        product: ProductModel,
        productBinding: ItemProductBinding
    ) {
        var itemCountInc = productBinding.productsCount.text.toString().toInt()
        itemCountInc++

        if (product.productStock!! + 1 > itemCountInc) {
            productBinding.productsCount.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)

            product.itemCount = itemCountInc
            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductsInDb(product)
                userViewModel.updateCartItemCount(product, itemCountInc)
            }
        }
    }

    private fun onDecrementButtonClicked(
        product: ProductModel,
        productBinding: ItemProductBinding
    ) {
        var itemCountDec = productBinding.productsCount.text.toString().toInt()
        itemCountDec--

        product.itemCount = itemCountDec
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductsInDb(product)
            userViewModel.updateCartItemCount(product, itemCountDec)
        }

        if (itemCountDec > 0) {
            productBinding.productsCount.text = itemCountDec.toString()
        } else {
            lifecycleScope.launch { userViewModel.deleteCartProduct(product.productId!!) }
            productBinding.addProductBtn.visibility = View.VISIBLE
            productBinding.productsAddOrRemoveLayout.visibility = View.GONE
            productBinding.productsCount.text = "0"
        }

        cartListener?.showCartLayout(-1)
    }

    private fun saveProductsInDb(product: ProductModel) {

        val cartProduct = CartModel(
            productId = product.productId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productPrice = "₹" + "${product.productPrice}",
            productCount = product.itemCount,
            productStock = product.productStock,
            productImage = product.productImageUris?.get(0)!!,
            productCategory = product.productCategory,
            adminId = product.adminUid,
            productType = product.productType
        )

        lifecycleScope.launch { userViewModel.insertCartProducts(cartProduct) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is CartListener) {
            cartListener = context
        } else {
            throw ClassCastException("Cart functionality not implemented")
        }

    }
}