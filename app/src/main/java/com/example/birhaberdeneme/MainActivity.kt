package com.example.birhaberdeneme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        binding.btnGirisYap.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val sifre = binding.etSifre.text.toString()
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

                                    }
                                }
                            }
                        }
                    }
                }

            }
        }






    }
}