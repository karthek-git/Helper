package com.karthek.android.s.helper.ui

import android.content.*
import android.content.Intent.ACTION_CREATE_SHORTCUT
import android.content.Intent.ACTION_SEND
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.karthek.android.s.helper.BuildConfig
import com.karthek.android.s.helper.SettingsActivity
import com.karthek.android.s.helper.state.db.App
import com.karthek.android.s.helper.ui.components.MemUsage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainActivityView(
	viewModel: AppListViewModel,
	saveAppCallback: (String, String) -> Unit,
	uninstallCallback: (Intent) -> Unit
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
			x = 0,
			y = toolbarOffsetHeightPx.value.roundToInt()
		)
	}
	var sheetNav by viewModel.sheetNav
	val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
	val scope = rememberCoroutineScope()
	var selectedApp by viewModel.selectedApp
	BackHandler(sheetState.isVisible) {
		scope.launch { sheetState.hide() }
	}
	ModalBottomSheetLayout(
		sheetState = sheetState,
		sheetShape = RoundedCornerShape(8.dp),
		scrimColor = if (MaterialTheme.colors.isLight) {
			MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
		} else {
			MaterialTheme.colors.surface.copy(alpha = 0.5f)
		},
		sheetContent = {
			BottomSheetContent(
				nav = sheetNav,
				app = selectedApp,
				saveAppCallback = saveAppCallback,
				uninstallCallback = uninstallCallback
			) {
				if (it == 0) {
					scope.launch { sheetState.hide() }
				} else {
					sheetNav = 1
				}
			}
		}
	) {
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
				.navigationBarsPadding(bottom = false)
		) {
			AppViewFragmentView(
				viewModel = viewModel,
				bottomSheetCallback = {
					scope.launch {
						sheetNav = 0
						selectedApp = it
						sheetState.show()
					}
				},
				modifier = scrollModifier.padding(it)
			)
		}
	}
}

@Composable
fun TopBar(viewModel: AppListViewModel, modifier: Modifier) {
	TopAppBar(
		contentPadding = PaddingValues(vertical = 8.dp),
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colors.surface,
		modifier = modifier,
		content = { SearchBar(viewModel, Modifier.padding(horizontal = 8.dp)) }
	)
}

