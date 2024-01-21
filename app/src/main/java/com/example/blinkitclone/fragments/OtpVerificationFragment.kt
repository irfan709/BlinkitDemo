package com.example.blinkitclone.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.UserActivity
import com.example.blinkitclone.databinding.FragmentOtpVerificationBinding
import com.example.blinkitclone.models.UserModel
import com.example.blinkitclone.utils.Utils
import com.example.blinkitclone.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class OtpVerificationFragment : Fragment() {

    private lateinit var binding: FragmentOtpVerificationBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var phoneNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        phoneNumber = arguments?.getString("number").toString()
        binding.tvNumber.text = "+91 $phoneNumber"

        binding.backArrow.setOnClickListener {
            findNavController().navigate(R.id.action_otpVerificationFragment_to_signInFragment)
        }

        binding.loginBtn.setOnClickListener {
            Utils.showDialog(requireContext(), "Verifying otp...")
            val editTexts = arrayOf(
                binding.etOtp1,
                binding.etOtp2,
                binding.etOtp3,
                binding.etOtp4,
                binding.etOtp5,
                binding.etOtp6
            )
            val enteredOtp = editTexts.joinToString("") { it.text.toString() }

            if (enteredOtp.length < editTexts.size) {
                Utils.showToast(requireContext(), "Invalid otp")
            } else {
                editTexts.forEach { it.text?.clear(); it.clearFocus() }
                verifyOtpAndSignIn(enteredOtp)
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_otpVerificationFragment_to_signInFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        otpEditTextListener()
        sendOtp()
    }

    private fun verifyOtpAndSignIn(enteredOtp: String) {
        val user =
            UserModel(uid = Utils.getCurrentUserId(), phoneNumber = phoneNumber, userAddress = " ")
        authViewModel.apply {
            signInWithPhoneAuthCredential(enteredOtp, phoneNumber, user)
            lifecycleScope.launch {
                isSignedInSuccessfully.collect {
                    if (it) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "LoggedIn successfully...")
                        startActivity(Intent(requireActivity(), UserActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }
    }


    private fun sendOtp() {
        Utils.showDialog(requireContext(), "Sending otp...")
        authViewModel.apply {
            sendOtp(phoneNumber, requireActivity())
            lifecycleScope.launch {
                otpSent.collect { otpSent ->
                    if (otpSent) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Otp sent to mobile number")
                    }
                }
            }
        }
    }

    private fun otpEditTextListener() {
        val editTexts = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    // Check the length of the current EditText
                    val length = s?.length ?: 0

                    // Move focus to the next EditText if a digit is entered
                    if (length == 1 && i < editTexts.size - 1) {
                        editTexts[i + 1].requestFocus()
                    }

                    // Move focus to the previous EditText if the digit is deleted
                    if (length == 0 && i > 0) {
                        editTexts[i - 1].requestFocus()
                    }

                    // Check the total length of all EditText fields
                    val totalLength = editTexts.sumOf { it.length() }

                    // Enable or disable the login button based on the total length
                    binding.loginBtn.isEnabled = (totalLength == 6)

                    // Change button background color accordingly
                    binding.loginBtn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (totalLength == 6) R.color.login_btn else R.color.btn_disabled
                        )
                    )
                }
            })

            // This method prevents to enter more than one number in EditText
            editTexts[i].setOnKeyListener { _, keyCode, _ ->
                // Prevent additional input if length is already 1
                editTexts[i].length() == 1 && keyCode != 67 // 67 is the key code for the Backspace key
            }
        }
    }
}