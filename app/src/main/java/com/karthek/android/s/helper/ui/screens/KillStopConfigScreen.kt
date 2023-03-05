package com.karthek.android.s.helper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.karthek.android.s.helper.CommonScaffold
import com.karthek.android.s.helper.R
import com.karthek.android.s.helper.state.accessibilityServiceEnabled
import com.karthek.android.s.helper.state.killStopSensitivity


@Composable
fun KillStopConfigScreen(onEnableClick: () -> Unit) {
	CommonScaffold(name = "Kill Stop", onBackClick = {}) {
		Column(
			modifier = Modifier
				.padding(it)
				.padding(16.dp)
		) {
			KillStopMessage(it, onEnableClick)
			SensitivityComponent()
		}
	}
}

@Composable
fun KillStopMessage(paddingValues: PaddingValues, onClickEnable: () -> Unit) {
	Column {
		Text(stringResource(R.string.ks_msg))
		Spacer(modifier = Modifier.height(32.dp))
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			if (!accessibilityServiceEnabled) {
				Text("Click ENABLE to open Accessibility screen")
				Spacer(modifier = Modifier.height(32.dp))
				Button(onClick = onClickEnable) {
					Text("ENABLE")
				}
			}
		}
	}
}

@Composable
fun SensitivityComponent() {
	var sliderPosition by remember { mutableStateOf(killStopSensitivity.toFloat()) }
	Column(modifier = Modifier.padding(top = 32.dp)) {
		Row {
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