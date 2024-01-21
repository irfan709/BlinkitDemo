package com.example.blinkitclone.utils

import android.widget.Filter
import com.example.blinkitclone.adapters.ProductsAdapter
import com.example.blinkitclone.models.ProductModel
import java.util.Locale

class FilterProducts(
    private val adapter: ProductsAdapter,
    private val productsList: ArrayList<ProductModel>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result = FilterResults()

        if (!constraint.isNullOrBlank()) {
            val filteredList = productsList.filter { product ->
                val query = constraint.toString().trim().uppercase(Locale.getDefault())
                product.productTitle?.uppercase(Locale.getDefault())?.contains(query) == true ||
                        product.productCategory?.uppercase(Locale.getDefault())?.contains(query) == true ||
                        product.productPrice?.toString()?.uppercase(Locale.getDefault())?.contains(query) == true ||
                        product.productType?.uppercase(Locale.getDefault())?.contains(query) == true
            }
            result.values = filteredList
            result.count = filteredList.size
        } else {
            result.values = productsList
            result.count = productsList.size
        }

        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.differ.submitList(results?.values as ArrayList<ProductModel>?)
    }
}