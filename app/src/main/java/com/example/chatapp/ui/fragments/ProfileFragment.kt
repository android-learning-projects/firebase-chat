package com.example.chatapp.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapter.OnItemClick
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.model.User
import com.example.chatapp.utils.Utils
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import timber.log.Timber
import kotlin.jvm.Throws
import com.example.chatapp.ui.MainActivity
import com.example.chatapp.utils.FileCompressor
import com.example.chatapp.utils.ImageCompression
import com.example.chatapp.utils.Resource

import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.File
import java.lang.Exception
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ProfileFragment : Fragment() {

    private var localPathGlobal: String?=null
    var localPath: File? = null
    var reference: DatabaseReference? = null
    var fuser: FirebaseUser? = null
    var storageReference: StorageReference? = null
    var imageUri: Uri? = null
    var dialog: AlertDialog? = null
    var uploadTask: UploadTask? = null

    private var _binding: FragmentProfileBinding? = null

    var mPhotoFile: File? = null
    var mCompressor: FileCompressor? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCompressor = FileCompressor(context)

        storageReference = FirebaseStorage.getInstance().getReference("uploads")

        fuser = FirebaseAuth.getInstance().currentUser

        reference =
            fuser?.let { FirebaseDatabase.getInstance().getReference("Users").child(it.uid) }


        binding.imageViewProfile.setOnClickListener {
            getPermission()
        }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)

                if (user != null) {
                    binding.textViewUserName.text = user.username
                    binding.editTextUaserName.setText(user.username)
                    binding.editTextDescription.setText(user.bio)

                    if (user.imageURL != null && user.imageURL.equals("default")) {
                        binding.imageViewProfile.setImageResource(R.drawable.profile_img)
                    } else {
                        context?.let {
                            Glide
                                .with(it)
                                .load(user.imageURL)
                                .placeholder(R.drawable.profile_img)
                                .into(binding.imageViewProfile)
                        }
                        binding.imageViewProfile
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("onCancelled: $error")
            }
        })

        binding.buttonUpdate.setOnClickListener {

            dialog = context?.let { it1 -> Utils.showLoader(it1) }

            reference?.child("bio")?.setValue(binding.editTextDescription.text.toString())
                ?.addOnCompleteListener { task ->
                    dialog?.dismiss()
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Profile Updated...", Toast.LENGTH_SHORT)
                    } else {
                        Toast.makeText(context, "Unable to Save...", Toast.LENGTH_SHORT)
                    }
                }

            reference?.child("username")?.setValue(binding.editTextUaserName.text.toString())
                ?.addOnCompleteListener { task ->
                    dialog?.dismiss()
                    uploadImage()

                    if (task.isSuccessful) {
                        Toast.makeText(context, "Profile Updated...", Toast.LENGTH_SHORT)
                    } else {
                        Toast.makeText(context, "Unable to Save...", Toast.LENGTH_SHORT)
                    }
                }

        }
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Utils.registerForActivityResult(
                    result.data,
                    requireContext(),
                    object : Resource.CallBack {
                        override fun callBack(resource: Resource<Any>) {




                            val data: Uri = resource.data as Uri
                            Timber.d("callBack: uri: $data, localPath: $localPath, mPhotoFile: $mPhotoFile")

                            val localPath = Utils.getPath(requireContext(),
                                data
                            )


                            Timber.d( "callBack: uri: $data, localPath: $localPath")
                            if (localPath != null)
                                ImageCompression(requireContext(),
                                    object : Resource.CallBack {
                                        override fun callBack(resource: Resource<Any>) {
                                            Timber.d( "callBack: $resource")

                                            when (resource.status) {
                                                Resource.Status.LOADING -> {
                                                    /*showProgress()*/
                                                }
                                                Resource.Status.SUCCESS -> {
//                                                    dismissProgress()

                                                    localPathGlobal = resource.data as String
                                                    Timber.d("localPathGlobal: $localPathGlobal")
                                                    Glide
                                                        .with(requireContext())
                                                        .load(resource.data as String)
                                                         .placeholder(R.drawable.profile_img)
                                                        .into(binding.imageViewProfile)


                                                }
                                                Resource.Status.ERROR -> {

                                                }
                                            }
                                        }
                                    }).execute<String>(localPath.toString())
                        }
                    })

            }
        }

    fun openImage() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)

        val photoFile = createImageFile()
        if (photoFile != null) {
            val uri: Uri? =
                context?.let {
                    FileProvider.getUriForFile(it, "com.example.chatapp.provider", photoFile)
                }

            mPhotoFile = photoFile;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        resultLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Timber.d("onDestroyView")
    }

    private fun uploadImage() {
        dialog = context?.let { Utils.showLoader(it, "Uploading...") }

            val fileReference =
                storageReference?.child("${System.currentTimeMillis()}.${getFileExtension(mPhotoFile?.toUri())}")
            uploadTask = File(localPathGlobal)?.toUri()?.let { fileReference?.putFile(it) }

            uploadTask?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                fileReference?.downloadUrl
            }?.addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val mUri = downloadUri.toString()

                    reference =
                        fuser?.uid?.let {
                            FirebaseDatabase.getInstance().getReference("Users").child(
                                it
                            )
                        }

                    val map = HashMap<String, Any>()
                    map["imageURL"] = mUri

                    reference?.updateChildren(map)
                } else {
                    context?.let { Utils.showMessage(it, "No image selected") }
                }

                dialog?.dismiss()
            }

    }

    private fun getFileExtension(uri: Uri?): String? {
        val contentResolver = context?.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(uri?.let { contentResolver?.getType(it) })
    }


    private fun getPermission() {
        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                openImage()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    context,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check();
    }

    @Throws(Exception::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val fileName = "JPG_${timeStamp}_"
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(fileName, ".jpg", storageDir)
        return file
    }

}