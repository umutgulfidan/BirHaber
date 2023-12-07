package com.example.birhaberdeneme

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NewsManagementAdapter :
    RecyclerView.Adapter<NewsManagementAdapter.NewsManagementViewHolder>() {
    var newsList : List<NewsModule> = emptyList()
    private var mListener: NewsManagementAdapter.onItemClickListener = object :
        NewsManagementAdapter.onItemClickListener {
        override fun onItemClick(position: Int) {
            // Buraya gerekirse onItemClick için bir kod ekleyebilirsiniz.
        }
    }
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnClickListener(listener: NewsManagementAdapter.onItemClickListener){
        mListener= listener
    }

    public fun updateNewsList(newsList:List<NewsModule>){
        this.newsList = newsList
        notifyDataSetChanged()
    }
    public fun filter(text:String){
        val filteredList = newsList.filter { news ->
            news.newsTitle?.contains(text, ignoreCase = true) == true
        }
        updateNewsList(filteredList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsManagementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_newsmanagement,parent,false)
        return NewsManagementViewHolder(view,mListener)
    }

    override fun getItemCount(): Int {
        return  newsList.size
    }

    override fun onBindViewHolder(holder: NewsManagementViewHolder, position: Int) {
        val new = newsList[position]
        holder.bind(new,this)
    }

    class NewsManagementViewHolder(itemView: View,listener:onItemClickListener):RecyclerView.ViewHolder(itemView){
        // ViewHolder içindeki view'ları tanımla
        private val ivHaberResim : ImageView = itemView.findViewById(R.id.ivNewsMangementHaberResim)
        private val tvHaberBaslik : TextView = itemView.findViewById(R.id.tvNewsMangementHaberBaslik)
        private val tvHaberAciklama : TextView = itemView.findViewById(R.id.tvNewsMangementHaberAciklama)
        private val tvYukleyenEmail : TextView = itemView.findViewById(R.id.tvNewsMangementHaberBaslikYukleyenEposta)
        private val btnHaberSil : Button = itemView.findViewById(R.id.btnHaberiSil)

        init {
            itemView.findViewById<Button>(R.id.btnHaberDuzenle).setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }



        fun bind(new:NewsModule,adapter:NewsManagementAdapter){
            tvHaberBaslik.text = new.newsTitle
            tvHaberAciklama.text = new.newsShortDescription

            new.uploadedById?.let {
                FirebaseFirestore.getInstance().collection("Users").document(it)
                    .get().addOnSuccessListener {
                        val email = it.getString("email")
                        tvYukleyenEmail.text = email
                    }
            }

            btnHaberSil.setOnClickListener{
                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("Emin misiniz?")
                builder.setMessage("Bu haber kalıcı olarak silinecek")
                builder.setPositiveButton("Eminim"){
                        dialog,which ->
                    FirebaseFirestore.getInstance().collection("News").document(new.newsId)
                        .delete().addOnSuccessListener {
                            // Silme işlemi başarılı olduğunda listeden öğeyi kaldır
                            val position = adapterPosition
                            val deletedNewsId = adapter.newsList[position].newsId
                            adapter.newsList = adapter.newsList .filterIndexed { index, _ -> index != position }
                            adapter.notifyItemRemoved(position)
                            adapter.notifyItemRangeChanged(position, adapter.newsList.size)
                            Toast.makeText(itemView.context,"Haber Başarıyla Silindi",Toast.LENGTH_SHORT).show()
                            // Silinen haberin ID'sini favori haberler listesinden kaldırmak için işlemi gerçekleştir
                            val userCollection = FirebaseFirestore.getInstance().collection("Users")
                            userCollection.get().addOnSuccessListener {querySnapshot ->
                                for (document in querySnapshot){
                                    val userId = document.id
                                    val userRef = userCollection.document(userId)

                                    userRef.get().addOnSuccessListener {
                                        val favoriteNewsList = it.get("favoriteNews") as? ArrayList<String>

                                        if(favoriteNewsList != null && favoriteNewsList.contains(deletedNewsId)){
                                            favoriteNewsList.remove(deletedNewsId)
                                            userRef.update("favoriteNews",favoriteNewsList).addOnSuccessListener {
                                                println("Kullanıcı $userId favori haberler listesinden silindi: $deletedNewsId")
                                            }
                                                .addOnFailureListener { e ->
                                                    println("Hata: Kullanıcı $userId favori haberler listesini güncellerken hata oluştu: ${e.message}")
                                                }
                                        }
                                    }
                                }
                            }


                        }
                        .addOnFailureListener{
                            Toast.makeText(itemView.context,"Hata : ${it.message}",Toast.LENGTH_SHORT).show()
                        }
                    // Haberin Resmini storage dan silme
                    FirebaseStorage.getInstance().reference.child("NewsPictures/${new.newsId}/news_image.jpg").delete().addOnSuccessListener {
                        Toast.makeText(itemView.context,"Eski Haber ilişkin resim veri tabanından silindi",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener{
                        Toast.makeText(itemView.context,"Hata ${it.message}",Toast.LENGTH_SHORT)
                    }
                }
                builder.setNegativeButton("İptal"){
                        dialog,which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()

            }

            if(!new.newsImageUrl.isNullOrEmpty()){
                val storageRef = FirebaseStorage.getInstance().reference.child("NewsPictures").child(new.newsId).child("news_image.jpg")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.haberler_vector_24) // Burada default_image, drawable klasöründe bulunan varsayılan görselinizdir
                        .error(R.drawable.hata_vector_kirmizi_32)// Eğer bir hata olursa gösterilecek görsel
                        .into(ivHaberResim)
                }.addOnFailureListener {
                    // Eğer resim yüklenirken bir hata olursa ne yapılacağı burada tanımlanabilir
                    // Örneğin: Toast mesajı gösterilebilir
                    Toast.makeText(itemView.context, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                val storageRef = FirebaseStorage.getInstance().reference.child("NewsPictures").child("default_picture.jpg")
                storageRef.downloadUrl.addOnSuccessListener { uri->
                    val defaultUrl = uri.toString()
                    Glide.with(itemView.context).load(defaultUrl)
                        .placeholder(R.drawable.haberler_vector_24)
                        .error(R.drawable.hata_vector_kirmizi_32)
                        .into(ivHaberResim)
                }
                    .addOnFailureListener{
                        Toast.makeText(itemView.context, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                    }

            }

        }

    }
}