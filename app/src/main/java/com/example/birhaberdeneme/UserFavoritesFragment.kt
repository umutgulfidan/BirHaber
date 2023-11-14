package com.example.birhaberdeneme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birhaberdeneme.databinding.FragmentUserFavoritesBinding
import com.example.birhaberdeneme.databinding.FragmentUserNewsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFavoritesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val view = inflater.inflate(R.layout.fragment_user_favorites, container, false)

        // RecyclerView ve adapter'ı tanımla
        recyclerView = view.findViewById(R.id.recyclerView)
        newsAdapter = NewsAdapter()

        // RecyclerView'a adapter'ı set et
        recyclerView.adapter = newsAdapter

        // LinearLayoutManager'ı kullanarak dikey düzeni ayarla
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        // Kullanıcının UID'sini al
        val userId = auth.currentUser?.uid

        // Firestore'dan favori haberleri çek
        userId?.let { fetchFavoriteNews(it) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun fetchFavoriteNews(userId: String) {
        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Favori haberlerin ID'lerini al
                    val favoriteNewsIds = document["favoriteNews"] as? List<String>

                    // Favori haberlerin içeriğini getir
                    favoriteNewsIds?.let { fetchNewsDetails(it) }
                }
            }
            .addOnFailureListener { exception ->
                // Hata durumunda uygulamanın nasıl davranacağını belirle
                Toast.makeText(context,"${exception.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchNewsDetails(favoriteNewsIds: List<String>) {
        if(favoriteNewsIds.isNotEmpty()){
            db.collection("News")
                .whereIn("newsId", favoriteNewsIds)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // querySnapshot içinde favori haberlerin detayları bulunmaktadır
                    val favoriteNewsList = mutableListOf<NewsModule>()

                    for (document in querySnapshot.documents) {
                        // Firestore'dan dönen her bir dökümanı NewsModule objesine çevir ve listeye ekle
                        val news = document.toObject(NewsModule::class.java)
                        news?.let { favoriteNewsList.add(it) }
                    }

                    // RecyclerView için adapter'ı oluştur ve verileri set et
                    newsAdapter.updateNewList(favoriteNewsList)
                }
                .addOnFailureListener { exception ->
                    // Hata durumunda uygulamanın nasıl davranacağını belirle
                    Toast.makeText(context,"${exception.message}",Toast.LENGTH_SHORT).show()
                }
        }
        else{
            Toast.makeText(context,"FAVORİ HABER EKLEMEDİNİZ",Toast.LENGTH_SHORT).show()
        }


}


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFavoritesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFavoritesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}