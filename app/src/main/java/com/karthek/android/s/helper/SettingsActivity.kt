package com.karthek.android.s.helper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.karthek.android.s.helper.ui.theme.AppTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface {
				val version = remember { getVersionString() }
				SettingsScreen(version)
			}
		}
	}

	private fun getVersionString(): String {
		return "${BuildConfig.VERSION_NAME}-${BuildConfig.BUILD_TYPE} (${BuildConfig.VERSION_CODE})"
	}

	private fun startLicensesActivity() {
		startActivity(Intent(this, LicensesActivity::class.java))
	}

	@Composable
	fun SettingsScreen(version: String) {
		CommonScaffold(name = "About", onBackClick = { this.finish() }) { paddingValues ->
			Column(modifier = Modifier.padding(paddingValues)) {
				ListItem(
					headlineContent = { Text(text = "Kill Stop") },
					modifier = Modifier.clickable {
						startActivity(
							Intent(
								this@SettingsActivity,
								KillStopConfigActivity::class.java
							)
						)
					}
				)
				HorizontalDivider()
				ListItem(
					headlineContent = { Text(text = "Version") },
					supportingContent = { Text(text = version, fontWeight = FontWeight.Light) }
				)
				HorizontalDivider()
				ListItem(
					headlineContent = { Text(text = "Privacy Policy") },
					modifier = Modifier.clickable {
						val uri =
							"https://policies.karthek.com/Helper/-/blob/master/privacy.md".toUri()
						startActivity(Intent(Intent.ACTION_VIEW, uri))
					})
				HorizontalDivider()
				ListItem(
					headlineContent = { Text(text = "Open source licenses") },
					modifier = Modifier.clickable { startLicensesActivity() }
				)
				HorizontalDivider()
				LicenseBottomSheet {
					val uri = "https://www.apache.org/licenses/LICENSE-2.0.txt".toUri()
					startActivity(Intent(Intent.ACTION_VIEW, uri))
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonScaffold(
	name: String,
	onBackClick: () -> Unit,
	content: @Composable (PaddingValues) -> Unit,
) {
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
				navigationIcon = {
					IconButton(onClick = onBackClick) {
						Icon(
							imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.go_back)
						)
					}
				},
				scrollBehavior = scrollBehavior
			)
		}, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
	) {
		content(it)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseBottomSheet(onClick: () -> Unit) {
	var openBottomSheet by rememberSaveable { mutableStateOf(false) }
	val sheetState = rememberModalBottomSheetState()

	ListItem(
		headlineContent = { Text(text = "Legal") },
		modifier = Modifier.clickable { openBottomSheet = true }
	)

	if (openBottomSheet) {
		ModalBottomSheetLayout(
			onDismissRequest = { openBottomSheet = false },
			sheetState = sheetState
		) {
			LicenseText(onClick)
		}
	}
}

@Composable
fun LicenseText(onClick: () -> Unit) {
	val annotatedLicenseText = buildAnnotatedString {
		val baseStyle = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
		withStyle(style = baseStyle) {
			append("Copyright © Karthik Alapati\n\n")
			append("This application comes with absolutely no warranty. See the")
		}

		pushStringAnnotation(tag = "lic3", annotation = "link")
		withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
			append(" Apache-2.0 ")
		}
		pop()

		withStyle(style = baseStyle) {
			append("License for details.")
		}
	}
	ClickableText(
		text = annotatedLicenseText,
		style = MaterialTheme.typography.labelLarge,
		onClick = { onClick() },
		modifier = Modifier
			.padding(16.dp)
			.padding(bottom = 16.dp)
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetLayout(
	onDismissRequest: () -> Unit, sheetState: SheetState, sheetContent: @Composable () -> Unit,
) {
	val coroutineScope = rememberCoroutineScope()
	BackHandler(enabled = sheetState.isVisible) {
		coroutineScope.launch { sheetState.hide() }
	}
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
		content = { sheetContent() }
	)
}

