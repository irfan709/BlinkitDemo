package com.example.blinkitclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.blinkitclone.databinding.ItemCartBinding
import com.example.blinkitclone.models.CartModel

class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    class CartViewHolder(val binding: ItemCartBinding) : ViewHolder(binding.root)

    val diffutil = object : DiffUtil.ItemCallback<CartModel>() {
        override fun areItemsTheSame(oldItem: CartModel, newItem: CartModel): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartModel, newItem: CartModel): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffutil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = differ.currentList[position]

        holder.binding.apply {
            Glide.with(holder.itemView).load(product.productImage).into(itemCartImage)
            itemProductTitle.text = product.productTitle
            itemProductQuantity.text = product.productQuantity
            itemProductPrice.text = product.productPrice
            itemProductCount.text = product.productCount.toString()
        }
    }
}