package com.karthek.android.s.helper.state

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.karthek.android.s.helper.R
import java.util.Stack


var f = false
var accessibilityServiceEnabled = false
var killStopSensitivity = 100L
var pos = 0

val appStack: Stack<String> by lazy {
	Stack<String>()
}

fun stopSession() {
	f = false
	appStack.clear()
}

fun nextStop(context: Context) {
	if (appStack.empty()) {
		f = false
		Toast.makeText(context, R.string.killed, Toast.LENGTH_SHORT).show()
		return
	}
	val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
	val packageName = appStack.pop()
	intent.data = "package:$packageName".toUri()
	intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
	Log.d("Kill stop global", "Kill Stop $packageName")
	pos = 0
	context.startActivity(intent)
}