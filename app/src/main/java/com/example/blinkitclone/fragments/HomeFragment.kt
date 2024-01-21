package com.example.blinkitclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.adapters.CategoryAdapter
import com.example.blinkitclone.databinding.FragmentHomeBinding
import com.example.blinkitclone.models.CategoryModel
import com.example.blinkitclone.utils.Constants
import com.example.blinkitclone.viewmodel.UserViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val userViewModel: UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        binding.etSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        showAllCategories()
        getAllProducts()
    }

    private fun getAllProducts() {
        userViewModel.getAllProducts().observe(viewLifecycleOwner) {
            for (i in it) {

            }
        }
    }

    private fun showAllCategories() {
        val categoryList = ArrayList<CategoryModel>()

        for (i in Constants.allProductCategory.indices) {
            categoryList.add(
                CategoryModel(
                    Constants.allProductCategory[i],
                    Constants.allProductCategoryImage[i]
                )
            )
        }

        binding.categoryRv.adapter = CategoryAdapter(categoryList) { category ->
            showSelectedProduct(category)
        }
    }

    private fun showSelectedProduct(category: CategoryModel) {
        val bundle = Bundle().apply {
            putString("category", category.name)
        }

        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
    }
}