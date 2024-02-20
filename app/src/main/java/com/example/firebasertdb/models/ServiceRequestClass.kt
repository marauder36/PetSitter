package com.example.firebasertdb.models

import java.io.Serializable

data class ServiceRequestClass(val requestingUserID:String,val requestingUserName:String,val serviceRequested:String,var status: String,
                               var date:String,var hour:String, var petID:String, var petName:String, var petRace:String):Serializable
