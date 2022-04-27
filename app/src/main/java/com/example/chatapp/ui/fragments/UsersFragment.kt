package com.example.chatapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatapp.R
import com.example.chatapp.adapter.OnItemClick
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.databinding.FragmentUsersBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.*


class UsersFragment : Fragment() {

    val users = mutableListOf<User>()
    var userAdapter: UserAdapter? = null

    companion object {
        var onItemClick: OnItemClick? = null

        fun newInstance(onItemClick: OnItemClick): UsersFragment {
            this.onItemClick = onItemClick
            val bundle = Bundle()
            val fragment = UsersFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _binding: FragmentUsersBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
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
        readUsers()

        binding.searchUsers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString().lowercase(Locale.getDefault()))
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun searchUsers(s: String) {
        val fuser = FirebaseAuth.getInstance().currentUser
        val query = FirebaseDatabase
            .getInstance()
            .getReference("Users")
            .orderByChild("search")
            .startAt(s)
            .endAt(s + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                users.clear()

                for (snapshot in dataSnapshot.children) {
                    val user: User? = snapshot.getValue(User::class.java)

                    Timber.d("user_id: ${user?.id}, fuser_id: ${fuser?.uid} User: ${user?.id == fuser?.uid}, name: ${user?.username}")

                    if (user != null && fuser != null) {
                        if (user.id != fuser.uid) {
                            users.add(user)
                        }
                    }
                }

                userAdapter?.update(users, false)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


    }

    private fun readUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnanshot: DataSnapshot) {

                val search = binding.searchUsers.text.toString()

                if (search.isEmpty()) {
                    users.clear()

                    for (snapshot in dataSnanshot.children) {
                        val user: User? = snapshot.getValue(User::class.java)

                        if (user != null && firebaseUser != null && !user.id.equals(firebaseUser.uid)) {
                            users.add(user)
                        }
                    }

                    binding.esLayout.userEs.visibility =
                        if (users.size == 0) View.VISIBLE else View.GONE

                    userAdapter?.update(users, false)


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}