package com.example.birhaberdeneme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birhaberdeneme.databinding.FragmentUserNewsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserNewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserNewsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentUserNewsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val fireStore =FirebaseFirestore.getInstance()
    private val newsCollection = fireStore.collection("News")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_news, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        newsAdapter = NewsAdapter()
        newsAdapter.setOnClickListener(object : NewsAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Toast.makeText(context,"You Clicked on item no ${position}",Toast.LENGTH_SHORT).show()
                val clickedNews = newsAdapter.newsList[position]
                val clickedNewsId = clickedNews.newsId
                val intent = Intent(context,NewsDetailActivity::class.java)
                intent.putExtra("newsId",clickedNewsId)
                startActivity(intent)
            }

        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = newsAdapter

        loadNewsData()

        return view
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_news, container, false)
    }

    private fun loadNewsData() {
        // Firebase Firestore'dan haber verilerini çekme
        newsCollection.orderBy("newsUploadDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val newsList = mutableListOf<NewsModule>()
                for (document in result.documents) {
                    val news = document.toObject(NewsModule::class.java)
                    news?.let {
                        newsList.add(it)
                    }
                }
                newsAdapter.updateNewList(newsList)
            }
            .addOnFailureListener { exception ->
                // Hata durumunda işlemler
                 Log.e("NewsFragment", "Haber verilerini çekerken hata oluştu: $exception")
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserNewsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserNewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}