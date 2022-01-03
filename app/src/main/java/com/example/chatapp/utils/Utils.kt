package com.example.chatapp.utils

import android.R
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.example.chatapp.databinding.ProgressdialogBinding

object Utils {

    interface OnClickListener {
        fun onClick()
    }

    fun showLoader(context: Context): AlertDialog {
        val binding = ProgressdialogBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
//        builder.setCancelable(false)
        return builder.show()
    }

    fun showMessage(
        context: Context,
        message: String,
        clickListener: OnClickListener? = null,
        isCancelable: Boolean = true
    ) {
        AlertDialog.Builder(context)
            .setTitle("Alert")
            .setMessage(message)
            .setCancelable(isCancelable)
            .setPositiveButton(
                R.string.ok
            ) { _, _ -> clickListener?.onClick() }
            .show()
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}