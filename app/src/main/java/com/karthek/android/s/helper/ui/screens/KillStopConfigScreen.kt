package com.karthek.android.s.helper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
				.verticalScroll(rememberScrollState())
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
				Text("Click ENABLE to open Accessibility screen to enable the Kill Stop service")
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