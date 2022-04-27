package com.example.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.LayoutChatItemBinding
import com.example.chatapp.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    companion object {
        val MSG_TYPE_LEFT = 0
        val MSG_TYPE_RIGHT = 1
    }

    private var imageUrl = ""
    private val list = mutableListOf<Chat>()

    var fuser: FirebaseUser? = null
    fun update(list: List<Chat>, imageUrl: String) {
        Timber.d("MessageAdapter:update: ${list}")
        this.list.clear()
        this.list.addAll(list)
        this.imageUrl = imageUrl
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.MyViewHolder {
        val binding =
            LayoutChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MyViewHolder(binding,viewType)
    }

    override fun onBindViewHolder(holder: MessageAdapter.MyViewHolder, position: Int) {
        holder.bind(list[position],  imageUrl, position, list.size)
    }

    class MyViewHolder(private val binding: LayoutChatItemBinding,private val viewType: Int) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Chat,imageUrl: String, position: Int, size: Int) {

           val isSendMessage = viewType==1
            binding.layoutLeft.visibility = if (isSendMessage) View.GONE else View.VISIBLE
            binding.layoutRight.visibility = if (isSendMessage) View.VISIBLE else View.GONE

            if (isSendMessage){
                binding.showMessageRight.text = data.message
            }else{
                binding.showMessageLeft.text = data.message
            }

            if (isSendMessage) { // Left layout
                binding.textSeenRight.text = convertTime(data.time)
            } else { // Right layout
                if (imageUrl == "equals") {
                    Glide.with(binding.root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.profile_img)
                        .into(binding.profileImageLeft)
                }
            }

            if (position == size - 1) {
                binding.textSeenRight.text = if (data.isseen) "Seen" else "Delivered"
            } else {
                binding.textSeenRight.visibility = View.GONE
            }
        }

        private fun convertTime(time: String): String {
            val formatter = SimpleDateFormat("h:mm a")
            val dateString = formatter.format(Date(time.toLong()))
            return dateString
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {

        fuser = FirebaseAuth.getInstance().currentUser

        if (list[position].sender.equals(fuser?.uid)) {
            return MSG_TYPE_RIGHT
        } else {
            return MSG_TYPE_LEFT
        }
    }
}