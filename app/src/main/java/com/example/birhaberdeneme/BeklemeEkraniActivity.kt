package com.example.birhaberdeneme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BeklemeEkraniActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bekleme_ekrani)

        Handler(Looper.getMainLooper()).postDelayed({
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            // Kullanıcı giriş yapmışsa, ana ekrana yönlendir
            if (auth.currentUser != null) {
                val userId = auth.currentUser?.uid
                userId.let {
                    if (userId != null) {
                        firestore.collection("Users").document(userId).get().addOnSuccessListener {
                            val userRole = it.get("role") as String
                            val userActive = it.get("active") as Boolean
                            when {
                                userRole == "User" && userActive -> {
                                    val intent = Intent(this, KullaniciAnaSayfaActivity::class.java)
                                    startActivity(intent)

                                }

                                userRole == "Admin" && userActive -> {
                                    val intent = Intent(this, AdminAnaSayfaActivity::class.java)
                                    startActivity(intent)
                                }

                                else -> {
                                    auth.signOut()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }

                            }
                        }
                    }
                }
            }
            else
            {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        },3000)




    }

}