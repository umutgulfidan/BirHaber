package com.example.birhaberdeneme

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.birhaberdeneme.databinding.ActivityHaberDuzenleBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class HaberDuzenleActivity : AppCompatActivity() {
    lateinit var binding : ActivityHaberDuzenleBinding
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storage =FirebaseStorage.getInstance()
    private lateinit var currentNewsId: String

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode== Activity.RESULT_OK){
            val selectedImageUrl : Uri? = result.data?.data
            selectedImageUrl?.let {
                uploadImageToFireBaseStroge(it)
            }
        }
        else{
            Toast.makeText(this,"${result.data},${result.resultCode}",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHaberDuzenleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Duzenlenecek Haberin Id sini al ve arayuzu guncelle
        currentNewsId = intent.getStringExtra("newsId").toString()
        println(currentNewsId)
        if(currentNewsId != null){
            val newsDocumentRef = firestore.collection("News").document(currentNewsId)
            getNews(newsDocumentRef){
                news ->
                arayuzGuncelle(news)
            }
        }

        // Butona tıklandığında resmi veri tabanında güncelle

        binding.btnHaberKaydet.setOnClickListener{
            if(currentNewsId != null){
                val newsDocumentRef = firestore.collection("News").document(currentNewsId)
                val updates = hashMapOf<String,Any>(
                    "newsTitle" to binding.etHaberBaslikHaberDuzenle.text.toString().trim(),
                    "newsShortDescription" to binding.etKisaAciklamaHaberDuzenle.text.toString().trim(),
                    "newsText" to binding.etAnaMetinHaberDuzenle.text.toString().trim(),
                    "newsUploadDate" to Timestamp.now(),
                )
                newsDocumentRef.update(updates).addOnSuccessListener {
                    Toast.makeText(this,"Başarıyla Düzenlendi",Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnHaberResimSec.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Emin misiniz?")
            builder.setMessage("Bir resim seçerseniz veri tabanında otomatik olarak güncellenecek")
            builder.setPositiveButton("Eminim"){
                dialog,which ->
                openGallery()
            }
            builder.setNegativeButton("İptal"){
                dialog,which ->
                dialog.cancel()
            }
            val dialog = builder.create()
            dialog.show()

        }
    }

    // getNews fonksiyonuna bir callback ekleyerek arayüz güncellemesini gerçekleştirin
    fun getNews(newsRef: DocumentReference, callback: (NewsModule?) -> Unit){
        newsRef.get().addOnSuccessListener {
            if(it.exists()){
                val newsModule = it.toObject(NewsModule::class.java)
                callback(newsModule)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            // Hata durumunda null gönderin veya hata işlemlerini gerçekleştirin
            callback(null)
        }
    }
    fun arayuzGuncelle(news: NewsModule?):Unit{
        if(news != null){
            binding.etHaberBaslikHaberDuzenle.setText(news.newsTitle)
            binding.etKisaAciklamaHaberDuzenle.setText(news.newsShortDescription)
            binding.etAnaMetinHaberDuzenle.setText(news.newsText)
            binding.haberId.text = news.newsId
            haberResmiDuzenle(news)
        }
    }
    fun haberResmiDuzenle(news:NewsModule){
        if(!news.newsImageUrl.isNullOrEmpty()){

            val storageRef = FirebaseStorage.getInstance().reference.child("NewsPictures")
                .child(news.newsId).child("news_image.jpg")

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.haberler_vector_24) // Burada default_image, drawable klasöründe bulunan varsayılan görselinizdir
                    .error(R.drawable.hata_vector_kirmizi_32)// Eğer bir hata olursa gösterilecek görsel
                    .into(binding.ivHaberResmi)
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
                    .into(binding.ivHaberResmi)
            }
                .addOnFailureListener{
                    Toast.makeText(this, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            getContent.launch(galleryIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Dosya seçme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFireBaseStroge(imageUri: Uri) {
        val strogeRef = FirebaseStorage.getInstance().reference
        val imageRef = strogeRef.child("NewsPictures/${currentNewsId}/news_image.jpg")
        binding.progressBar2.visibility = View.VISIBLE
        binding.btnHaberResimSec.visibility = View.GONE

        imageRef.putFile(imageUri).addOnSuccessListener{
            //Resim yükleme başarılı oldu
            imageRef.downloadUrl.addOnSuccessListener {
                //Resim URL SİNİ AL VE FİRESTORE DA GÜNCELLE
                updateNewsImageUrl(imageUri.toString())
                binding.progressBar2.visibility = View.GONE
                binding.btnHaberResimSec.visibility = View.VISIBLE

            }
        }.addOnFailureListener{
            it.printStackTrace()
            Log.e("FirestoreError","Belge Alınamadı : ${it.message}")
        }

    }

    private fun updateNewsImageUrl(imageUri: String) {
        firestore.collection("News").document(currentNewsId).update("newsImageUrl",imageUri).addOnSuccessListener {
            Toast.makeText(this,"Başarıyla Haber Resmi Verisi Güncellendi",Toast.LENGTH_SHORT).show()
            getNews(firestore.collection("News").document(currentNewsId)){
                if (it != null) {
                    haberResmiDuzenle(it)
                }
            }
        }
            .addOnFailureListener{
                Toast.makeText(this,"Haber Urlsi Firestore'a Aktarilamadi, Haber Default Resimle gösterilecektir",Toast.LENGTH_SHORT).show()
                it.printStackTrace()
            }

    }

}