package com.example.firebasertdb.utils

import android.content.Context
import android.content.SharedPreferences


class SharedStorageManager(context: Context) {

    private val BUCKET_NAME = "PetMePrefs"
    private var sharedPref: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPref = context.getSharedPreferences(BUCKET_NAME, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }

    fun put(key: String, value: String) {
        editor.putString(key, value)
            .apply()
    }

    fun put(key: String, value: Boolean) {
        editor.putBoolean(key, value)
            .apply()
    }

    fun put(key: String, value: Long) {
        editor.putLong(key, value)
            .apply()
    }

    fun put(key: String, value: Int) {
        editor.putInt(key, value)
            .apply()
    }

    fun put(key: String, value: Set<String>) {
        editor.putStringSet(key, value)
            .apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, true)
    }

    fun getLong(key: String): Long? {
        return sharedPref.getLong(key, 0)
    }

    fun getInt(key: String): Int {
        return sharedPref.getInt(key, 0)
    }

    fun getString(key: String): String? {
        return sharedPref.getString(key, null)
    }

    fun getStringSet(key: String): Set<String>? {
        return sharedPref.getStringSet(key, null)
    }

    fun getDefaultBoolean(key: String,defaultKey: Boolean): Boolean {
        return sharedPref.getBoolean(key, defaultKey)
    }

    fun getDefaultLong(key: String,defaultKey: Long): Long? {
        return sharedPref.getLong(key, defaultKey)
    }

    fun getDefaultInt(key: String, defaultKey: Int): Int {
        return sharedPref.getInt(key, defaultKey)
    }

    fun getDefaultString(key: String,defaultKey: String): String? {
        return sharedPref.getString(key, defaultKey)
    }

    fun getDefaultStringSet(key: String, defaultKey: Set<String>): Set<String>? {
        return sharedPref.getStringSet(key, defaultKey)
    }

    fun clear() {
        editor.clear()
            .apply()
    }


}