package com.karthek.android.s.helper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.karthek.android.s.helper.ui.theme.AppTheme
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LicensesActivity : ComponentActivity() {

	private val title = "Open source licenses"
	private var libs by mutableStateOf<List<Library>?>(null)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.CREATED) {
				libs = withContext(Dispatchers.Default) {
					Libs.Builder().withContext(this@LicensesActivity).build().libraries
				}
			}
		}
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface(color = MaterialTheme.colorScheme.background) {
				LicensesContent()
			}
		}
	}

	@Composable
	fun LicensesContent() {
		val lazyListState = rememberLazyListState()
		var licenseName by remember { mutableStateOf(title) }
		var nav by remember { mutableStateOf(0) }
		var licenseText by remember { mutableStateOf("") }
		BackHandler(nav != 0) {
			nav = 0
			licenseName = title
		}
		CommonScaffold(name = licenseName, onBackClick = { this.finish() }) { paddingValues ->
			if (nav == 1) {
				LicenseViewer(paddingValues, licenseText)
			} else {
				LicensesMenu(lazyListState, paddingValues) { license ->
					licenseName = license.name
					nav = 1
					licenseText = license.licenseContent.toString()
				}
			}
		}
	}

	@Composable
	fun LicensesMenu(
		state: LazyListState, paddingValues: PaddingValues, callback: (License) -> Unit,
	) {
		if (libs != null) {
			LazyColumn(state = state, contentPadding = paddingValues) {
				items(libs!!) {
					Column(modifier = Modifier
						.clickable {
							callback(it.licenses.first())
						}
						.padding(start = 16.dp)) {
						Text(
							text = it.name, modifier = Modifier
								.fillMaxWidth()
								.padding(24.dp)
						)
						Divider()
					}
				}
			}
		} else {
			CircularProgressIndicator(
				modifier = Modifier
					.padding(paddingValues)
					.fillMaxSize()
					.size(64.dp)
					.wrapContentSize(Alignment.Center), strokeWidth = 4.dp
			)
		}
	}

	@Composable
	fun LicenseViewer(paddingValues: PaddingValues, text: String) {
		Text(
			text = text,
			fontWeight = FontWeight.Light,
			modifier = Modifier
				.padding(paddingValues)
				.padding(horizontal = 16.dp)
				.verticalScroll(rememberScrollState())
		)
	}

}
