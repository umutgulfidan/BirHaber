package com.example.birhaberdeneme

import com.google.firebase.Timestamp

data class UserModule(
    val id : String,
    val active : Boolean?,
    val email : String?,
    val password : String?,
    val registerDate :Timestamp?,
    val profilPictureUrl : String?,
    val role : String?,
    val favoriteNews : List<String>?,
    val okunanHaberSayisi : Long?
) {
    constructor():this("",null,"","",null,"","", emptyList(),0){

    }
}