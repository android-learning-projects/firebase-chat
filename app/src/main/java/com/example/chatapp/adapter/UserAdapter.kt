package com.example.chatapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.LayoutUserItemBinding
import com.example.chatapp.model.Chat
import com.example.chatapp.model.User
import com.example.chatapp.ui.MainActivity
import com.example.chatapp.ui.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAdapter(private val onItemClick: OnItemClick) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder>() {


    private val list = mutableListOf<User>()
    private var isLastChat: Boolean = false

    fun update(list: List<User>, isLastChat: Boolean) {
        this.list.clear()
        this.list.addAll(list)
        this.isLastChat = isLastChat
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.MyViewHolder {
        val binding =
            LayoutUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserAdapter.MyViewHolder, position: Int) {
        holder.bind(list[position], isLastChat, onItemClick)

    }

    class MyViewHolder(private val binding: LayoutUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: User, isLastChat: Boolean, onItemClick: OnItemClick) {

            if (data.imageURL.equals("default")) {
                binding.profileImage.setImageResource(R.drawable.profile_img)
            } else {
                Glide
                    .with(binding.root.context)
                    .load(data.imageURL)
                    .into(binding.profileImage)
            }
            binding.textViewUserName.text = data.username

            binding.frameLayout.setOnClickListener { v ->
                val intent = Intent(v.context, MessageActivity::class.java)
                intent.putExtra("userid", data.id)
                v.context.startActivity(intent)
            }

            if (isLastChat) {
                lastMessage(data.id, binding.textViewLastMessage)
            } else {
                binding.textViewLastMessage.visibility = View.GONE
            }

            if (isLastChat) {
                val isOnline = data.status == "online"

                binding.circleImageViewOnline.visibility = if (isOnline) View.VISIBLE else View.GONE
                binding.circleImageViewOffline.visibility =
                    if (isOnline) View.GONE else View.VISIBLE
            } else {
                binding.circleImageViewOffline.visibility = View.GONE
                binding.circleImageViewOnline.visibility = View.GONE
            }
        }

        // Check for the last message
        private fun lastMessage(userId: String, textViewLastMessage: TextView) {
            var lastMessage = "default"

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val reference = FirebaseDatabase.getInstance().getReference("Chats")

            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val chat: Chat? = snapshot.getValue(Chat::class.java)

                        if (chat?.receiver?.equals(firebaseUser?.uid) == true && chat.sender == userId) {
                            if (chat.receiver.equals(firebaseUser!!.uid) && chat.sender
                                    .equals(userId) ||
                                chat.receiver.equals(userId) && chat.sender
                                    .equals(firebaseUser.uid)
                            ) {
                                lastMessage = chat.message

                            }
                        }

                    }

                    textViewLastMessage.text = when (lastMessage) {
                        "default" -> "No Message"
                        else -> lastMessage
                    }
                    lastMessage = "default"
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }


}