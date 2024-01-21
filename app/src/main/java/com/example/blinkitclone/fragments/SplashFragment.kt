package com.example.blinkitclone.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.UserActivity
import com.example.blinkitclone.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                authViewModel.isLoggedIn.collect {
                    if (it) {
                        startActivity(Intent(requireActivity(), UserActivity::class.java))
                        requireActivity().finish()
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                    }
                }
            }
        }, 2000)

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }
}