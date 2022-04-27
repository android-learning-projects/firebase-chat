package com.example.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapter.OnItemClick
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.model.Chat
import com.example.chatapp.model.User
import com.example.chatapp.ui.fragments.ChatFragment
import com.example.chatapp.ui.fragments.ProfileFragment
import com.example.chatapp.ui.fragments.UsersFragment
import com.example.chatapp.utils.Utils
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), OnItemClick {

    var doubleBackToExitPressedOnce = false

    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var onItemClick: OnItemClick? = null

    var dialog: AlertDialog? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        this.onItemClick = this
        // Setup support actionbar
        supportActionBar?.hide()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        binding.profileImage.setOnClickListener {
            binding.tabs.getTabAt(2)?.select()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            reference =
                FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)

            reference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val user: User? = snapshot.getValue(User::class.java)
                    binding.username.text = user?.username

                    if (user?.imageURL != null && user.imageURL == "default") {
                        binding.profileImage.setImageResource(R.drawable.profile_img)
                    } else {
                        Glide
                            .with(this@MainActivity)
                            .load(user?.imageURL)
                            .into(binding.profileImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            reference =
                FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser!!.uid)

            dialog = Utils.showLoader(this)

            reference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val viewPagerAdapter = ViewPagerAdapter(this@MainActivity)

                    var unread = 0

                    for (snapshot in dataSnapshot.children) {
                        val chat: Chat? = snapshot.getValue(Chat::class.java)

                        if (chat?.receiver?.equals(firebaseUser?.uid) == true && !chat.isseen) {
                            unread++
                        }
                    }

                    if (unread == 0) {
                        viewPagerAdapter.addFragment(
                            ChatFragment.newInstance(onItemClick as MainActivity),
                            "Chats"
                        )
                    } else {
                        viewPagerAdapter.addFragment(
                            ChatFragment.newInstance(onItemClick as MainActivity),
                            "($unread) Chats"
                        )

                    }
                    viewPagerAdapter.addFragment(
                        UsersFragment.newInstance(onItemClick as MainActivity),
                        "Users"
                    )
                    viewPagerAdapter.addFragment(ProfileFragment(), "Profile")

                    binding.viewPager.adapter = viewPagerAdapter
//                    binding.tabs.setupWithViewPager(binding.viewPager)

                    TabLayoutMediator(binding.tabs,binding.viewPager){tab,position->
                        tab.text = when(position){
                            0->"Chat"
                            1->"Users"
                            2->"Profile"
                            else->""
                        }
                    }.attach()

                    dialog?.dismiss()

                }

                override fun onCancelled(error: DatabaseError) {
                    dialog?.dismiss()
                }
            })

        }

//        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//            }
//
//            override fun onPageSelected(position: Int) {
//                Utils.hideKeyboard(this@MainActivity)
//            }
//        })
    }

    class ViewPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {

        private val fragments = mutableListOf<Fragment>()
        private val titles = mutableListOf<String>()


        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }


        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return  fragments[position]
        }

//        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }

    override fun onItemClick(uid: String, view: View) {
    }


    private fun status(status: String) {
        reference =
            firebaseUser?.let { FirebaseDatabase.getInstance().getReference("Users").child(it.uid) }

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reference?.updateChildren(hashMap)
    }


    override fun onResume() {
        super.onResume()
        status("online")
    }

    override fun onPause() {
        super.onPause()
        status("offline")
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true

        Toast.makeText(this, "Please click Back again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                );
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}