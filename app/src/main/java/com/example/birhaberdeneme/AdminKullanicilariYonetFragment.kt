package com.example.birhaberdeneme

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birhaberdeneme.databinding.FragmentAdminKullanicilariYonetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminKullanicilariYonetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminKullanicilariYonetFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding : FragmentAdminKullanicilariYonetBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val firestore= FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("Users")

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
        binding = FragmentAdminKullanicilariYonetBinding.inflate(inflater,container,false)
        val view = binding.root
        recyclerView = view.findViewById(R.id.recyclerViewUser)
        userAdapter = UserAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter =userAdapter

        loadUsersData()

         binding.buttonSearchUserManagement.setOnClickListener{
             val text = binding.editTextSearchUserManagement.text.toString().lowercase()
             if(text.isNotEmpty()){
                 userAdapter.filter(text)
                 binding.buttonSearchUserManagement.visibility = View.GONE
                 binding.btnAramaTemizleUserManagement.visibility = View.VISIBLE
             }
             else{
                 loadUsersData()
             }
         }

        binding.btnAramaTemizleUserManagement.setOnClickListener{
            binding.editTextSearchUserManagement.text.clear()
            binding.btnAramaTemizleUserManagement.visibility = View.GONE
            binding.buttonSearchUserManagement.visibility = View.VISIBLE
            loadUsersData()
        }

        binding.btnCikis.setOnClickListener{
            auth.signOut()
            Toast.makeText(activity,"Çıkış Yapıldı", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity,MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }

    private fun loadUsersData(){
        usersCollection.get().addOnSuccessListener {
            result ->
            val userList = mutableListOf<UserModule>()
            for (document in result.documents){
                var users = document.toObject(UserModule::class.java)
                users?.let {
                        userList.add(it)
                }
            }
            userAdapter.updateUserList(userList)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminKullanicilariYonetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminKullanicilariYonetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}