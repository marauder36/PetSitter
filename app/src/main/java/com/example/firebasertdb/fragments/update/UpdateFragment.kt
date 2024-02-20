package com.example.firebasertdb.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firebasertdb.R
import com.example.firebasertdb.model.User
import com.example.firebasertdb.viewmodel.UserViewModel

class UpdateFragment : Fragment() {

    private lateinit var firstName:EditText
    private lateinit var lastName:EditText
    private lateinit var age:EditText

    private val args by navArgs<UpdateFragmentArgs>()

    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update, container, false)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        firstName=view.findViewById(R.id.editTextUpdateFirstName)
        lastName=view.findViewById(R.id.editTextUpdateLastName)
        age= view.findViewById(R.id.editTextUpdateAge)

        view.findViewById<EditText>(R.id.editTextUpdateFirstName).setText(args.currentUser.firstName)
        view.findViewById<EditText>(R.id.editTextUpdateLastName).setText(args.currentUser.lastName)
        view.findViewById<EditText>(R.id.editTextUpdateAge).setText(args.currentUser.age.toString())

        view.findViewById<Button>(R.id.update_button_fragment).setOnClickListener {
            updateItem()
        }

        //Add Menu
        setHasOptionsMenu(true)


        return view
    }

    private fun updateItem(){
        if(TextUtils.isEmpty(firstName.text.toString())){
            firstName.setError("First name cannot be empty")
            firstName.requestFocus()
        }else if (TextUtils.isEmpty(lastName.text.toString())) {
            lastName.setError("Last name cannot be empty")
            lastName.requestFocus()
        }else if (TextUtils.isEmpty(age.text.toString())){
            age.setError("Age cannot be empty")
            age.requestFocus()
        }
        else{

            val firstName = view?.findViewById<EditText>(R.id.editTextUpdateFirstName)?.text.toString()
            val lastName = view?.findViewById<EditText>(R.id.editTextUpdateLastName)?.text.toString()
            val age = Integer.parseInt(view?.findViewById<EditText>(R.id.editTextUpdateAge)?.text.toString())

            val updatedUser= User(args.currentUser.id, firstName,lastName,age)

            mUserViewModel.updateUser(updatedUser)
            Toast.makeText(requireContext(),"Updated Succesfully",Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menu_delete){
            deleteUserWarning()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteUserWarning() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_,_->
            mUserViewModel.deleteUser(args.currentUser)
            Toast.makeText(requireContext(),"User succesfully deleted",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)

        }
        builder.setNegativeButton("No"){_,_->}
        builder.setTitle("This action cannot be undone !")
        builder.setMessage("Are you sure you want to delete ${args.currentUser.firstName} ${args.currentUser.lastName} age ${args.currentUser.age} ?")
        builder.create().show()
    }
}