package com.example.birhaberdeneme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.birhaberdeneme.databinding.ActivityAdminAnaSayfaBinding

class AdminAnaSayfaActivity : AppCompatActivity() {
    lateinit var binding : ActivityAdminAnaSayfaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAdminAnaSayfaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(UserNewsFragment())

        binding.bottomNavigationView2.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.anasayfa -> replaceFragment(UserNewsFragment())
                R.id.haberEkle -> replaceFragment(AdminHaberEkleFragment())
                R.id.haberleriYonet -> replaceFragment(AdminHaberleriYonetFragment())
                R.id.kullanicilariYonet -> replaceFragment(AdminKullanicilariYonetFragment())
                else -> {Toast.makeText(this,"İlgili Fragment Bulunamadı",Toast.LENGTH_SHORT).show()}
            }
            true
        }

    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayoutAdmin,fragment)
        fragmentTransaction.commit()
    }
}