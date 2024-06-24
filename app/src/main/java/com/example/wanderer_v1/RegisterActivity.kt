package com.example.wanderer_v1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wanderer_v1.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

            binding.btnSignup.setOnClickListener(){
            if (binding.editEmail.text.toString().isEmpty() || binding.editUsername.text.toString().isEmpty()
                || binding.editPass.text.toString().isEmpty()) {
                Toast.makeText(applicationContext, "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.editEmail.text.toString(), binding.editPass.text.toString())
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult?> {
                        override fun onComplete(task: Task<AuthResult?>) {
                            if (task.isSuccessful) {
                                val userInfo: HashMap<String,String> = HashMap()
                                userInfo.put("email", binding.editEmail.text.toString())
                                userInfo.put("username", binding.editUsername.text.toString())
                                userInfo.put("profileImage", "")
                                FirebaseDatabase.getInstance().reference.child("Users").child(
                                    FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                ).setValue(userInfo)
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            }
                        }
                    })
            }
        }

        binding.backFromRegist.setOnClickListener(){
            startActivity(Intent(binding.root.context, LoginActivity::class.java))
        }
    }
}