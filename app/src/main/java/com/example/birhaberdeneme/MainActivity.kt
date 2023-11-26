package com.example.birhaberdeneme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.birhaberdeneme.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val database =Firebase.firestore


        binding.twUyeOl.setOnClickListener{
            val intent = Intent(this,UyeOlActivity::class.java)
            startActivity(intent)
        }

        binding.twSifremiUnuttum.setOnClickListener{
            // ALERT DİALOG OLUŞTURMA
            var activity_sifremiunuttum = layoutInflater.inflate(R.layout.activity_sifremiunuttum,null)
            val  alertDialog = AlertDialog.Builder(this)
            alertDialog.setView(activity_sifremiunuttum)
            alertDialog.setNegativeButton("Kapat"){dialog,which -> }

            // epostayı alalım ve şifre sıfırlama yapalım

            val button = activity_sifremiunuttum.findViewById<Button>(R.id.btnSifreGonder)
            button.setOnClickListener{
                val tvEposta = activity_sifremiunuttum.findViewById<EditText>(R.id.editTextTextEmailAddress)
                tvEposta.error = null
                val eposta = tvEposta.text.toString()
                if(eposta.isNotEmpty()){
                    auth.sendPasswordResetEmail(eposta).addOnSuccessListener {
                        Toast.makeText(applicationContext,"E Postanızı Kontrol Ediniz",Toast.LENGTH_SHORT).show()
                    }
                        .addOnFailureListener{
                            Toast.makeText(applicationContext,"Hata : ${it.message}",Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    tvEposta.error = "Eposta Boş Geçilemez"
                    Toast.makeText(applicationContext,"Eposta Boş olamaz",Toast.LENGTH_SHORT).show()
                }


            }


            alertDialog.show()
        }

        binding.btnGirisYap.setOnClickListener{


            val email = binding.etEmail.text.toString()
            val sifre = binding.etSifre.text.toString()
            if(email.isEmpty()){
                binding.etEmail.error = "BOŞ GEÇİLEMEZ"
            }
            else{
                binding.etEmail.error = null
            }
            if(sifre.isEmpty()){
                binding.etSifre.error = "BOŞ GEÇİLEMEZ"
            }else{
                binding.etSifre.error = null
            }

            if(email.isNotEmpty() && sifre.isNotEmpty()){
                auth.signInWithEmailAndPassword(email,sifre).addOnSuccessListener {
                        task ->
                    val userId = task.user?.uid.toString()
                    val userRef = database.collection("Users").document(userId)
                    if(userId != null){
                        userRef.get().addOnSuccessListener {
                                documentSnapshot ->
                            if(documentSnapshot.exists()){
                                val userData =documentSnapshot.data
                                if(userData != null){
                                    val userRole = userData["role"] as String
                                    when(userRole){
                                        "user" -> {
                                            val intent = Intent(this@MainActivity,KullaniciAnaSayfaActivity::class.java)
                                            startActivity(intent)

                                        }
                                        "admin" ->{
                                            
                                        }


                                        else ->{
                                            Toast.makeText(this,"İlgili role elişkin activity tanımlanmamış",Toast.LENGTH_SHORT).show()
                                        }


                                    }
                                }
                                else{
                                    Toast.makeText(this,"Data Boş Döndü",Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                Toast.makeText(this,"Kullanici Bulunamadi",Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                    else{
                        Toast.makeText(this,"User Id Boş Döndü",Toast.LENGTH_SHORT).show()
                    }

                }
                    .addOnFailureListener{
                        Toast.makeText(this,"Hata ${it.message}",Toast.LENGTH_SHORT).show()
                    }
            }


        }






    }
}