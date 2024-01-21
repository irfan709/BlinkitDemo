package com.example.blinkitclone.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueBtn.setOnClickListener {
            val number = binding.etNumber.text.toString()
            val bundle = Bundle().apply {
                putString("number", number)
            }
            findNavController().navigate(R.id.action_signInFragment_to_otpVerificationFragment, bundle)

        }

        binding.etNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                val numberLength = number?.length

                if (numberLength == 10) {
                    binding.continueBtn.isEnabled = true
                    binding.continueBtn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.signIn_btn
                        )
                    )
                } else {
                    binding.continueBtn.isEnabled = false
                    binding.continueBtn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.btn_disabled
                        )
                    )
                }

            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length != 10) {
                    binding.continueBtn.isEnabled = false
                    binding.continueBtn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.btn_disabled
                        )
                    )
                }
            }

        })
    }
}