package com.example.birhaberdeneme

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

    public fun updateNewsList(newsList:List<NewsModule>){
        this.newsList = newsList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsManagementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_newsmanagement,parent,false)
        return NewsManagementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  newsList.size
    }

    override fun onBindViewHolder(holder: NewsManagementViewHolder, position: Int) {
        val new = newsList[position]
        holder.bind(new,this)
    }

    class NewsManagementViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        // ViewHolder içindeki view'ları tanımla
        private val ivHaberResim : ImageView = itemView.findViewById(R.id.ivNewsMangementHaberResim)
        private val tvHaberBaslik : TextView = itemView.findViewById(R.id.tvNewsMangementHaberBaslik)
        private val tvHaberAciklama : TextView = itemView.findViewById(R.id.tvNewsMangementHaberAciklama)
        private val tvYukleyenEmail : TextView = itemView.findViewById(R.id.tvNewsMangementHaberBaslikYukleyenEposta)
        private val btnHaberSil : Button = itemView.findViewById(R.id.btnHaberiSil)



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
                FirebaseFirestore.getInstance().collection("News").document(new.newsId)
                    .delete().addOnSuccessListener {
                        // Silme işlemi başarılı olduğunda listeden öğeyi kaldır
                        val position = adapterPosition
                        adapter.newsList = adapter.newsList .filterIndexed { index, _ -> index != position }
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(position, adapter.newsList.size)
                        Toast.makeText(itemView.context,"Haber Başarıyla Silindi",Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Toast.makeText(itemView.context,"Hata : ${it.message}",Toast.LENGTH_SHORT).show()
                    }
            }

            if(!new.newsImageUrl.isNullOrEmpty()){
                val storageRef = FirebaseStorage.getInstance().reference.child("NewsPictures").child(new.newsId).child("image.jpg")
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