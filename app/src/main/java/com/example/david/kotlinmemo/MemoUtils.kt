package com.example.david.kotlinmemo

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object MemoUtils {
	private val PREFERENCE_NAME = "com.example.david.kotlinmemo"
	private val PASSWORD = "memo_password"
	private var sharedPreferences: SharedPreferences? = null

	fun getDate(millis: Long): String {
		val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.getDefault())
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = millis
		return simpleDateFormat.format(calendar.time)
	}

	fun getSharedPreferences(context: Context): SharedPreferences? {
		if (sharedPreferences == null) {
			sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
		}
		return sharedPreferences
	}

	fun getPassword(): String {
		return sharedPreferences?.getString(PASSWORD, "") ?: ""
	}

	fun setPassword(password: String) {
		val editor = sharedPreferences?.edit()
		if (editor != null) {
			editor.putString(PASSWORD, password)
			editor.apply()
		}
	}
}
