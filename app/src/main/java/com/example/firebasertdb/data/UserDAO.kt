package com.example.firebasertdb.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.firebasertdb.model.User


@Dao
interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Update
    suspend fun updateUser(user:User)

    @Delete
    suspend fun deleteUser(user:User)

    @Query(value="DELETE FROM user_table")
    suspend fun deleteAllUsers()

    @Query(value = "SELECT * FROM user_table ORDER BY id ASC")
    fun readAllData():LiveData<List<User>>

}