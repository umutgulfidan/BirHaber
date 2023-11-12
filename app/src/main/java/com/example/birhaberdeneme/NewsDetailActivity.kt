package com.example.birhaberdeneme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.birhaberdeneme.databinding.ActivityNewsDetailBinding
import com.example.birhaberdeneme.databinding.FragmentUserNewsBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // recyclerview de tıklanan haberin id si
        val newsId = intent.getStringExtra("newsId")
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



    }
    private fun formatTimestamp(date: Date?): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(date ?: Date())
    }
}