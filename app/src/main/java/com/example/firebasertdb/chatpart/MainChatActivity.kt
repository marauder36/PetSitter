package com.example.firebasertdb.chatpart

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.chatpart.adapter.MessagesAdaptor
import com.example.firebasertdb.databinding.ActivityMainChatBinding
import com.example.firebasertdb.models.ChatMessage
import com.example.firebasertdb.models.UserMessages
import com.example.firebasertdb.utils.Constants
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.internal.util.NotificationLite.getValue
import java.util.Date

class MainChatActivity : AppCompatActivity() {

    private lateinit var mBinding:ActivityMainChatBinding

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var petSitterReference:DatabaseReference
    private lateinit var petOwnerReference:DatabaseReference
    private lateinit var prefType:String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID:String
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar

    private lateinit var otherUserID:String
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messagesAdaptor:MessagesAdaptor
    private lateinit var messagesList:MutableList<ChatMessage>
    private lateinit var sendButton:AppCompatButton
    private lateinit var sendMessageButton:AppCompatButton
    private lateinit var editTextMessage:AppCompatEditText
    private lateinit var inputMessage:AppCompatEditText

    private lateinit var currUserPrenume:String
    private lateinit var currUserNume:String
    private lateinit var currUserImageURI:String

    private lateinit var otherUserPrenume:String
    private lateinit var otherUserNume:String
    private lateinit var otherUserImageURI:String

    override fun onStart() {
        val firstAuth=FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
            super.onStart()
        }
        else{
            startActivity(Intent(this@MainChatActivity, SelectorActivity::class.java))
            finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Handle back button click
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding=ActivityMainChatBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        messagesList= mutableListOf()

        getDataFromIntent()
        initUI()
        initRTDB()
        setTitleToolbar(otherUserID,currentUserID)
        getCurrentUserDataFromRTDB()
        initRecycler()
        getMessages()
        inputMessage.setOnFocusChangeListener{_,hasFocus->
            handleFocusChange(hasFocus)
            scrollRVToBottom()
        }

        sendButton.setOnClickListener {

            saveMessage(otherUserID,currentUserID)


        }
    }

