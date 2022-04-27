package com.example.chatapp.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseIdService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { s ->
                updateToken(s)
            }
        }
    }

    private fun updateToken(refreshToken: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(refreshToken)
        if (firebaseUser != null) {
            reference.child(firebaseUser.uid).setValue(token)
        }
    }
}