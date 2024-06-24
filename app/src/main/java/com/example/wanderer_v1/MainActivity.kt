package com.example.wanderer_v1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.wanderer_v1.bottomnav.newentry.NewEntryFragment
import com.example.wanderer_v1.bottomnav.profile.ProfileFragment
import com.example.wanderer_v1.bottomnav.tape.TapeFragment
import com.example.wanderer_v1.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val fragmentMap: HashMap<Int, Fragment> = hashMapOf(
        R.id.tape to TapeFragment(),
        R.id.new_entry to NewEntryFragment(),
        R.id.profile to ProfileFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
            return // Для предотвращения дальнейшего выполнения кода
        }

        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            fragmentMap[item.itemId]?.let { fragment ->
                supportFragmentManager.beginTransaction()
                    .replace(binding.conteiner.id, fragment)
                    .commit()
                true
            } ?: false
        }

        // По умолчанию выбираем первый фрагмент
        binding.bottomNav.selectedItemId = R.id.tape
    }
}