package com.karthek.android.s.helper

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileUtils
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import com.google.android.gms.ads.MobileAds
import com.karthek.android.s.helper.ui.AppListViewModel
import com.karthek.android.s.helper.ui.components.ScaleIndication
import com.karthek.android.s.helper.ui.screens.MainScreen
import com.karthek.android.s.helper.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private var path: String? = null
	private val viewModel: AppListViewModel by viewModels()

	private val saveContent =
		registerForActivityResult(
			CreateDocument("application/vnd.android.package-archive")
		) { result: Uri? ->
			if (result == null || path == null) return@registerForActivityResult
			lifecycleScope.launch {
				var response = "Saved"
				withContext(Dispatchers.IO) {
					kotlin.runCatching {
						try {
							val i = FileInputStream(path)
							val o =
								contentResolver.openOutputStream(result) ?: return@withContext
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) FileUtils.copy(i, o)
							else i.copyTo(o)
							i.close(); o.close()
						} catch (e: Exception) {
							e.message?.let { response = it }
						}
					}
					path = null
				}
				Toast.makeText(this@MainActivity, response, Toast.LENGTH_SHORT).show()
			}
		}

	private val uninstallApp = registerForActivityResult(StartActivityForResult()) {
		if (it.resultCode == RESULT_OK) {
			viewModel.removeUninstalledApp()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		val iconSize = 48 * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
		val appIconLoader = AppIconLoader(iconSize, true, this@MainActivity)
		Coil.setImageLoader {
			ImageLoader.Builder(this)
				.components {
					add(AppIconKeyer())
					add(ActivityIconKeyer())
					add(AppIconFetcher.Factory(appIconLoader))
					add(ActivityIconFetcher.Factory(appIconLoader))
				}
				.build()
		}

		setContent { ScreenContent() }

		val backgroundScope = CoroutineScope(Dispatchers.IO)
		backgroundScope.launch {
			MobileAds.initialize(this@MainActivity) {}
		}
	}

	private fun uninstall(intent: Intent) {
		uninstallApp.launch(intent)
	}

	private fun saveApp(path: String, name: String) {
		this.path = path
		saveContent.launch(name)
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface(modifier = Modifier.fillMaxSize()) {
				CompositionLocalProvider(LocalIndication provides ScaleIndication) {
					MainScreen(
						viewModel = viewModel,
						saveAppCallback = this::saveApp,
						uninstallCallback = this::uninstall
					)
				}
			}
		}
	}
}