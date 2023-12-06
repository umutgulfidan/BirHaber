package com.example.birhaberdeneme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birhaberdeneme.databinding.FragmentAdminHaberleriYonetBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminHaberleriYonetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminHaberleriYonetFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var newAdapter: NewsManagementAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val newsCollection = firestore.collection("News")
    private lateinit var binding:FragmentAdminHaberleriYonetBinding

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
        // Inflate the layout for this fragment
        binding =  FragmentAdminHaberleriYonetBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = view.findViewById(R.id.recyclerViewNewsManagement)
        newAdapter = NewsManagementAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = newAdapter
        loadNewsData()

        binding.buttonSearchNewsManagement.setOnClickListener{
            val text = binding.editTextSearchNewsManagement.text.toString().lowercase(Locale.getDefault())
            if(text.isNotEmpty()){
                newAdapter.filter(text)
                binding.btnAramaTemizleNewsManagement.visibility = View.VISIBLE
                binding.buttonSearchNewsManagement.visibility = View.GONE
            }
            else{
                loadNewsData()
            }
        }
        binding.btnAramaTemizleNewsManagement.setOnClickListener{
            loadNewsData()
            binding.btnAramaTemizleNewsManagement.visibility=View.GONE
            binding.buttonSearchNewsManagement.visibility=View.VISIBLE
            binding.editTextSearchNewsManagement.text.clear()
        }


        return  view

    }

    private fun loadNewsData(){
        newsCollection.get().addOnSuccessListener {
            result ->
            val newList = mutableListOf<NewsModule>()
            for(document in result.documents){
                val news = document.toObject(NewsModule::class.java)
                news?.let {
                    newList.add(it)
                }
                newAdapter.updateNewsList(newList)
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
         * @return A new instance of fragment AdminHaberleriYonetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminHaberleriYonetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}