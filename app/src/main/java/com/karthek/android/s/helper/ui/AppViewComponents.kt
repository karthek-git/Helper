package com.karthek.android.s.helper.ui

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AddToHomeScreen
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Launch
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.karthek.android.s.helper.BuildConfig
import com.karthek.android.s.helper.ShortcutActivity
import com.karthek.android.s.helper.createShortcut
import com.karthek.android.s.helper.onAppLongClick
import com.karthek.android.s.helper.state.db.App
import com.karthek.android.s.helper.ui.components.MemUsageComponent
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
	onMoreClick: () -> Unit,
) {
	val toolbarHeightPx = with(LocalDensity.current) { 72.dp.roundToPx().toFloat() }
	val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
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

//	BackHandler(sheetState.isVisible) {
//		closeBottomSheet()
//	}

	LaunchedEffect(key1 = sheetState.isVisible) {
		if (!sheetState.isVisible) openSheet = false
	}

	val context = LocalContext.current

	Scaffold(
		topBar = { TopBar(viewModel, scrollModifier, onMoreClick) },
		floatingActionButton = {
			KillFab(
				extended = toolbarOffsetHeightPx.floatValue.roundToInt() == 0,
				onClick = {
					context.startActivity(Intent(context, ShortcutActivity::class.java))
				}
			)
		},
		modifier = Modifier
			.nestedScroll(nestedScrollConnection)
			.statusBarsPadding()
	) { paddingValues ->
		AppViewFragmentView(
			viewModel = viewModel, bottomSheetCallback = {
				scope.launch {
					sheetNav = "0"
					selectedApp = it
					openSheet = true
					sheetState.show()
				}
			}, modifier = scrollModifier.padding(paddingValues)
		)

	}

	if (openSheet) {
		ModalBottomSheet(
			onDismissRequest = { openSheet = false },
			sheetState = sheetState,
			scrimColor = if (!isSystemInDarkTheme()) {
				MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
			} else {
				Color.Black.copy(alpha = 0.5f)
			},
			containerColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth()
		) {
			val navController = rememberNavController()
			BottomSheetContent(
				navController = navController,
				app = selectedApp,
				saveAppCallback = saveAppCallback,
				uninstallCallback = uninstallCallback
			) {
				if (it == "0") {
					closeBottomSheet()
				} else {
					sheetNav = "1"
					navController.navigate("1")
				}
			}
		}
	}
}

@Composable
fun TopBar(viewModel: AppListViewModel, modifier: Modifier, onMoreClick: () -> Unit) {
	SearchBar(
		viewModel,
		modifier
			.padding(
				WindowInsets.navigationBars
					.only(WindowInsetsSides.Horizontal)
					.asPaddingValues()
			)
			.padding(horizontal = 8.dp, vertical = 8.dp),
		onMoreClick
	)
}

@Composable
fun AppViewFragmentView(
	viewModel: AppListViewModel,
	bottomSheetCallback: (App) -> Unit,
	modifier: Modifier = Modifier,
) {
	Box {
		AppViewList(
			viewModel = viewModel,
			bottomSheetCallback = bottomSheetCallback,
			modifier = Modifier.fillMaxSize()
		)
		MemUsageComponent(flow = viewModel.memUsage, modifier = modifier.fillMaxWidth())
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
		AppViewListContent(
			appList = appList,
			showSystem = viewModel.showSystem,
			bottomSheetCallback = bottomSheetCallback,
			onShowSystem = viewModel::onShowSystem,
			onCheck = viewModel::insertApp
		)
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
			.add(WindowInsets(top = 250.dp, bottom = 80.dp))
			.asPaddingValues(),
		modifier = Modifier.fillMaxSize()
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
		TextButton(
			onClick = onShowSystem,
			modifier = Modifier
				.padding(end = 16.dp)
				.align(Alignment.End)
		) {
			Text(
				text = if (showSystem) "HIDE SYSTEM" else "SHOW SYSTEM",
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.titleSmall
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
	navController: NavHostController,
	app: App,
	saveAppCallback: (String, String) -> Unit,
	uninstallCallback: (Intent) -> Unit,
	callback: (String) -> Unit,
) {
	NavHost(
		navController = navController,
		startDestination = "0",
		enterTransition = { scaleIn(initialScale = 0.98f) },
		exitTransition = { scaleOut(targetScale = 0.98f) }
	) {
		composable(
			route = "0",

			) {
			AppOptions(app, saveAppCallback, uninstallCallback, callback)
		}
		composable(route = "1") {
			Column {
				TopAppBar(
					title = { Text("Activities") },
					navigationIcon = {
						IconButton(onClick = { navController.navigateUp() }) {
							Icon(Icons.AutoMirrored.Outlined.ArrowBack, "")
						}
					},
					windowInsets = WindowInsets(0, 0, 0, 0)
				)
				AppActivities(app)
			}
		}
	}
}

@Composable
fun AppOptions(
	app: App,
	saveAppCallback: (String, String) -> Unit,
	uninstallCallback: (Intent) -> Unit,
	callback: (String) -> Unit,
) {
	val context = LocalContext.current
	Column(modifier = Modifier.navigationBarsPadding()) {
		AppOptionsItem(icon = Icons.AutoMirrored.Outlined.Launch, text = "Launch") {
			val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
			intent?.let { context.startActivity(intent) }
			callback("0")
		}
		AppOptionsItem(icon = Icons.AutoMirrored.Outlined.List, text = "Show Activities") {
			callback("1")
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
			callback("0")
		}
		AppOptionsItem(icon = Icons.Outlined.SaveAlt, text = "Save as") {
			saveAppCallback(app.applicationInfo!!.publicSourceDir, "${app.label}.apk")
			callback("0")
		}
		AppOptionsItem(icon = Icons.Outlined.Info, text = "App Info") {
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
			intent.data = "package:${app.packageName}".toUri()
			context.startActivity(intent)
			callback("0")
		}
		if ((app.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) ?: 0) != 1) {
			AppOptionsItem(icon = Icons.Outlined.Delete, text = "Uninstall") {
				if (app.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
					val intent = Intent(Intent.ACTION_DELETE)
					intent.data = "package:${app.packageName}".toUri()
					intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
					uninstallCallback(intent)
				}
				callback("0")
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
		)
	} else {
		LazyColumn(
			contentPadding = WindowInsets.navigationBars
				.only(WindowInsetsSides.Horizontal)
				.asPaddingValues()
		) {
			items(activities) {
				ListItem(
					leadingContent = {
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
									imageVector = Icons.AutoMirrored.Outlined.AddToHomeScreen,
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
			.combinedClickable(
				onClick = { onClick(app) },
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
	val modifier = Modifier.padding(
		WindowInsets.navigationBars
			.only(WindowInsetsSides.Horizontal)
			.asPaddingValues()
	).run { if (isPressed) scale(scale) else this }
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