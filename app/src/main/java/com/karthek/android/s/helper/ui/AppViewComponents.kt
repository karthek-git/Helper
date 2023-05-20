package com.karthek.android.s.helper.ui

import android.content.*
import android.content.Intent.ACTION_SEND
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.karthek.android.s.helper.BuildConfig
import com.karthek.android.s.helper.createShortcut
import com.karthek.android.s.helper.onAppLongClick
import com.karthek.android.s.helper.state.db.App
import com.karthek.android.s.helper.ui.components.MemUsage
import com.karthek.android.s.helper.ui.components.SearchBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityView(
	viewModel: AppListViewModel,
	saveAppCallback: (String, String) -> Unit,
	uninstallCallback: (Intent) -> Unit,
) {
	val toolbarHeightPx = with(LocalDensity.current) { 72.dp.roundToPx().toFloat() }
	val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }
	val nestedScrollConnection = remember {
		object : NestedScrollConnection {
			override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
				val delta = available.y
				val newOffset = toolbarOffsetHeightPx.value + delta
				toolbarOffsetHeightPx.value = newOffset.coerceIn(-toolbarHeightPx, 0f)
				return Offset.Zero
			}
		}
	}

	val scrollModifier = Modifier.offset {
		IntOffset(
			x = 0, y = toolbarOffsetHeightPx.value.roundToInt()
		)
	}
	var sheetNav by viewModel.sheetNav
	var openSheet by remember { mutableStateOf(false) }
	val sheetState = rememberModalBottomSheetState()
	val scope = rememberCoroutineScope()
	var selectedApp by viewModel.selectedApp

	val closeBottomSheet = {
		scope.launch { sheetState.hide() }.invokeOnCompletion {
			if (!sheetState.isVisible) {
				openSheet = false
			}
		}
	}

	BackHandler(sheetState.isVisible) {
		closeBottomSheet()
	}

	LaunchedEffect(key1 = sheetState.isVisible) {
		if (!sheetState.isVisible) openSheet = false
	}

	Scaffold(
		topBar = { TopBar(viewModel, scrollModifier) },
		floatingActionButton = {
			KillFab(
				extended = toolbarOffsetHeightPx.value.roundToInt() == 0,
				onClick = viewModel::killAll
			)
		},
		modifier = Modifier
			.nestedScroll(nestedScrollConnection)
			.statusBarsPadding()
	) {
		AppViewFragmentView(
			viewModel = viewModel, bottomSheetCallback = {
				scope.launch {
					sheetNav = 0
					selectedApp = it
					openSheet = true
					sheetState.show()
				}
			}, modifier = scrollModifier.padding(it)
		)

	}

	if (openSheet) {
		val systemBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
		ModalBottomSheet(
			onDismissRequest = { openSheet = false },
			sheetState = sheetState,
			scrimColor = if (!isSystemInDarkTheme()) {
				MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
			} else {
				Color.Black.copy(alpha = 0.5f)
			},
			containerColor = MaterialTheme.colorScheme.background, modifier = Modifier
				.fillMaxWidth()
				.offset(y = systemBarHeight)
		) {
			BottomSheetContent(
				nav = sheetNav,
				app = selectedApp,
				saveAppCallback = saveAppCallback,
				uninstallCallback = uninstallCallback
			) {
				if (it == 0) {
					closeBottomSheet()
				} else {
					sheetNav = 1
				}
			}
		}
	}
}

@Composable
fun TopBar(viewModel: AppListViewModel, modifier: Modifier) {
	SearchBar(
		viewModel,
		modifier
			.padding(
				WindowInsets.navigationBars
					.only(WindowInsetsSides.Horizontal)
					.asPaddingValues()
			)
			.padding(horizontal = 8.dp, vertical = 8.dp)
	)
}

@Composable
fun AppViewFragmentView(
	viewModel: AppListViewModel, bottomSheetCallback: (App) -> Unit, modifier: Modifier = Modifier,
) {
	Box {
		AppViewList(
			viewModel = viewModel,
			bottomSheetCallback = bottomSheetCallback,
			modifier = Modifier.fillMaxSize()
		)
		MemUsage(flow = viewModel.memUsage, modifier = modifier.fillMaxWidth())
	}
}

