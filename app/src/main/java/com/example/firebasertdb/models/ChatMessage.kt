package com.example.firebasertdb.models

import java.util.Date

data class ChatMessage(
    val sender: UserMessages,
    val message: String,
    val receiver:UserMessages,
    val timeStamp:Any,
    val receiverType:String,
    val viewed:String

)