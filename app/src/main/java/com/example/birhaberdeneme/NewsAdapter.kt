package com.example.birhaberdeneme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>()
{
    public var newsList : List<NewsModule> = emptyList()
    private var mListener: onItemClickListener = object : onItemClickListener {
        override fun onItemClick(position: Int) {
            // Buraya gerekirse onItemClick için bir kod ekleyebilirsiniz.
        }
    }
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener){
        mListener= listener
    }

    fun updateNewList(newList: List<NewsModule>){
        newsList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: NewsAdapter.NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.bind(news)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }


    class NewsViewHolder(itemView: View,listener:onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvHaberBaslik)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvHaberAciklama)
        private val newsImageView : ImageView = itemView.findViewById(R.id.ivHaberResim)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

        fun bind(news: NewsModule) {
            titleTextView.text = news.newsTitle
            descriptionTextView.text = news.newsShortDescription

            if(!news.newsImageUrl.isNullOrEmpty()){
                val storageRef = FirebaseStorage.getInstance().reference.child("NewsPictures").child(news.newsId).child("image.jpg")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Glide.with(itemView.context)
                        .load(imageUrl) // Burada default_image, drawable klasöründe bulunan varsayılan görselinizdir
                        .error(R.drawable.hata_vector_kirmizi_32)// Eğer bir hata olursa gösterilecek görsel
                        .into(newsImageView)
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
                        .into(newsImageView)
                }
                    .addOnFailureListener{
                        Toast.makeText(itemView.context, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                    }

            }

        }
    }
}