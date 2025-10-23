package com.karthek.android.s.helper.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.karthek.android.s.helper.ui.AppListViewModel.MemUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Composable
fun MemUsageComponent(flow: Flow<MemUsage>, modifier: Modifier) {
	val memUsage by flow.collectAsState(
		initial = MemUsage(0f, "0 GB / 0 GB", "0 GB"),
		context = Dispatchers.Default
	)
	Column(modifier = modifier) {
		MemUsageContent(memUsage.u, memUsage.a, memUsage.f)
		BannerAdComponentContainer()
	}
}

@Composable
fun MemUsageContent(memPercent: Float, s_used: String, s_free: String) {
	val animatedProgress by animateFloatAsState(
		targetValue = memPercent,
		animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
	)
	Card(
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp) // todo(temp fix for compose m3 car elevation color issue)
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
		modifier = Modifier.padding(8.dp)
	) {
		ConstraintLayout(
			modifier = Modifier
				.padding(16.dp)
				.fillMaxWidth()
		) {
			val (title, progress, percent, pSign, usage, used, free) = createRefs()

			Text(
				text = "Memory",
				style = MaterialTheme.typography.titleSmall,
				modifier = Modifier.constrainAs(title) {
					width = Dimension.fillToConstraints
					start.linkTo(parent.start)
					top.linkTo(parent.top)
				})
			LinearProgressIndicator(
				progress = { animatedProgress }, modifier = Modifier
					.constrainAs(progress) {
						width = Dimension.fillToConstraints
						start.linkTo(parent.start)
						end.linkTo(usage.start, 16.dp)
						top.linkTo(title.bottom, 16.dp)
					}
					.height(8.dp)
					.clip(RoundedCornerShape(4.dp)))
			Text(
				text = (memPercent * 100).fastRoundToInt().toString(),
				fontSize = 34.sp,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.constrainAs(percent) {
					top.linkTo(parent.top)
					bottom.linkTo(usage.top)
					end.linkTo(pSign.start)

				})
			Text(
				text = "%",
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.constrainAs(pSign) {
					baseline.linkTo(percent.baseline)
					end.linkTo(parent.end)
				})
			Text(
				text = "USAGE",
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