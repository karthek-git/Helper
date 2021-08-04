package com.karthek.android.s.helper.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.roundToInt

@Composable
fun Drawable(drawable: Drawable, modifier: Modifier = Modifier) {
    Box(modifier = modifier.drawBehind { drawIntoCanvas { drawablePainter(drawable, it, size) } })
}


fun drawablePainter(drawable: Drawable, canvas: Canvas, size: Size) {
    drawable.let {
        it.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
        it.draw(canvas.nativeCanvas)
    }
}