@Composable
fun AppViewList(
	viewModel: AppListViewModel, bottomSheetCallback: (App) -> Unit, modifier: Modifier,
) {
	val appList = viewModel.appData
	if (viewModel.loading) {
		CircularProgressIndicator(
			modifier = modifier
				.size(64.dp)
				.wrapContentSize(Alignment.Center), strokeWidth = 4.dp
		)
	} else {
		Surface {
			AppViewListContent(
				appList = appList,
				showSystem = viewModel.showSystem,
				bottomSheetCallback = bottomSheetCallback,
				onShowSystem = viewModel::onShowSystem,
				onCheck = viewModel::insertApp
			)
		}
	}
}

@Composable
fun AppViewListContent(
	appList: List<App>,
	showSystem: Boolean,
	bottomSheetCallback: (App) -> Unit,
	onShowSystem: () -> Unit,
	onCheck: (App, Boolean) -> Unit,
) {
	LazyColumn(
		contentPadding = WindowInsets.navigationBars
			.only(WindowInsetsSides.Horizontal)
			.add(WindowInsets(top = 216.dp, bottom = 80.dp))
			.asPaddingValues(),
		modifier = Modifier
			.fillMaxSize()
	) {
		item { ListHeader(showSystem, onShowSystem) }
		if (appList.isEmpty()) {
			item {
				Text(
					text = "Nothing found",
					modifier = Modifier
						.fillParentMaxSize()
						.wrapContentSize(Alignment.Center)
				)
			}
		}
		items(appList) {
			AppView(app = it, onCheck = onCheck, onClick = bottomSheetCallback)
		}
	}
}

@Composable
fun ListHeader(showSystem: Boolean, onShowSystem: () -> Unit) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = if (showSystem) "HIDE SYSTEM" else "SHOW SYSTEM",
			color = MaterialTheme.colorScheme.primary,
			style = MaterialTheme.typography.titleSmall,
			modifier = Modifier
				.padding(end = 16.dp)
				.align(Alignment.End)
				.clickable(onClick = onShowSystem)
		)
	}
}

@Composable
fun BottomSheetContent(
	nav: Int,
	app: App,
	saveAppCallback: (String, String) -> Unit,
	uninstallCallback: (Intent) -> Unit,
	callback: (Int) -> Unit,
) {
	if (nav == 0) {
		AppOptions(app, saveAppCallback, uninstallCallback, callback)
	} else {
		AppActivities(app)
	}
}

@Composable
fun AppOptions(
	app: App,
	saveAppCallback: (String, String) -> Unit,
	uninstallCallback: (Intent) -> Unit,
	callback: (Int) -> Unit,
) {
	val context = LocalContext.current
	Column(modifier = Modifier.navigationBarsPadding()) {
		AppOptionsItem(icon = Icons.Outlined.Launch, text = "Launch") {
			val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
			intent?.let { context.startActivity(intent) }
			callback(0)
		}
		AppOptionsItem(icon = Icons.Outlined.List, text = "Show Activities") {
			callback(1)
		}
		AppOptionsItem(icon = Icons.Outlined.Share, text = "Share") {
			val intent = Intent(ACTION_SEND)
			val f = File(app.applicationInfo!!.publicSourceDir)
			intent.putExtra(
				Intent.EXTRA_STREAM, FileProvider.getUriForFile(
					context, "${BuildConfig.APPLICATION_ID}.fileprovider", f, "${app.label}.apk"
				)
			)
			intent.type = "application/vnd.android.package-archive"
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			context.startActivity(Intent.createChooser(intent, null))
			callback(0)
		}
		AppOptionsItem(icon = Icons.Outlined.SaveAlt, text = "Save as") {
			saveAppCallback(app.applicationInfo!!.publicSourceDir, "${app.label}.apk")
			callback(0)
		}
		AppOptionsItem(icon = Icons.Outlined.Info, text = "App Info") {
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
			intent.data = Uri.parse("package:" + app.packageName)
			context.startActivity(intent)
			callback(0)
		}
		if ((app.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) ?: 0) != 1) {
			AppOptionsItem(icon = Icons.Outlined.Delete, text = "Uninstall") {
				if (app.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
					val intent = Intent(Intent.ACTION_DELETE)
					intent.data = Uri.parse("package:${app.packageName}")
					intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
					uninstallCallback(intent)
				}
				callback(0)
			}
		}
	}
}

