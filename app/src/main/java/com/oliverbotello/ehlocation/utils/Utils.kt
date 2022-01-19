package com.oliverbotello.ehlocation.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun showMessage(message: String) {
    Log.e("Location", message)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}