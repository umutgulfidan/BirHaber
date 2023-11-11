package com.example.birhaberdeneme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>()
{
    private var newsList : List<NewsModule> = emptyList()

    fun updateNewList(newList: List<NewsModule>){
        newsList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsAdapter.NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.bind(news)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }


    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvHaberBaslik)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvHaberAciklama)
        private val newsImageView : ImageView = itemView.findViewById(R.id.ivHaberResim)

        fun bind(news: NewsModule) {
            titleTextView.text = news.newsTitle
            descriptionTextView.text = news.newsShortDescription
            Glide.with(itemView.context).load(news.newsImageUrl)
                .into(newsImageView)
        }
    }
}