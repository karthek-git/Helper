package com.karthek.android.s.helper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.karthek.android.s.helper.CommonScaffold
import com.karthek.android.s.helper.R
import com.karthek.android.s.helper.state.Prefs
import com.karthek.android.s.helper.state.accessibilityServiceEnabled
import com.karthek.android.s.helper.state.killStopSensitivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun KillStopConfigScreenContent(
	prefs: Prefs,
	onEnableClick: () -> Unit = {},
	onBackClick: () -> Unit = {},
) {
	CommonScaffold(name = "Kill Stop", onBackClick = onBackClick) {
		Column(
			modifier = Modifier
				.padding(it)
				.padding(horizontal = 16.dp)
				.verticalScroll(rememberScrollState())
		) {
			KillStopMessage(prefs, onEnableClick)
		}
	}
}

@Composable
fun KillStopMessage(prefs: Prefs, onClickEnable: () -> Unit) {
	val checked by prefs.prefsFlow.collectAsState(false)
	var mAccessibilityServiceEnabled by remember { mutableStateOf(accessibilityServiceEnabled) }
	val coroutineScope = rememberCoroutineScope()
	val lifecycle = LocalLifecycleOwner.current

	LaunchedEffect(lifecycle) {
		lifecycle.lifecycleScope.launch {
			lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
				mAccessibilityServiceEnabled = accessibilityServiceEnabled
			}
		}
	}

	Column {
		Text(stringResource(R.string.ks_msg))
		Spacer(modifier = Modifier.height(32.dp))
		Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
			Checkbox(
				checked = checked,
				onCheckedChange = {
					coroutineScope.launch {
						prefs.onAccessibilityConsentChange(it)
					}
				},
				modifier = Modifier.padding(2.dp)
			)
			Text("I agree to register this App as an Accessibility Service")
		}
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			if (checked && !mAccessibilityServiceEnabled) {
				//Text("By clicking ENABLE, you agree to register this App as an Accessibility Service")
				Spacer(modifier = Modifier.height(16.dp))
				Text("Click ENABLE to open Accessibility screen to enable the Kill Stop service")
				Spacer(modifier = Modifier.height(32.dp))
				Button(onClick = onClickEnable) {
					Text("ENABLE")
				}
			}
			if (mAccessibilityServiceEnabled) {
				SensitivityComponent()
				Text("Click DISABLE to open Accessibility screen to disable the Kill Stop service")
				Spacer(modifier = Modifier.height(32.dp))
				Button(onClick = onClickEnable) {
					Text("DISABLE")
				}
			}
		}
	}
}

@Composable
fun SensitivityComponent() {
	var sliderPosition by remember { mutableFloatStateOf(killStopSensitivity.toFloat()) }
	Column(modifier = Modifier.padding(vertical = 32.dp)) {
		Row(modifier = Modifier.padding(bottom = 8.dp)) {
			Text(text = "Kill Stop Time Sensitivity", style = MaterialTheme.typography.labelLarge)
			Spacer(modifier = Modifier.width(32.dp))
			Text(text = sliderPosition.toString(), style = MaterialTheme.typography.bodyMedium)
		}
		Slider(
			modifier = Modifier.semantics { contentDescription = "Localized Description" },
			value = sliderPosition,
			onValueChange = { sliderPosition = it },
			valueRange = 100f..500f,
			onValueChangeFinished = {
				killStopSensitivity = sliderPosition.toLong()
			},
			steps = 7
		)
	}
}

@HiltViewModel
class KillStopConfigViewModel @Inject constructor(val prefs: Prefs) :
	ViewModel() {

}