package com.example.chatapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatapp.R
import com.example.chatapp.adapter.OnItemClick
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.model.ChatList
import com.example.chatapp.model.User
import com.example.chatapp.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging


class ChatFragment : Fragment() {


    private var users = mutableListOf<User>()

    var fuser: FirebaseUser? = null
    var reference: DatabaseReference? = null

    var userList = mutableListOf<ChatList>()

    var userAdapter: UserAdapter? = null

    companion object {
        var onItemClick: OnItemClick? = null

        fun newInstance(click: OnItemClick): ChatFragment {
            onItemClick = click
            val bundle = Bundle()

            val fragment = ChatFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        userAdapter = UserAdapter(object : OnItemClick {
            override fun onItemClick(uid: String, view: View) {
            }
        })
        binding.recyclerView.adapter = userAdapter

        fuser = FirebaseAuth.getInstance().currentUser

        reference = fuser?.let { fuser ->
            FirebaseDatabase.getInstance().getReference("Chatlist").child(
                fuser.uid
            )
        }

        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()

                for (snapshot in dataSnapshot.children) {
                    val chatList: ChatList? = snapshot.getValue(ChatList::class.java)
                    chatList?.let { userList.add(it) }
                }

                binding.esLayout.userEs.visibility =
                    if (userList.size == 0) View.VISIBLE else View.GONE

                chatList()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        FirebaseMessaging.getInstance().token.addOnSuccessListener { s ->
            updateToken(s)
        }
    }

    private fun chatList() {
        users.clear()

        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                users.clear()

                for (snapshot in dataSnapshot.children) {
                    val user: User? = snapshot.getValue(User::class.java)

                    for (list in userList){
                        if (user!=null){
                            if (user.id==list.id){
                                users.add(user)
                            }
                        }
                    }
                }

                userAdapter?.update(users, true)

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateToken(s: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(s)
        fuser?.let {
            reference.child(it.uid).setValue(token)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}