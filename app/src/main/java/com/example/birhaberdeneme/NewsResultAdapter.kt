package com.example.birhaberdeneme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsResultAdapter : RecyclerView.Adapter<NewsResultAdapter.NewsResultViewHolder>() {
    public var newsResultList : List<Article> = emptyList()
    private var mListener: onItemClickListener = object :
        onItemClickListener {
        override fun onItemClick(position: Int) {
            // Buraya gerekirse onItemClick için bir kod ekleyebilirsiniz.
        }
    }

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener:onItemClickListener){
        mListener= listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsResultViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_newsresult, parent, false)
        return NewsResultViewHolder(itemView,mListener)
    }

    override fun getItemCount(): Int {
        return  newsResultList.size
    }

    override fun onBindViewHolder(holder: NewsResultViewHolder, position: Int) {
        val currentHaber = newsResultList[position]
        holder.bind(currentHaber)
    }

    class NewsResultViewHolder(itemView: View,listener:onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
        private val titleTextView: TextView = itemView.findViewById(R.id.tvHaberBaslik2)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvHaberAciklama2)
        private val newsImageView : ImageView = itemView.findViewById(R.id.ivHaberResim22)
        private val kaynakTextView : TextView = itemView.findViewById(R.id.tvKaynak)
        private val yazarTextView : TextView = itemView.findViewById(R.id.tvYazar)
        private val tarihTextView : TextView = itemView.findViewById(R.id.tvNewsResultTarih)
        fun bind(newsResultArticle: Article){
            titleTextView.text = newsResultArticle.title
            descriptionTextView.text = newsResultArticle.description
            kaynakTextView.text = newsResultArticle.source.name.toString()
            yazarTextView.text = newsResultArticle.author
            tarihTextView.text = newsResultArticle.publishedAt
            Glide.with(itemView.context).load(newsResultArticle.urlToImage)
                .placeholder(R.drawable.haberler_vector_24)
                .into(newsImageView)
            println(newsResultArticle.urlToImage)
            println(newsImageView)
        }
    }
}