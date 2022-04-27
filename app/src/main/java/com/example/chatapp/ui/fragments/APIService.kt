package com.example.chatapp.ui.fragments

import com.example.chatapp.notifications.MyResponse
import com.example.chatapp.notifications.Sender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAupAIwg0:APA91bFs5yNvkp-AZtyEhtPiGxdUtXw__obNL8pZx5fnv40wS25Uv82ggeiQOYWRPW_b879qbjMkQ9qfEWQItUflcx4Zn9pzAYmLNk2JYht2CdMZ4SWwz8ZS9rUW93S-KmgeW6CaQjH5"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender): Call<MyResponse>
}