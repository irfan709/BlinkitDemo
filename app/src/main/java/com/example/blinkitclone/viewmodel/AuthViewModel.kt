package com.example.blinkitclone.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.blinkitclone.models.UserModel
import com.example.blinkitclone.utils.Utils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val _verificationId = MutableStateFlow<String?>(null)
    private val _otpSent = MutableStateFlow(false)
    val otpSent = _otpSent
    private val _isSignedInSuccessfully = MutableStateFlow(false)
    val isSignedInSuccessfully = _isSignedInSuccessfully
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn

    init {
        Utils.getAuthInstance().currentUser?.let {
            _isLoggedIn.value = true
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        val options = phoneNumber.let {
            PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
                .setPhoneNumber("+91$phoneNumber")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                    }

                    override fun onVerificationFailed(p0: FirebaseException) {

                    }

                    override fun onCodeSent(
                        verificationId: String,
                        p1: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(verificationId, p1)
                        _verificationId.value = verificationId
                        _otpSent.value = true
                    }

                }).build()
        }
        options.let { PhoneAuthProvider.verifyPhoneNumber(it) }
    }

    fun signInWithPhoneAuthCredential(otp: String, phoneNumber: String, user: UserModel) {
        val credential = PhoneAuthProvider.getCredential(_verificationId.value.toString(), otp)
        Utils.getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authenticatedUser = Utils.getAuthInstance().currentUser
                    if (authenticatedUser != null) {
                        val userId = authenticatedUser.uid
                        user.uid = userId

                        FirebaseDatabase.getInstance().getReference("AllUsers")
                            .child("Users")
                            .child(userId)
                            .setValue(user)

                        _isSignedInSuccessfully.value = true
                    } else {
                        // Handle the case when authenticated user is null
                    }
                } else {
                    // Handle the case when authentication is not successful
                }
            }
    }

}