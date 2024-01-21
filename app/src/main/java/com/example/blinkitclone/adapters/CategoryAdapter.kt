package com.example.blinkitclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.ItemProductCategoryBinding
import com.example.blinkitclone.models.CategoryModel

class CategoryAdapter(
    val categoryList: ArrayList<CategoryModel>,
    val onProductItemSelected: (category: CategoryModel) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemProductCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemProductCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = categoryList[position]

        holder.binding.apply {
            productCategoryName.text = currentItem.name
            productCategoryImage.setImageResource(currentItem.image)
        }

        holder.itemView.setOnClickListener {
            onProductItemSelected(currentItem)
        }
    }
}