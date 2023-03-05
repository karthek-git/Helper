package com.karthek.android.s.helper.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.karthek.android.s.helper.SettingsActivity
import com.karthek.android.s.helper.ui.AppListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(viewModel: AppListViewModel, modifier: Modifier) {
	val softwareKeyboardController = LocalSoftwareKeyboardController.current
	val focusHandler = LocalFocusManager.current
	val focusCancel = {
		softwareKeyboardController?.hide()
		focusHandler.clearFocus()
	}
	ElevatedCard(
		shape = RoundedCornerShape(12.dp),
		elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
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
					}, enabled = viewModel.query.isNotEmpty()
				) {
					Icon(
						imageVector = if (viewModel.query.isEmpty()) Icons.Outlined.Search else Icons.Outlined.ArrowBack,
						contentDescription = "",
						tint = MaterialTheme.colorScheme.onSurface
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
					)
				}
			},
			colors = TextFieldDefaults.textFieldColors(
				containerColor = Color.Transparent,
				focusedIndicatorColor = Color.Transparent,
				unfocusedIndicatorColor = Color.Transparent,
				disabledIndicatorColor = Color.Transparent,
				focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
				focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
			),
			modifier = Modifier.fillMaxWidth()
		)
	}
}