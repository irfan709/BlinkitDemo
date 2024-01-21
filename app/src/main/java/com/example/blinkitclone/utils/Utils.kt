package com.example.blinkitclone.utils

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.blinkitclone.databinding.ProgressLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private var dialog: AlertDialog? = null

    fun showDialog(context: Context, message: String) {
        val progress = ProgressLayoutBinding.inflate(LayoutInflater.from(context))
        progress.progressMessage.text = message
        dialog = AlertDialog.Builder(context).setView(progress.root).setCancelable(false).create()
        dialog!!.show()
    }

    fun hideDialog() {
        dialog?.dismiss()
    }

    private var firebaseAuth: FirebaseAuth? = null

    fun getAuthInstance(): FirebaseAuth {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance()
        }
        return firebaseAuth as FirebaseAuth
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    fun getRandomId(): String {
        val length = 25
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length).map { allowedChars.random() }.joinToString("")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String? {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }

}