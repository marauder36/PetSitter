package com.example.firebasertdb.models

import java.sql.Timestamp

data class ReviewClass(val userThatPostedID:String, val userThatPostedPrenume:String,val userThatPostedNume:String, val userThatPostedImageURI: String,
                        val ratingServiciu:String,val ratingPetSitter:String, val comment:String,val timestamp: String,
                       val userForReviewingID:String,val userForReviewingPrenume:String, val userForReviewingNume: String, val userForReviewingImageURI: String)
