package com.example.firebasertdb.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.versionedparcelable.ParcelField
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "User_table")
data class User (

    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val firstName:String,
    val lastName:String,
    val age: Int

):Parcelable