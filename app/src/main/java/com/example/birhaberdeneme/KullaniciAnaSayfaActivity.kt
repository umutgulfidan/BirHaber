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
        replaceFregmant(UserNewsFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.news -> replaceFregmant(UserNewsFragment())
                R.id.favorites -> replaceFregmant(UserFavoritesFragment())
                R.id.accountSettings -> replaceFregmant(UserAccountSettingsFragment())

                else -> {
                    Toast.makeText(this,"İlgili Fragment Id si bulunamadı",Toast.LENGTH_SHORT)
                }

            }

            true
        }

    }

    private fun replaceFregmant(fragment:Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}