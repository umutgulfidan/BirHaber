package com.example.birhaberdeneme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.birhaberdeneme.databinding.ActivityUyeOlBinding
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class UyeOlActivity : AppCompatActivity() {
    lateinit var binding: ActivityUyeOlBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding = ActivityUyeOlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val database = Firebase.firestore

        binding.btnGeriDon.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        binding.btnUyeOl.setOnClickListener{
            if(binding.etSifre.text.toString() == binding.etSifreTekrar.text.toString()){

                binding.etSifre.error = null
                binding.etSifreTekrar.error = null

                val email = binding.etEmail.text.toString().trim()
                val password = binding.etSifre.text.toString().trim()
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                    task ->
                    Toast.makeText(this,"Autha Kayıt Edildi",Toast.LENGTH_SHORT).show()
                    val userId = task.result.user?.uid.toString()
                    val newUser = hashMapOf(
                        "email" to email,
                        "password" to password,
                        "id" to userId,
                        "registerDate" to Timestamp.now(),
                        "active" to true,
                        "favoriteNews" to ArrayList<String>()
                    )
                    database.collection("Users")
                        .add(newUser)
                        .addOnSuccessListener {
                            Toast.makeText(this,"Firebase Cloud a kaydedildi",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            Toast.makeText(this,"Firebase Cloud a kaydedilemedi",Toast.LENGTH_SHORT).show()
                        }

                    Toast.makeText(this,"Başarıyla Kayıt Oldunuz Giriş Sayfasına Yönlendiriliyorsunuz",Toast.LENGTH_LONG).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@UyeOlActivity,MainActivity::class.java))
                        finish()
                    },4000)

                }
            }
            else{
                Toast.makeText(this,"Şifreler Eşleşmedi",Toast.LENGTH_SHORT)
                val mesaj = getString(R.string.sifreEslesmedi)
                binding.etSifre.error = mesaj
                binding.etSifreTekrar.error = mesaj
            }

            }

    }
}