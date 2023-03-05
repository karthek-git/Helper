package com.karthek.android.s.helper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.karthek.android.s.helper.ui.screens.KillStopConfigScreen
import com.karthek.android.s.helper.ui.theme.AppTheme

class KillStopConfigActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent { ScreenContent() }
	}

	private fun startAccessibilityActivity() {
		Toast.makeText(this, R.string.enable_as, Toast.LENGTH_SHORT).show()
		startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface(
				modifier = Modifier.fillMaxSize(),
				color = MaterialTheme.colorScheme.background
			) {
				KillStopConfigScreen(onEnableClick = this::startAccessibilityActivity)
			}
		}
	}
}