@Composable
fun SearchBar(viewModel: AppListViewModel, modifier: Modifier) {
	val textInputService = LocalTextInputService.current
	val focusHandler = LocalFocusManager.current
	val focusCancel = {
		textInputService?.hideSoftwareKeyboard()
		focusHandler.clearFocus()
	}
	Card(
		shape = RoundedCornerShape(12.dp),
		elevation = 8.dp,
		modifier = modifier.padding(2.dp)
		//.border(3.dp, MaterialTheme.colors.surface, RoundedCornerShape(12.dp))
	) {
		OutlinedTextField(
			value = viewModel.query,
			onValueChange = { viewModel.search(it) },
			keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
			keyboardActions = KeyboardActions(onAny = { focusCancel() }),
			singleLine = true,
			placeholder = { Text(text = "Search apps") },
			leadingIcon = {
				IconButton(
					onClick = {
						focusCancel()
						viewModel.search("")
					},
					enabled = viewModel.query.isNotEmpty()
				) {
					Icon(
						imageVector = if (viewModel.query.isEmpty()) Icons.Outlined.Search else Icons.Outlined.ArrowBack,
						contentDescription = "",
						modifier = Modifier.padding(start = 16.dp, end = 8.dp),
						tint = MaterialTheme.colors.onSurface
					)
				}
			},
			trailingIcon = {
				val context = LocalContext.current
				IconButton(onClick = {
					context.startActivity(Intent(context, SettingsActivity::class.java))
				}) {
					Icon(
						imageVector = Icons.Outlined.MoreVert,
						contentDescription = "",
						modifier = Modifier
							.padding(start = 8.dp, end = 16.dp)
					)
				}
			},
			colors = TextFieldDefaults.textFieldColors(
				backgroundColor = Color.Transparent,
				focusedIndicatorColor = Color.Transparent,
				unfocusedIndicatorColor = Color.Transparent,
				disabledIndicatorColor = Color.Transparent,
				leadingIconColor = MaterialTheme.colors.onSurface,
				trailingIconColor = MaterialTheme.colors.onSurface,
			),
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@Composable
fun AppViewFragmentView(
	viewModel: AppListViewModel,
	bottomSheetCallback: (App) -> Unit,
	modifier: Modifier = Modifier
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
	viewModel: AppListViewModel,
	bottomSheetCallback: (App) -> Unit,
	modifier: Modifier
) {
	val appList = viewModel.appData
	if (viewModel.loading) {
		CircularProgressIndicator(
			modifier = modifier
				.size(64.dp)
				.wrapContentSize(Alignment.Center),
			strokeWidth = 4.dp
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
	onCheck: (App, Boolean) -> Unit
) {
	LazyColumn(
		contentPadding = rememberInsetsPaddingValues(
			insets = LocalWindowInsets.current.navigationBars,
			applyStart = false,
			applyEnd = false,
			additionalTop = 140.dp,
			additionalBottom = 80.dp
		),
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
		Text(
			text = if (showSystem) "HIDE SYSTEM" else "SHOW SYSTEM",
			color = MaterialTheme.colors.primary,
			style = MaterialTheme.typography.subtitle2,
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
	callback: (Int) -> Unit
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
	callback: (Int) -> Unit
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
			//callback(0)
		}
		AppOptionsItem(icon = Icons.Outlined.Share, text = "Share") {
			val intent = Intent(ACTION_SEND)
			val f = File(app.applicationInfo!!.publicSourceDir)
			intent.putExtra(
				Intent.EXTRA_STREAM,
				FileProvider.getUriForFile(
					context,
					"${BuildConfig.APPLICATION_ID}.fileprovider",
					f,
					"${app.label}.apk"
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
		if (app.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) ?: 0 != 1) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppOptionsItem(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
	ListItem(
		icon = { Icon(icon, contentDescription = text) },
		text = { Text(text, fontWeight = FontWeight.SemiBold) },
		modifier = Modifier.clickable(onClick = onClick)
	)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppActivities(app: App) {
	val context = LocalContext.current
	val pm = context.packageManager
	val activities = app.packageInfo?.activities
	if (activities == null) {
		Text(
			text = "No Activities", modifier = Modifier
				.padding(32.dp)
				.navigationBarsPadding()
		)
	} else {
		LazyColumn(
			contentPadding = rememberInsetsPaddingValues(
				insets = LocalWindowInsets.current.navigationBars,
			),
			modifier = Modifier.padding(vertical = 8.dp)
		) {
			items(activities) {
				ListItem(icon = {
					/*Drawable(
						it.loadIcon(pm),
						modifier = Modifier.requiredSize(32.dp)
					)*/
					Image(
						painter = rememberImagePainter(data = it),
						contentDescription = "",
						modifier = Modifier.requiredSize(40.dp)
					)
				}, secondaryText = {
					SelectionContainer {
						Text(text = it.name)
					}
				}, trailing = {
					if (it.exported) {
						IconButton(onClick = { createShortcut(context, it) }) {
							Icon(
								imageVector = Icons.Outlined.AddToHomeScreen,
								contentDescription = "",
								modifier = Modifier
							)
						}
					}
				}, modifier = Modifier.clickable {
					val intent = Intent()
					intent.component = ComponentName(it.packageName, it.name)
					intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
					try {
						context.startActivity(intent)
					} catch (e: Exception) {
						Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
					}
				}) {
					Text(
						text = it.loadLabel(pm).toString(),
						fontWeight = FontWeight.SemiBold,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		}
	}
}

fun createShortcut(context: Context, activityInfo: ActivityInfo) {
	if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
		val pm = context.packageManager
		val intent = Intent(ACTION_CREATE_SHORTCUT)
		val label = activityInfo.loadLabel(pm)
			.let { if (it.isEmpty()) activityInfo.applicationInfo.loadLabel(pm) else it }
		if (label.isEmpty()) return
		intent.component = ComponentName(activityInfo.packageName, activityInfo.name)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
		val shortcutInfo = try {
			ShortcutInfoCompat.Builder(context, activityInfo.name)
				.setIntent(intent)
				.setShortLabel(label)
				.setIcon(IconCompat.createWithBitmap(activityInfo.loadIcon(pm).toBitmap()))
				.build()
		} catch (e: Exception) {
			return
		}
		if (ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)) {
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
				Toast.makeText(context, "Shortcut added to Home screen", Toast.LENGTH_SHORT).show()
			}
		} else {
			Toast.makeText(context, "Shortcuts not supported by launcher", Toast.LENGTH_SHORT)
				.show()
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppView(app: App, onCheck: (App, Boolean) -> Unit, onClick: (App) -> Unit) {
	val context = LocalContext.current
	Row(modifier = Modifier
		.combinedClickable(
			onClick = { onClick(app) },
			onLongClick = { onAppLongClick(context, app) }
		)
		.padding(4.dp)) {
		var checked by app.isSelected
		/* val scope = rememberCoroutineScope()
		 val icon: MutableState<Drawable?> = remember { mutableStateOf(null) }
		 scope.launch {
			 icon.value = withContext(Dispatchers.Default) {
				 app.applicationInfo!!.loadIcon(pm)
			 }
		 }

		 Box(
			 modifier = Modifier
				 .padding(8.dp)
				 .requiredSize(48.dp)
				 .drawBehind {
					 drawIntoCanvas { icon.value?.let { it1 -> drawablePainter(it1, it, size) } }
				 }
		 )*/
		Image(
			painter = rememberImagePainter(data = app.packageInfo),
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
				style = MaterialTheme.typography.body2,
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
				.padding(16.dp)
				.align(Alignment.CenterVertically),
			colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
		)

	}
}

fun onAppLongClick(context: Context, app: App) {
	val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	val clip: ClipData = ClipData.newPlainText(app.label, app.packageName)
	clipboard.setPrimaryClip(clip)
	Toast.makeText(context, "${app.packageName} copied to clipboard", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KillFab(extended: Boolean = false, onClick: () -> Unit) {
	var isPressed by remember { mutableStateOf(false) }
	val transition = updateTransition(targetState = isPressed, label = "")
	val scale by transition.animateFloat(label = "") { if (it) 0.9f else 1f }
	val modifier =
		Modifier.navigationBarsPadding().run { if (isPressed) scale(scale) else this }
	val coroutineScope = rememberCoroutineScope()
	FloatingActionButton(
		onClick = {
			coroutineScope.launch {
				isPressed = true
				onClick()
				delay(200)
				isPressed = false
			}
		},
		modifier = modifier.padding(8.dp)
	) {
		Row(modifier = Modifier.padding(horizontal = 16.dp)) {
			Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = "Kill")
			AnimatedVisibility(extended) {
				Text(
					text = "Kill all",
					modifier = Modifier.padding(start = 8.dp, top = 3.dp, end = 4.dp)
				)
			}
		}
	}
}