package com.example.chatapp.model

data class Chat(
    val sender: String="",
    val receiver: String="",
    val message: String="",
    val isseen: Boolean=false,
    val time: String="",
)