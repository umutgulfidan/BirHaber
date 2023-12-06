package com.example.birhaberdeneme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.birhaberdeneme.databinding.FragmentAdminHaberEkleBinding
import com.example.birhaberdeneme.databinding.FragmentUserAccountSettingsBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminHaberEkleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminHaberEkleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding:FragmentAdminHaberEkleBinding
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storage =FirebaseStorage.getInstance()
    private lateinit var currentUserId: String
    private lateinit var currentNewsId: String

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode== Activity.RESULT_OK){
            val selectedImageUrl : Uri? = result.data?.data
            selectedImageUrl?.let {
                uploadImageToFireBaseStroge(it)
            }
        }
        else{
            Toast.makeText(context,"${result.data},${result.resultCode}",Toast.LENGTH_SHORT).show()
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
        // Inflate the layout for this fragment
        binding = FragmentAdminHaberEkleBinding.inflate(inflater, container, false)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        currentNewsId = UUID.randomUUID().toString()

        binding.btnHaberEkle.setOnClickListener{
            val news = NewsModule(
                currentNewsId,
                binding.etHaberBaslik.text.toString(),
                binding.etKisaAciklama.text.toString(),
                binding.etAnaMetin.text.toString(),
                "",
                currentUserId,
                Timestamp.now()
            )
            firestore.collection("News").document(currentNewsId).set(news).addOnSuccessListener {
                openGallery()
                Toast.makeText(context,"Başarıyla Eklendi",Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener{
                    Toast.makeText(context,"Hata ${it.message}",Toast.LENGTH_SHORT).show()
                }



        }

        return binding.root
    }

    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            getContent.launch(galleryIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Dosya seçme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFireBaseStroge(imageUri: Uri) {
        val strogeRef = FirebaseStorage.getInstance().reference
        val imageRef = strogeRef.child("NewsPictures/${currentNewsId}/news_image.jpg")

        imageRef.putFile(imageUri).addOnSuccessListener{
            //Resim yükleme başarılı oldu
            imageRef.downloadUrl.addOnSuccessListener {
                //Resim URL SİNİ AL VE FİRESTORE DA GÜNCELLE
                updateNewsImageUrl(imageUri.toString())

            }
        }.addOnFailureListener{
            it.printStackTrace()
            Log.e("FirestoreError","Belge Alınamadı : ${it.message}")
        }

    }

    private fun updateNewsImageUrl(imageUri: String) {
        firestore.collection("News").document(currentNewsId).update("newsImageUrl",imageUri).addOnSuccessListener {
            Toast.makeText(context,"Başarıyla Haber Resmi Verisi Güncellendi",Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener{
                Toast.makeText(context,"Haber Urlsi Firestore'a Aktarilamadi, Haber Default Resimle gösterilecektir",Toast.LENGTH_SHORT).show()
                it.printStackTrace()
            }

    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminHaberEkleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminHaberEkleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}