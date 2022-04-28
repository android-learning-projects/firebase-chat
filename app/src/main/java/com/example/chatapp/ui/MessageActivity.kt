package com.example.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapter.MessageAdapter
import com.example.chatapp.databinding.ActivityMessageBinding
import com.example.chatapp.model.Chat
import com.example.chatapp.model.User
import com.example.chatapp.notifications.*
import com.example.chatapp.ui.fragments.APIService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class MessageActivity : AppCompatActivity() {

    var fuser: FirebaseUser? = null
    var reference: DatabaseReference? = null

    val chats = mutableListOf<Chat>()

    var seenListener: ValueEventListener? = null
    var userid = ""
    var apiService: APIService? = null
    var notify = false

    var messageAdapter: MessageAdapter? = null
    private lateinit var binding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
        }

        apiService = Client.getClient("https://fcm.googleapis.com/")?.create(APIService::class.java)

        binding.recyclerView.setHasFixedSize(true)
        messageAdapter = MessageAdapter()

        userid = intent.getStringExtra("userid").toString()
        fuser = FirebaseAuth.getInstance().currentUser


        binding.buttonSend.setOnClickListener {
            notify = true

            val msg = binding.editTextMessage.text.toString()
            val time = System.currentTimeMillis().toString()

            if (msg.isNotEmpty()) {
                sendMessage(fuser?.uid, userid, msg, time)
            } else {
                Toast.makeText(this, "You can't send empty message", Toast.LENGTH_SHORT).show()
            }

            binding.editTextMessage.setText("")
        }

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)

        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user: User? = dataSnapshot.getValue(User::class.java)
                binding.username.text = user?.username

                if (user != null && fuser != null) {
                    if (user.imageURL.equals("default")) {
                        binding.imageProfile.setImageResource(R.drawable.profile_img)
                    } else {
                        Glide
                            .with(this@MessageActivity)
                            .load(user.imageURL)
                            .into(binding.imageProfile)
                    }
                    readMessages(fuser!!.uid, userid, user.imageURL)

                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        sendMessage(userid)
    }

    private fun sendMessage(userid: String) {
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        seenListener = reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chat: Chat? = snapshot.getValue(Chat::class.java)

                    if (chat != null) {
                        if (chat.receiver == fuser?.uid && chat.sender == userid) {
                            val hashMap = HashMap<String, Any>()
                            hashMap["isseen"] = false
                            snapshot.ref.updateChildren(hashMap)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendMessage(
        sender: String?,
        receiver: String,
        message: String,
        time: String
    ) {

        var reference: DatabaseReference? = FirebaseDatabase.getInstance().reference

        val hashMap = HashMap<String, Any>()
        if (sender != null) {
            hashMap["sender"] = sender
        }
        hashMap["receiver"] = receiver;
        hashMap["message"] = message;
        hashMap["isseen"] = false;
        hashMap["time"] = time;

        reference?.child("Chats")?.push()?.setValue(hashMap)

        val chatRef = fuser?.let {
            FirebaseDatabase
                .getInstance()
                .getReference("Chatlist")
                .child(it.uid)
                .child(userid)
        }

        chatRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        val chatRefReceiver = fuser?.let {
            FirebaseDatabase
                .getInstance()
                .getReference("Chatlist")
                .child(userid)
                .child(it.uid)
        }

        chatRefReceiver?.child("id")?.setValue(fuser?.uid)

        val msg = message

        reference =
            fuser?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it.uid)
            }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                if (notify) {
                    sendNotification(receiver, user?.username, msg)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("onCancelled: $error")
            }
        })
    }

    private fun sendNotification(
        receiver: String,
        userName: String?,
        message: String
    ) {

        val tokens = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = tokens.orderByKey().equalTo(receiver)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children) {
                    val token: Token? = snapshot.getValue(Token::class.java)

                    val data = fuser?.let { data ->
                        Data(
                            data.uid,
                            R.drawable.profile_img,
                            "$userName: $message",
                            "New Message",
                            userid
                        )
                    }
                    val sender = data?.let { token?.let { it1 -> Sender(it, it1.token) } }

                    sender?.let {
                        apiService?.sendNotification(it)?.enqueue(object :
                            Callback<MyResponse> {
                            override fun onResponse(
                                call: retrofit2.Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()?.success ?: false != 1) {

                                    }
                                }
                            }

                            override fun onFailure(
                                call: retrofit2.Call<MyResponse>,
                                t: Throwable
                            ) {
                            }

                        })
                    }


                }


            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun readMessages(myId: String, userId: String, imageUrl: String) {
        chats.clear()

        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chats.clear()

                for (snapshot in dataSnapshot.children) {
                    val chat: Chat? = snapshot.getValue(Chat::class.java)

                    if (chat != null) {
                        if (chat.receiver.equals(myId) && chat.sender.equals(userId) ||
                            chat.receiver.equals(userId) && chat.sender.equals(myId)
                        ) {
                            chats.add(chat)
                        }
                    }
                }
                binding.recyclerView.adapter = messageAdapter

                messageAdapter?.update(chats, imageUrl)


            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun currentUser(userid: String) {
        val editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit()
        editor.putString("currentuser", userid)
        editor.apply()
    }

    private fun status(status: String) {
        reference =
            fuser?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it.uid)
            }

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reference?.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        status("online")
        currentUser(userid)
    }

    override fun onPause() {
        super.onPause()
        status("offline")
        currentUser("none")
    }


}