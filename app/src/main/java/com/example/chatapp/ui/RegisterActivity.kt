package com.example.chatapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.chatapp.databinding.ActivityRegisterBinding
import com.example.chatapp.utils.Config
import com.example.chatapp.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var reference: DatabaseReference? = null
    var dialog: AlertDialog? = null

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = "Register"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()

        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "All Fields are required", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                    .show()
            } else {
                register(name, email, password)
            }

        }
    }

    private fun register(name: String, email: String, password: String) {

        dialog = Utils.showLoader(this)

        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth!!.currentUser

                val userId = firebaseUser?.uid
                Timber.d("user_id: $userId")

                if (userId != null) {
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    val hashMap: HashMap<String, String> = HashMap()

                    hashMap[Config.ID] = userId
                    hashMap[Config.USER_NAME] = name
                    hashMap[Config.IMAGE_URL] = "default"
                    hashMap[Config.STATUS] = Config.OFFLINE
                    hashMap[Config.BIO] = ""
                    hashMap[Config.SEARCH] = name.lowercase(Locale.getDefault())


                    reference?.setValue(hashMap)?.addOnCompleteListener { task ->
                        dialog?.dismiss()
                        if (task.isSuccessful) {
                            Utils.showMessage(
                                context = this,
                                message = "Account registered successfully!",
                            )

                        } else {
                            Utils.showMessage(context = this, message = "Something went wrong!")
                        }
                    }


                }
            } else {
                Utils.showMessage(
                    context = this,
                    message = "Something went wrong.\nTry again later!"
                )

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}