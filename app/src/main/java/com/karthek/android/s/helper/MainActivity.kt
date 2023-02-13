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
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.karthek.android.s.helper.ui.AppListViewModel
import com.karthek.android.s.helper.ui.MainActivityView
import com.karthek.android.s.helper.ui.theme.AppTheme
import com.karthek.android.s.helper.ui.theme.BlackSanUI
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private var path: String? = null
	private val viewModel: AppListViewModel by viewModels()

	private val saveContent = registerForActivityResult(CreateDocument()) { result: Uri? ->
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
		WindowCompat.setDecorFitsSystemWindows(window, false)
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

		setContent {
			AppTheme {
				BlackSanUI {
					val systemUiController = rememberSystemUiController()
					val useDarkIcons = !isSystemInDarkTheme()
					SideEffect {
						systemUiController.setSystemBarsColor(Color.Transparent, useDarkIcons)
					}
					ProvideWindowInsets {
						Surface {
							MainActivityView(
								viewModel = viewModel,
								saveAppCallback = this::saveApp,
								uninstallCallback = this::uninstall
							)
						}
					}
				}
			}
		}
	}

	private fun uninstall(intent: Intent) {
		uninstallApp.launch(intent)
	}

	private fun saveApp(path: String, name: String) {
		this.path = path
		saveContent.launch(name)
	}
}