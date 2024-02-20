package com.example.firebasertdb.repository

import androidx.lifecycle.LiveData
import com.example.firebasertdb.data.UserDAO
import com.example.firebasertdb.model.User

class UserRepository(private val userDao: UserDAO) {

    val readAllData: LiveData<List<User>> = userDao.readAllData()

    suspend fun addUser(user: User){
        userDao.addUser(user)
    }

    suspend fun updateUser(user:User){
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user:User){
        userDao.deleteUser(user)
    }

    suspend fun deleteAllUsers(){
        userDao.deleteAllUsers()
    }
}
