package com.example.birhaberdeneme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birhaberdeneme.databinding.FragmentNewsResultBinding
import com.example.birhaberdeneme.databinding.FragmentUserNewsBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewsResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewsResultFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding : FragmentNewsResultBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsResultAdapter: NewsResultAdapter
    private var currentKeywords: String? = null
    private var currentCountryCode: String = "tr"
    private var currentLanguage: String = "tr"

    private fun getNews(countryCode : String, pageNumber: Int): Deferred<List<Article>> = GlobalScope.async {
        try {
            val response = RetrofitInstance.newsApi.getAll(countryCode,pageNumber)
            val articleList = response.body()?.articles ?: emptyList()
            Log.i("Api  Result :",articleList.toString())
            articleList
        } catch (e: Exception) {
            Log.e("API_ERROR", "API'den veri çekilirken hata oluştu: ${e.message}")
            emptyList()
        }
    }

    private fun searchNews(language: String,pageNumber: Int,keywords:String):Deferred<List<Article>> = GlobalScope.async {
        try {
            val response = RetrofitInstance.newsApi.searchByKeywords(language,pageNumber,keywords)
            val articleList = response.body()?.articles ?: emptyList()
            Log.i("Api  Result :",articleList.toString())
            articleList
        } catch (e: Exception) {
            Log.e("API_ERROR", "API'den veri çekilirken hata oluştu: ${e.message}")
            emptyList()
        }
    }


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
        binding = FragmentNewsResultBinding.inflate(inflater, container, false)
        val view = binding.root
        var pageNumber = 1


        recyclerView = view.findViewById(R.id.recyclerViewNewsResult)
        newsResultAdapter = NewsResultAdapter()
        newsResultAdapter.setOnClickListener(object : NewsResultAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Toast.makeText(context,"You Clicked on item no ${position}", Toast.LENGTH_SHORT).show()
                val clickedNews = newsResultAdapter.newsResultList[position]
                val clickedNewsUrl = clickedNews.url
                val intent = Intent(requireContext(), HaberWebViewActivity::class.java)
                intent.putExtra("newsUrl",clickedNewsUrl)
                startActivity(intent)
            }

        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = newsResultAdapter
        updateNewsList("tr",1)

        binding.ivIncreasePageNumber.setOnClickListener{
            pageNumber++
            binding.tvPageNumber.text = pageNumber.toString()
            updateNewsList(currentLanguage,pageNumber,currentKeywords)
        }
        binding.ivDecreasePageNumber.setOnClickListener{
            if(pageNumber>1){
                pageNumber--
                binding.tvPageNumber.text = pageNumber.toString()
                updateNewsList(currentLanguage,pageNumber,currentKeywords)
            }
            else{
                Toast.makeText(context,"Sayfa Sayısı 1'den az olamaz",Toast.LENGTH_SHORT).show()
            }
        }

        //spinner nesneleri
        val countries = resources.getStringArray(R.array.countries)
        val adapter : ArrayAdapter<String> = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,countries)
        binding.spinner.adapter = adapter

        binding.buttonSearch.setOnClickListener{
            if (binding.editTextSearch.text == null){
                currentCountryCode = binding.spinner.selectedItem.toString().lowercase()
                setLanguage()
                updateNewsList(currentCountryCode,1)
                pageNumber = 1
                binding.tvPageNumber.text = pageNumber.toString()
            }
            else{
                val keywords = binding.editTextSearch.text.toString()
                currentCountryCode = binding.spinner.selectedItem.toString().lowercase()
                setLanguage()
                currentKeywords = binding.editTextSearch.text.toString()
                updateNewsList(currentLanguage,1,keywords)
                pageNumber = 1
                binding.tvPageNumber.text = pageNumber.toString()

            }

        }








        return view
    }

    private fun setLanguage():Unit{
        when (currentCountryCode) {
            "tr" -> currentLanguage = "tr"
            "us" -> currentLanguage = "en"
            "fr" -> currentLanguage = "fr"
            "de" -> currentLanguage = "de"
        }

    }

    private fun updateNewsList(countryCodeOrLanguage: String,pageNumber: Int,keywords: String?=null) {
        GlobalScope.launch {
            try {
                println(keywords)
                println(countryCodeOrLanguage)
                val newsList = if (keywords.isNullOrEmpty()) {
                    if (countryCodeOrLanguage == currentCountryCode) {
                        getNews(countryCodeOrLanguage, pageNumber).await()
                    } else {
                        getNews("us", pageNumber).await()
                    }
                } else {
                    searchNews(countryCodeOrLanguage, pageNumber, keywords).await()
                }
                activity?.runOnUiThread {
                    newsResultAdapter.newsResultList = newsList
                    newsResultAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("LOAD_ERROR", "Veri yüklenirken hata oluştu: ${e.message}")
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewsResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewsResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}