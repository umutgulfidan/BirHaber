package com.example.birhaberdeneme

import android.app.AlertDialog
import android.icu.text.Transliterator.Position
import android.util.Log
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
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    public var userList : List<UserModule> = emptyList()



    fun updateUserList(newUserList : List<UserModule>){
        userList = newUserList
        notifyDataSetChanged()
    }

    fun filter(text:String){
        val filteredList = userList.filter { user ->
            user.email?.contains(text,ignoreCase = true) == true
        }
        updateUserList(filteredList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    class UserViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){

        // ViewHolder içindeki view'ları tanımla
        val storageInstance = FirebaseStorage.getInstance()
        private val emailTextView: TextView = itemView.findViewById(R.id.tvEposta)
        private val roleTextView: TextView = itemView.findViewById(R.id.tvRole)
        private val activeTextView: TextView = itemView.findViewById(R.id.tvActive)
        private val banButton: Button = itemView.findViewById(R.id.btnYasakla)
        private val adminButton: Button = itemView.findViewById(R.id.btnYetkilendir)
        private val userProfilImageView : ImageView = itemView.findViewById(R.id.ivUserProfileImage)

        fun bind(user: UserModule) {
            // Kullanıcı verilerini ilgili view'lara ata
            emailTextView.text = user.email
            roleTextView.text = user.role
            activeTextView.text = user.active.toString()
            if(user.active == false){
                banButton.text = "Yasağı Kaldır"
            }
            else{
                banButton.text = "Yasakla"
            }
            if(user.role == "Admin"){
                adminButton.text = "Yetkisini Al"
            }
            else{
                adminButton.text = "Yetkilendir"
            }

            // Butonlara tıklama işlevlerini ekle
            banButton.setOnClickListener {
                    val userRef = FirebaseFirestore.getInstance().collection("Users").document(user.id)

                    userRef.get().addOnSuccessListener { documentSnapshot ->
                        val isActive = documentSnapshot.getBoolean("active") ?: false

                        // Tersine çevrilen değeri güncelle
                        userRef.update("active", !isActive).addOnSuccessListener {
                            if (isActive) {
                                // Eğer aktifse pasif hale getirildi
                                Toast.makeText(itemView.context, "Kullanıcı Başarıyla Yasaklandı", Toast.LENGTH_SHORT).show()
                                activeTextView.text = "false"
                                banButton.text = "Yasağı Kaldır"
                            } else {
                                // Eğer pasifse aktif hale getirildi
                                Toast.makeText(itemView.context, "Kullanıcının Yasağı Başarıyla Kaldırıldı", Toast.LENGTH_SHORT).show()
                                activeTextView.text = "true"
                                banButton.text = "Yasakla"
                            }
                        }.addOnFailureListener { exception ->
                            // Hata durumunda işlemler
                            Toast.makeText(itemView.context, "Hata: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        // Belirtilen kullanıcı bulunamadığında veya başka bir hata olduğunda işlemler
                        Toast.makeText(itemView.context, "Kullanıcı Bulunamadı veya Hata Oluştu: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }

            }

            adminButton.setOnClickListener {
                // Admin yap butonuna tıklandığında yapılacak işlemler
                // Kullanıcının rolünü admin olarak güncelleme gibi işlemler yapılabilir

                    val userRef = FirebaseFirestore.getInstance().collection("Users").document(user.id)

                    userRef.get().addOnSuccessListener { documentSnapshot ->
                        val currentRole = documentSnapshot.getString("role") ?: "User"

                        // Tersine çevrilen rolü güncelle
                        val newRole = if (currentRole == "User") "Admin" else "User"
                        userRef.update("role", newRole).addOnSuccessListener {
                            val buttonText = if (newRole == "User") "Yetkilendir" else "Yetkisini Al"
                            Toast.makeText(itemView.context, "Kullanıcı Başarıyla $newRole Yapıldı", Toast.LENGTH_SHORT).show()
                            roleTextView.text = newRole
                            adminButton.text = buttonText
                        }.addOnFailureListener { exception ->
                            // Hata durumunda işlemler
                            Toast.makeText(itemView.context, "Hata: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        // Belirtilen kullanıcı bulunamadığında veya başka bir hata olduğunda işlemler
                        Toast.makeText(itemView.context, "Kullanıcı Bulunamadı veya Hata Oluştu: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }

                }

            if(!user.profilePictureUrl.isNullOrEmpty())
            {
                val storageRef = storageInstance.reference.child("ProfilPictures").child(user.id).child("profile.jpg")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.baseline_account_circle_24) // Burada default_image, drawable klasöründe bulunan varsayılan görselinizdir
                        .error(R.drawable.hata_vector_kirmizi_32)// Eğer bir hata olursa gösterilecek görsel
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(userProfilImageView)
                }.addOnFailureListener {
                    // Eğer resim yüklenirken bir hata olursa ne yapılacağı burada tanımlanabilir
                    // Örneğin: Toast mesajı gösterilebilir
                    Toast.makeText(itemView.context, "Resim yüklenirken hata oluştu : ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }else {
                // Eğer profil resmi yoksa, imageView'inizde varsayılan görseli gösterebilirsiniz
                userProfilImageView.setImageResource(R.drawable.baseline_account_circle_24)
            }

            }


        }

    }