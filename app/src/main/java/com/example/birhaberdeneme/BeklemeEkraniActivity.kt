package com.example.birhaberdeneme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class BeklemeEkraniActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bekleme_ekrani)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@BeklemeEkraniActivity,MainActivity::class.java))
            finish()
        },3000)

    }
}