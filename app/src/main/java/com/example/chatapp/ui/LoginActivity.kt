package com.example.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.chatapp.databinding.ActivityLoginBinding
import com.example.chatapp.utils.Utils
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var dialog: AlertDialog? = null

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.buttonLogin.setOnClickListener {

            login()
        }


    }

    private fun login() {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()

        Utils.hideKeyboard(this)

        if (email.isEmpty()) {
            binding.editTextEmail.error = "Enter email"
        } else if (password.isEmpty()) {
            binding.editTextPassword.error = "Enter password"
        } else if (password.length < 6) {
            Utils.showMessage(this, message = "Password length should be minimum 6")
        } else {
            dialog = Utils.showLoader(this)

            auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    Utils.showMessage(this, "success!")
                } else {
                    Utils.showMessage(this, "Authentication Failed!")
                }

                dialog?.dismiss()

            }


        }
    }
}