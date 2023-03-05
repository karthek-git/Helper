package com.karthek.android.s.helper.state

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.karthek.android.s.helper.R
import java.util.*


var f = false
var accessibilityServiceEnabled = false
var killStopSensitivity = 100L

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
	intent.data = Uri.parse("package:${appStack.pop()}")
	intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
	context.startActivity(intent)
}

