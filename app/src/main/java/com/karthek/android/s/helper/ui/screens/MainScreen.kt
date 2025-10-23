package com.karthek.android.s.helper.ui.screens

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.karthek.android.s.helper.KillStopConfigScreen
import com.karthek.android.s.helper.LicensesContent
import com.karthek.android.s.helper.SettingsScreen
import com.karthek.android.s.helper.ui.AppListViewModel
import com.karthek.android.s.helper.ui.MainActivityView
import com.karthek.android.s.helper.ui.components.loadInterstitialAd
import com.karthek.android.s.helper.ui.screens.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	viewModel: AppListViewModel,
	saveAppCallback: (String, String) -> Unit,
	uninstallCallback: (Intent) -> Unit,
) {
	val backStack = rememberNavBackStack(Screen.Home)
	val onBackClick = { backStack.removeLastOrNull(); Unit }

	val context = LocalContext.current
	var interstitialAd: InterstitialAd? = null
	val interstitialAdLoader = {
		loadInterstitialAd(
			context,
			adLoadedCallback = { interstitialAd = it },
			adCompleteCallback = { interstitialAd = null }
		)
	}
	val activity = LocalActivity.current
	val showInterstitialAd = {
		interstitialAd?.show(activity!!) ?: interstitialAdLoader()
	}
	LaunchedEffect(context) {
		interstitialAdLoader()
	}

	NavDisplay(
		backStack = backStack,
		entryDecorators = listOf(
			rememberSceneSetupNavEntryDecorator(),
			rememberSavedStateNavEntryDecorator(),
			rememberViewModelStoreNavEntryDecorator()
		),
		transitionSpec = {
			slideInHorizontally(initialOffsetX = { it }) togetherWith
					slideOutHorizontally(targetOffsetX = { -it })
		},
		popTransitionSpec = {
			slideInHorizontally(initialOffsetX = { -it }) togetherWith
					slideOutHorizontally(targetOffsetX = { it })
		},
		predictivePopTransitionSpec = {
			slideInHorizontally(initialOffsetX = { -it }) togetherWith
					slideOutHorizontally(targetOffsetX = { it })
		},
		entryProvider = entryProvider {
			entry<Screen.Home> {
				MainActivityView(
					viewModel = viewModel,
					saveAppCallback = saveAppCallback,
					uninstallCallback = uninstallCallback,
					onMoreClick = {
						backStack.add(Screen.Settings)
						showInterstitialAd()
					}
				)
			}
			entry<Screen.Btm> {
				ModalBottomSheet(onDismissRequest = onBackClick) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.height(80.dp)
					) { }
				}
			}
			entry<Screen.KillStopConfigScreen> {
				KillStopConfigScreen(
					onBackClick = onBackClick,
					viewModel = hiltViewModel()
				)
			}
			entry<Screen.Settings> {
				SettingsScreen(
					onBackClick = onBackClick,
					onKillStopConfigClick = {
						backStack.add(Screen.KillStopConfigScreen)
						showInterstitialAd()
					},
					onLicensesClick = { backStack.add(Screen.Licenses) })
			}
			entry<Screen.Licenses> { LicensesContent(onBackClick = onBackClick) }
		}
	)
}

