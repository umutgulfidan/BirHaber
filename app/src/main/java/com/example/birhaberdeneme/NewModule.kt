package com.example.birhaberdeneme

import com.google.firebase.Timestamp

data class NewsModule(
    val newsId :String,
    val newsTitle: String?,
    val newsShortDescription: String?,
    val newsText : String?,
    val newsImageUrl: String?,
    val uploadedById: String?,
    val newsUploadDate: Timestamp?
){
    constructor():this("","","","",""
    ,"",null){

    }
}