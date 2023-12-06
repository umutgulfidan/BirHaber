package com.example.birhaberdeneme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
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
        val ivFavoriEkle : ImageView = this.findViewById<ImageView>(R.id.btnFavori)
        val adminMi = checkIfAdmin { isAdmin ->
            if(isAdmin)
                binding.btnFavori.visibility = View.GONE
        }

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

                            if(!news.newsImageUrl.isNullOrEmpty()){

                                val storageRef = FirebaseStorage.getInstance().reference.child("NewsPictures")
                                    .child(news.newsId).child("news_image.jpg")

                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val imageUrl = uri.toString()
                                    Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.haberler_vector_24) // Burada default_image, drawable klasöründe bulunan varsayılan görselinizdir
                                        .error(R.drawable.hata_vector_kirmizi_32)// Eğer bir hata olursa gösterilecek görsel
                                        .into(binding.haberResim)
                                }.addOnFailureListener {
                                    // Eğer resim yüklenirken bir hata olursa ne yapılacağı burada tanımlanabilir
                                    // Örneğin: Toast mesajı gösterilebilir
                                    Toast.makeText(this, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                val storageRef = FirebaseStorage.getInstance().reference.child("NewsPictures").child("default_picture.jpg")
                                storageRef.downloadUrl.addOnSuccessListener { uri->
                                    val defaultUrl = uri.toString()
                                    Glide.with(this).load(defaultUrl)
                                        .placeholder(R.drawable.haberler_vector_24)
                                        .error(R.drawable.hata_vector_kirmizi_32)
                                        .into(binding.haberResim)
                                }
                                    .addOnFailureListener{
                                        Toast.makeText(this, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                                    }

                            }


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
    private fun checkIfAdmin(callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val user = FirebaseFirestore.getInstance().collection("Users")
                .document(currentUser.uid).get().addOnSuccessListener {
                    val userRole = it.get("role") as String
                    callback.invoke(userRole == "Admin")
                }.addOnFailureListener {
                    callback.invoke(false) // Hata durumunda admin değil olarak işaretleyebiliriz
                }
        } else {
            callback.invoke(false) // Kullanıcı yoksa admin değil olarak işaretleyebiliriz
        }
    }




    }
    private fun formatTimestamp(date: Date?): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date ?: Date())
    }
