package com.karthek.android.s.helper

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.karthek.android.s.helper.state.Prefs
import com.karthek.android.s.helper.ui.screens.KillStopConfigScreenContent
import com.karthek.android.s.helper.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class KillStopConfigActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface(
				modifier = Modifier.fillMaxSize(),
				color = MaterialTheme.colorScheme.background
			) { KillStopConfigScreen(onBackClick = { this.finish() }, viewModel = hiltViewModel()) }
		}
	}
}

@HiltViewModel
class KillStopConfigViewModel @Inject constructor(val prefs: Prefs) : ViewModel(){

}

private fun Context.startAccessibilityActivity() {
	Toast.makeText(this, R.string.enable_as, Toast.LENGTH_SHORT).show()
	startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

@Composable
fun KillStopConfigScreen(
	onBackClick: () -> Unit,
	viewModel: KillStopConfigViewModel,
) {
	val context = LocalContext.current
	KillStopConfigScreenContent(
		prefs = viewModel.prefs,
		onEnableClick = { context.startAccessibilityActivity() },
		onBackClick = onBackClick
	)
}

