package com.example.firebasertdb.models

data class UserMessages(
    val name: String,
    val profileImage: String,
    val id: String
) {
    constructor() : this("", "", "")
}