@Composable
fun AppOptionsItem(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
	ListItem(
		leadingContent = { Icon(icon, contentDescription = text) },
		headlineContent = { Text(text, fontWeight = FontWeight.SemiBold) },
		colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
		modifier = Modifier.clickable(onClick = onClick)
	)
}

@Composable
fun AppActivities(app: App) {
	val context = LocalContext.current
	val pm = context.packageManager
	val activities = app.packageInfo?.activities
	if (activities == null) {
		Text(
			text = "No Activities",
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(32.dp)
				.navigationBarsPadding()
		)
	} else {
		LazyColumn(
			contentPadding = WindowInsets.navigationBars.asPaddingValues(),
			modifier = Modifier.padding(vertical = 8.dp)
		) {
			items(activities) {
				ListItem(leadingContent = {
					Image(
						painter = rememberAsyncImagePainter(model = it),
						contentDescription = "",
						modifier = Modifier.requiredSize(40.dp)
					)
				},
					supportingContent = {
						SelectionContainer {
							Text(text = it.name)
						}
					},
					trailingContent = {
						if (it.exported) {
							IconButton(onClick = {
								createShortcut(
									context, it
								)
							}) {
								Icon(
									imageVector = Icons.Outlined.AddToHomeScreen,
									contentDescription = "",
									modifier = Modifier
								)
							}
						}
					},
					colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
					modifier = Modifier.clickable {
						val intent = Intent()
						intent.component = ComponentName(it.packageName, it.name)
						intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
						try {
							context.startActivity(intent)
						} catch (e: Exception) {
							Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
						}
					},
					headlineContent = {
						Text(
							text = it.loadLabel(pm).toString(),
							fontWeight = FontWeight.SemiBold,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					})
			}
		}
	}
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppView(app: App, onCheck: (App, Boolean) -> Unit, onClick: (App) -> Unit) {
	val context = LocalContext.current
	Row(
		modifier = Modifier
			.combinedClickable(onClick = { onClick(app) },
				onLongClick = { onAppLongClick(context, app) })
			.padding(4.dp)
	) {
		var checked by app.isSelected
		Image(
			painter = rememberAsyncImagePainter(model = app.packageInfo),
			contentDescription = "",
			modifier = Modifier
				.padding(8.dp)
				.requiredSize(48.dp)
		)
		Column(
			Modifier
				.weight(1f)
				.align(Alignment.CenterVertically)
		) {
			Text(
				text = app.label!!,
				fontWeight = FontWeight.SemiBold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(start = 8.dp)
			)
			Text(
				text = app.packageName,
				modifier = Modifier
					.alpha(0.7f)
					.padding(start = 8.dp),
				style = MaterialTheme.typography.bodyMedium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
		Checkbox(
			checked = checked,
			onCheckedChange = {
				checked = it
				onCheck(app, it)
			},
			modifier = Modifier
				.padding(6.dp)
				.align(Alignment.CenterVertically),
		)

	}
}

@Composable
fun KillFab(extended: Boolean = false, onClick: () -> Unit) {
	var isPressed by remember { mutableStateOf(false) }
	val transition = updateTransition(targetState = isPressed, label = "")
	val scale by transition.animateFloat(label = "") { if (it) 0.9f else 1f }
	val modifier = Modifier.navigationBarsPadding().run { if (isPressed) scale(scale) else this }
	val coroutineScope = rememberCoroutineScope()
	FloatingActionButton(
		onClick = {
			coroutineScope.launch {
				isPressed = true
				onClick()
				delay(200)
				isPressed = false
			}
		}, modifier = modifier.padding(8.dp)
	) {
		Row(modifier = Modifier.padding(horizontal = 16.dp)) {
			Icon(
				imageVector = Icons.Default.AutoFixHigh, contentDescription = "Kill"
			)
			AnimatedVisibility(extended) {
				Text(
					text = "Kill all",
					modifier = Modifier.padding(start = 8.dp, top = 3.dp, end = 4.dp)
				)
			}
		}
	}
}