package com.karthek.android.s.helper.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Composable
fun MemUsage(flow: Flow<Array<String>>, modifier: Modifier) {
	val memUsage by flow.collectAsState(
		initial = arrayOf("0", "0 GB / 0 GB", "0 GB"),
		context = Dispatchers.Default
	)
	MemUsageContent(memUsage[0], memUsage[1], memUsage[2], modifier)
}

@Composable
fun MemUsageContent(memPercent: String, s_used: String, s_free: String, modifier: Modifier) {
	ElevatedCard(
		modifier = modifier
			.padding(8.dp),
		shape = RoundedCornerShape(8.dp),
		elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
	) {
		ConstraintLayout(
			modifier = Modifier
				.padding(16.dp)
				.fillMaxWidth()
		) {
			val (title, progress, percent, pSign, usage, used, free) = createRefs()

			Text(text = "Memory",
				style = MaterialTheme.typography.titleSmall,
				modifier = Modifier.constrainAs(title) {
					width = Dimension.fillToConstraints
					start.linkTo(parent.start)
					top.linkTo(parent.top)
				})
			LinearProgressIndicator(progress = 0.6f, modifier = Modifier
				.constrainAs(progress) {
					width = Dimension.fillToConstraints
					start.linkTo(parent.start)
					end.linkTo(usage.start, 16.dp)
					top.linkTo(title.bottom, 16.dp)
				}
				.height(8.dp)
				.clip(RoundedCornerShape(4.dp)))
			Text(text = memPercent,
				fontSize = 34.sp,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.constrainAs(percent) {
					top.linkTo(parent.top)
					bottom.linkTo(usage.top)
					end.linkTo(pSign.start)

				})
			Text(text = "%",
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.constrainAs(pSign) {
					baseline.linkTo(percent.baseline)
					end.linkTo(parent.end)
				})
			Text(text = "USAGE",
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.constrainAs(usage) {
					start.linkTo(percent.start)
					end.linkTo(pSign.end)
					bottom.linkTo(free.bottom)
				})
			Text(text = s_used, modifier = Modifier.constrainAs(used) {
				start.linkTo(parent.start)
				top.linkTo(percent.bottom, 8.dp)
			})
			Text(text = s_free, modifier = Modifier.constrainAs(free) {
				top.linkTo(used.top)
				end.linkTo(progress.end, 8.dp)
			})
		}
	}
}