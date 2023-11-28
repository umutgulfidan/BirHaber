package com.example.birhaberdeneme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.birhaberdeneme.databinding.FragmentUserAccountSettingsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserAccountSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserAccountSettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding : FragmentUserAccountSettingsBinding
    val auth = FirebaseAuth.getInstance()
    val storage =FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()
    private lateinit var currentUserId: String


    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode== Activity.RESULT_OK){
            val selectedImageUrl : Uri? = result.data?.data
            selectedImageUrl?.let {
                uploadImageToFireBaseStroge(it)
            }
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

        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }



        // Kullanıcı oturum açmışsa id yi alıp veri çekme
        currentUser.let {

            val userId = currentUser?.uid
            if(userId != null){
                val userRef = db.collection("Users").document(userId)
                userRef.get().addOnSuccessListener {
                    if(it.exists()){
                        val data = it.data
                        val email = data?.get("email").toString()
                        val role = data?.get("role").toString()
                        val profilePhotoUrl =data?.get("profilePictureUrl").toString()
                        val okunanHaberSayisi = data?.get("okunanHaberSayisi").toString()
                        val favoriHaberArray = data?.get("favoriteNews") as ArrayList<String>
                        val favoriHaberSayisi = favoriHaberArray.size.toString()
                        arayuzGuncelle(email,role,okunanHaberSayisi,favoriHaberSayisi)
                    }
                }
            }




        }


        // Inflate the layout for this fragment
        binding = FragmentUserAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogout.setOnClickListener()
        {
            auth.signOut()
            Toast.makeText(activity,"Çıkış Yapıldı",Toast.LENGTH_SHORT).show()
            val intent = Intent(activity,MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
        binding.ivSetPhoto.setOnClickListener{

            openGallery()
        }

    }

    private fun arayuzGuncelle(email:String,role:String,okunanHaberSayisi:String,favoriHaberSayisi:String){
        binding.tvEmail.text = email
        binding.tvRol.text = role
        binding.tvFavoriHaberSayisi.text = favoriHaberSayisi
        binding.tvOkunanHaberSayisi.text = okunanHaberSayisi
        loadProfileImage()

    }

    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(galleryIntent)
    }

    private fun uploadImageToFireBaseStroge(imageUri: Uri){
        binding.progressBar.visibility = View.VISIBLE
        val storageRef = storage.reference
        val imageRef =  storageRef.child("ProfilPictures/$currentUserId/profile.jpg")

        imageRef.putFile(imageUri).addOnSuccessListener {
            //Resim yükleme başarılı oldu
            imageRef.downloadUrl.addOnSuccessListener {
                //Resim URL SİNİ AL VE FİRESTORE DA GÜNCELLE
                updateProfileImageUrl(imageUri.toString())

                binding.progressBar.visibility = View.GONE
            }
        }
            .addOnFailureListener{
                it.printStackTrace()
                Log.e("FirestoreError","Belge Alınamadı : ${it.message}")
                binding.progressBar.visibility = View.GONE
            }

    }

    private fun updateProfileImageUrl(imageUri: String){
        //Fire store da kullanıcının profil resim url sini günceller
        db.collection("Users").document(currentUserId).update("profilePictureUrl",imageUri).addOnSuccessListener {
            loadProfileImage()
        }
            .addOnFailureListener{
                //Hata Durumunda
                it.printStackTrace()
            }
    }

    private fun loadProfileImage(){
        db.collection("Users").document(currentUserId).get().addOnSuccessListener {
            if(it.exists()){
                val user = it.toObject(UserModule::class.java)
                if(user != null){
                    if(!user.profilePictureUrl.isNullOrEmpty())
                    {
                        val storageRef = storage.reference.child("ProfilPictures").child(user.id).child("profile.jpg")
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.baseline_account_circle_white_24) // Burada default_image, drawable klasöründe bulunan varsayılan görselinizdir
                                .error(R.drawable.baseline_account_circle_white_24)// Eğer bir hata olursa gösterilecek görsel
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(binding.ivProfil)
                        }.addOnFailureListener {
                            // Eğer resim yüklenirken bir hata olursa ne yapılacağı burada tanımlanabilir
                            // Örneğin: Toast mesajı gösterilebilir
                            Toast.makeText(context, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }else {
                        // Eğer profil resmi yoksa, imageView'inizde varsayılan görseli gösterebilirsiniz
                        Glide.with(this).load(R.drawable.baseline_account_circle_white_24).into(binding.ivProfil)
                    }
                }
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
         * @return A new instance of fragment UserAccountSettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserAccountSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}