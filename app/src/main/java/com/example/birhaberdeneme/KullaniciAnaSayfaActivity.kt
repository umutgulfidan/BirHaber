package com.example.birhaberdeneme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.example.birhaberdeneme.databinding.ActivityKullaniciAnaSayfaBinding

class KullaniciAnaSayfaActivity : AppCompatActivity() {
    lateinit var binding: ActivityKullaniciAnaSayfaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKullaniciAnaSayfaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(UserNewsFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.news -> replaceFragment(UserNewsFragment())
                R.id.favorites -> replaceFragment(UserFavoritesFragment())
                R.id.accountSettings -> replaceFragment(UserAccountSettingsFragment())

                else -> {
                    Toast.makeText(this,"İlgili Fragment Id si bulunamadı",Toast.LENGTH_SHORT).show()
                }

            }

            true
        }

    }

    private fun replaceFragment(fragment:Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}