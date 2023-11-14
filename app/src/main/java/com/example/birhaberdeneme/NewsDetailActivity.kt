package com.example.birhaberdeneme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.birhaberdeneme.databinding.ActivityNewsDetailBinding
import com.example.birhaberdeneme.databinding.FragmentUserNewsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityNewsDetailBinding
    val fireStore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // recyclerview de tıklanan haberin id si
        val newsId = intent.getStringExtra("newsId")
        val currentUserId = auth.uid.toString()
        // fire store dan bu habere ilişkin verileri çekelim
        if(newsId != null){
            val newsDocumentRef = fireStore.collection("News").document(newsId.toString())
            newsDocumentRef.get()
                .addOnSuccessListener {
                    documentSnapshot ->
                    if(documentSnapshot.exists()){
                        val news = documentSnapshot.toObject(NewsModule::class.java)
                        if(news != null){
                            val timeStampData = news.newsUploadDate?.toDate()
                            val formattedDate = formatTimestamp(timeStampData)
                            binding.tvTarih.text = formattedDate
                            binding.tvHaberMetin.text = news.newsText
                            binding.textViewHaberBaslik.text = news.newsTitle
                            binding.tvHaberYazar.text = news.uploadedById
                            Glide.with(this).load(news.newsImageUrl).into(binding.haberResim)

                            fireStore.collection("Users").document(currentUserId).get().addOnSuccessListener {
                                documentSnapshot ->
                                val user = documentSnapshot.toObject(UserModule::class.java)
                                if(user != null){
                                    if(user.favoriteNews!!.contains(newsId)){
                                        binding.btnFavori.setImageResource(R.drawable.favori_vector_kirmizi_32)
                                    }
                                    else{
                                        binding.btnFavori.setImageResource(R.drawable.favoriekle_vector_kirmizi_32)
                                    }
                                }

                            }
                                .addOnFailureListener{
                                    Toast.makeText(this,"User Ararken Hata : ${it.message}",Toast.LENGTH_SHORT).show()
                                }

                        }else{
                            Toast.makeText(this, "Haber bulunamadı", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener{
                    exception ->
                    Log.e("NewsDetailActivity", "Haber verilerini çekerken hata oluştu: $exception")
                    Toast.makeText(this, "Hata oluştu", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnGeri.setOnClickListener{
            finish()
        }

        binding.btnFavori.setOnClickListener{
            fireStore.collection("Users").document(currentUserId).get().addOnSuccessListener {
                    documentSnapshot ->
                if(documentSnapshot.exists()) {
                    val favoriteNews = documentSnapshot["favoriteNews"] as? ArrayList<String> ?: ArrayList()
                    if(favoriteNews.contains(newsId)) {
                        favoriteNews.remove(newsId)
                        binding.btnFavori.setImageResource(R.drawable.favoriekle_vector_kirmizi_32)
                    }
                    else{
                        if (newsId != null) {
                            favoriteNews.add(newsId)
                            binding.btnFavori.setImageResource(R.drawable.favori_vector_kirmizi_32)
                        }
                    }

                    fireStore.collection("Users").document(currentUserId).update("favoriteNews",favoriteNews).addOnSuccessListener {
                        Toast.makeText(this,"Başarıyla Favorilerinize Eklendi",Toast.LENGTH_SHORT).show()
                    }
                        .addOnFailureListener{
                            Toast.makeText(this,"Bir Hatayla Karşılaşıldı",Toast.LENGTH_SHORT).show()
                        }

                }

                }
                    .addOnFailureListener{
                        Toast.makeText(this,"User Ararken Hata : ${it.message}",Toast.LENGTH_SHORT).show()
                    }
            }

        }




    }
    private fun formatTimestamp(date: Date?): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(date ?: Date())
    }
