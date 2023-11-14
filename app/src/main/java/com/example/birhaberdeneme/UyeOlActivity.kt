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
                auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
                    task ->

                    val userId = task.user?.uid.toString()
                    val newUser = hashMapOf(
                        "email" to email,
                        "password" to password,
                        "id" to userId,
                        "registerDate" to Timestamp.now(),
                        "profilePictureUrl" to "https://firebasestorage.googleapis.com/v0/b/birhaberuygulama.appspot.com/o/ProfilPictures%2Fdefault_picture.jpg?alt=media&token=87cb3f46-ca79-4c93-a23c-381cad108c71",
                        "active" to true,
                        "role" to "user",
                        "favoriteNews" to ArrayList<String>(),
                        "okunanHaberSayisi" to 0
                    )
                    database.collection("Users").document(userId).set(newUser)

                        .addOnSuccessListener {
                            Toast.makeText(this,"Firebase Cloud a kaydedildi",Toast.LENGTH_SHORT).show()

                            Toast.makeText(this,"Başarıyla Kayıt Oldunuz Giriş Sayfasına Yönlendiriliyorsunuz",Toast.LENGTH_LONG).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(Intent(this@UyeOlActivity,MainActivity::class.java))
                                finish()
                            },4000)

                        }
                        .addOnFailureListener{
                            Toast.makeText(this,"Firebase Cloud a kaydedilemedi\n ${it.message}",Toast.LENGTH_SHORT).show()
                        }

                }
                    .addOnFailureListener{
                        Toast.makeText(this,"Hata: ${it.message}",Toast.LENGTH_LONG).show()
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