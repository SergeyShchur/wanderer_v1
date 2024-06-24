package com.example.wanderer_v1.bottomnav.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.wanderer_v1.LoginActivity
import com.example.wanderer_v1.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var uri: Uri
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        loadUserInfo()
        binding.profilePhoto.setOnClickListener {
            selectImage()
        }
        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        return binding.root
    }

    private fun loadUserInfo() {
        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser?.uid
            ?: "")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()

                    binding.usernameTv.text = username
                    if (profileImage.isNotEmpty()) {
                        Glide.with(requireContext()).load(profileImage).into(binding.profilePhoto)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun selectImage() {
        val intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            uri = result.data?.data!!
            binding.profilePhoto.setImageURI(uri)
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                binding.profilePhoto.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            uploadProfileImage()
        }
    }

    private fun uploadProfileImage() {
        if (uri!= null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            FirebaseStorage.getInstance().reference.child("image/" + userId).putFile(uri)
                .addOnSuccessListener { task ->
                    Toast.makeText(requireContext(), "Изображение загружено", Toast.LENGTH_SHORT).show()

                    FirebaseStorage.getInstance().reference.child("image/" + userId).getDownloadUrl()
                        .addOnSuccessListener { uri ->
                            FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser?.uid?: "")
                                .child("profileImage").setValue(uri.toString())
                        }
                }
            }
        }
}