package com.example.firebasertdb.model

data class UserAttributes(val uID:String, val distance:String, val nume:String, val prenume:String,
                          val email:String, val rating:String, val mediePretServiciu:String,
                          val nrEvenimente:String,val uri:String,val datiEvenimente:MutableList<String>,
                          val statusEvenimente:MutableList<String>)