    private fun getMessages(){
        database.getReference("Messages").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for(message in snapshot.children){
                    val currMessID=message.key.toString()

                    val senderID = message.child("sender").child("id").value.toString()
                    val senderName=message.child("sender").child("name").value.toString()
                    val senderProfileImage=message.child("sender").child("profileImage").value.toString()
                    val sender=UserMessages(senderName,senderProfileImage,senderID)

                    val receiverID=message.child("receiver").child("id").value.toString()
                    val receiverName=message.child("receiver").child("name").value.toString()
                    val receiverProfileImage=message.child("receiver").child("profileImage").value.toString()
                    val receiver=UserMessages(receiverName,receiverProfileImage,receiverID)

                    val userType = message.child("userType").value.toString()

                    val messageText=message.child("message").value.toString()

                    val viewed=message.child("viewed").value.toString()

                    val timestamp=message.child("timeStamp").getValue(Long::class.java)
                    Log.i("ConvertedString","$timestamp")
                    val date:Date
                    if(timestamp!=null){
                        date = Date(timestamp)
                        Log.i("ConvertedString","$date")

                        if(senderID==currentUserID&&receiverID==otherUserID || receiverID==currentUserID&&senderID==otherUserID) {
                            messagesList.add(ChatMessage(sender, messageText, receiver, timestamp,userType,viewed))
                            if(receiverID==currentUserID&&senderID==otherUserID)
                                database.getReference("Messages").child(currMessID).child("viewed").setValue("yes")
                        }
                    }

                }
                messagesList.sortBy { ChatMessage->
                    ChatMessage.timeStamp.toString().toLong()
                }
                messagesAdaptor.setData(messagesList)
                messagesAdaptor.notifyDataSetChanged()
                scrollRVToBottom()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("FailedToGetDATA","$error")
            }

        })
    }

    private fun getCurrentUserDataFromRTDB(){
        databaseReference.child(currentUserID).get().addOnSuccessListener { snapshot->
            currUserPrenume = snapshot.child("prenume").value.toString()
            currUserNume = snapshot.child("nume").value.toString()
            currUserImageURI= snapshot.child("imagine").value.toString()
            Log.i("OtherUserDataCurr","$currUserPrenume $currUserNume $currUserImageURI")
        }
        if(prefType=="PetSitter"){
            petOwnerReference.child(otherUserID).get().addOnSuccessListener {snapshot->
                otherUserPrenume = snapshot.child("prenume").value.toString()
                otherUserNume = snapshot.child("nume").value.toString()
                otherUserImageURI= snapshot.child("imagine").value.toString()
                Log.i("OtherUserDataPetSitter","$otherUserPrenume $otherUserNume $otherUserImageURI")
            }
        }
        else if(prefType=="Petowner"){
            petSitterReference.child(otherUserID).get().addOnSuccessListener {snapshot->
                otherUserPrenume = snapshot.child("prenume").value.toString()
                otherUserNume = snapshot.child("nume").value.toString()
                otherUserImageURI= snapshot.child("imagine").value.toString()
                Log.i("OtherUserDataPetowner","$otherUserPrenume $otherUserNume $otherUserImageURI")
            }
        }
    }

    private fun saveMessage(otherUserID: String,currentUserID: String) {
        val textMesaj = editTextMessage.text.toString()
        val senderName = "$currUserPrenume $currUserNume"
        val sender = UserMessages(senderName, currUserImageURI, currentUserID)

        val receiverName = "$otherUserPrenume $otherUserNume"
        val receiver = UserMessages(receiverName, otherUserImageURI, otherUserID)

        val date = Date().toString()


        if (prefType == "PetSitter") {
            val receiverType = "Petowner"
            val currentMessage=ChatMessage(sender,textMesaj,receiver, ServerValue.TIMESTAMP,receiverType,"no")

            database.getReference("Messages").child("Mesaj de la $senderName pentru $receiverName la data ${date}").setValue(currentMessage)
                .addOnSuccessListener {
                    editTextMessage.text?.clear()
                }
        }

        else if(prefType=="Petowner") {
            val receiverType = "PetSitter"
            val currentMessage=ChatMessage(sender,textMesaj,receiver, ServerValue.TIMESTAMP,receiverType,"no")

            database.getReference("Messages").child("Mesaj de la $senderName pentru $receiverName la data ${date}").setValue(currentMessage)
                .addOnSuccessListener {
                    editTextMessage.text?.clear()
                }
        }



    }

    private fun scrollRVToBottom(){
        messagesRecyclerView.scrollToPosition(messagesAdaptor.itemCount-1)
    }
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextMessage.windowToken, 0)
    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar, otherUserPrenume: String, otherUserNume: String){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setTitle("Chat with $otherUserPrenume $otherUserNume")
        }
    }

    private fun setTitleToolbar(otherUserID:String,currentUserID:String){

        if (prefType=="PetSitter"){
            petOwnerReference.child(otherUserID).get().addOnSuccessListener {
                val otherUserPrenume = it.child("nume").value.toString()
                val otherUserNume = it.child("prenume").value.toString()
                initToolbar(toolbar,otherUserPrenume,otherUserNume)
            }
        }
        else if (prefType=="Petowner"){
            petSitterReference.child(otherUserID).get().addOnSuccessListener {
                val otherUserPrenume = it.child("nume").value.toString()
                val otherUserNume = it.child("prenume").value.toString()
                initToolbar(toolbar,otherUserPrenume,otherUserNume)
            }
        }

    }

    private fun getDataFromIntent(){
        otherUserID=intent.getStringExtra(Constants.chat_with_other_user_ID)!!
        Log.i("OtherUserID","Got: $otherUserID")
    }

    private fun initRecycler() {
        messagesAdaptor = MessagesAdaptor(this,messagesList)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.setHasFixedSize(false)
        messagesRecyclerView.adapter = messagesAdaptor
        
    }

    private fun initUI() {
        messagesRecyclerView = findViewById(R.id.message_recycler_view)
        toolbar = findViewById(R.id.main_chat_toolbar)
        sendButton=findViewById(R.id.send_message_button)
        sendMessageButton=findViewById(R.id.send_message_button)
        editTextMessage=findViewById(R.id.input_message)
        inputMessage=findViewById(R.id.input_message)

    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        currentUserID=mAuth.currentUser?.uid.toString()
        
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetOwner")!!

        petSitterReference=database.getReference("PetSitter")
        petOwnerReference =database.getReference("Petowner")

        databaseReference = database.getReference(prefType)
    }
    private fun handleFocusChange(hasFocus: Boolean) {
        if (hasFocus) {
            val layoutParams = messagesRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomToTop = R.id.linear_l
            messagesRecyclerView.layoutParams = layoutParams
            messagesRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom && messagesAdaptor.itemCount>0) {
                    messagesRecyclerView.postDelayed({
                        messagesRecyclerView.smoothScrollToPosition(messagesAdaptor.itemCount - 1)
                    }, 100)
                }
            }
        } else {
            val layoutParams = messagesRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomToTop = ConstraintLayout.LayoutParams.PARENT_ID
            messagesRecyclerView.layoutParams = layoutParams
            scrollRVToBottom()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}