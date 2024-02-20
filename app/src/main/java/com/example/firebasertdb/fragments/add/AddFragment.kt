package com.example.firebasertdb.fragments.add

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.firebasertdb.R
import com.example.firebasertdb.model.User
import com.example.firebasertdb.viewmodel.UserViewModel

class AddFragment : Fragment() {

    private lateinit var mUserViewModel: UserViewModel

    private lateinit var firstName:EditText
    private lateinit var lastName:EditText
    private lateinit var age:EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        firstName=view.findViewById(R.id.editTextPersonFirstName)
        lastName=view.findViewById(R.id.editTextPersonLastName)
        age= view.findViewById(R.id.editTextPersonAge)

        mUserViewModel=ViewModelProvider(this).get(UserViewModel::class.java)

        view.findViewById<Button>(R.id.add_button).setOnClickListener {

            insertDataToDatabase()

        }

        return view
    }

    private fun insertDataToDatabase() {
        if(TextUtils.isEmpty(firstName.text.toString())){
            firstName.setError("First Name Required")
            firstName.requestFocus()
        }else if (TextUtils.isEmpty(lastName.text.toString())) {
            lastName.setError("Last Name Required")
            lastName.requestFocus()
        }else if (TextUtils.isEmpty(age.text.toString())){
            age.setError("Age Required")
            age.requestFocus()
        }
        else{
            val firstName = view?.findViewById<EditText>(R.id.editTextPersonFirstName)?.text.toString()
            val lastName = view?.findViewById<EditText>(R.id.editTextPersonLastName)?.text.toString()
            val age = view?.findViewById<EditText>(R.id.editTextPersonAge)?.text

            val user = User(0,firstName,lastName,Integer.parseInt(age.toString()))
            mUserViewModel.addUser(user)
            Toast.makeText(requireContext(),"SUCCESfully added!",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }

    }


}