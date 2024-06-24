package com.example.wanderer_v1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.wanderer_v1.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener(){
            if (binding.editEmail.text.toString().isEmpty() || binding.editPass.text.toString().isEmpty()) {
                Toast.makeText(applicationContext, "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.editEmail.text.toString(), binding.editPass.text.toString())
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult?> {
                        override fun onComplete(task: Task<AuthResult?>) {
                            if (task.isSuccessful) {
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            }
                        }
                    })
            }
        }
        binding.toRegistActivity.setOnClickListener(){
            startActivity(Intent(binding.root.context, RegisterActivity::class.java))
        }
    }
}