package com.karthek.android.s.helper

import android.content.*
import android.content.pm.ActivityInfo
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.karthek.android.s.helper.state.db.App

fun onAppLongClick(context: Context, app: App) {
	val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	val clip: ClipData = ClipData.newPlainText(app.label, app.packageName)
	clipboard.setPrimaryClip(clip)
	Toast.makeText(context, "${app.packageName} copied to clipboard", Toast.LENGTH_SHORT).show()
}

fun createShortcut(context: Context, activityInfo: ActivityInfo) {
	if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
		val pm = context.packageManager
		val intent = Intent(Intent.ACTION_CREATE_SHORTCUT)
		val label = activityInfo.loadLabel(pm)
			.let { it.ifEmpty { activityInfo.applicationInfo.loadLabel(pm) } }
		if (label.isEmpty()) return
		intent.component = ComponentName(activityInfo.packageName, activityInfo.name)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
		val shortcutInfo = try {
			ShortcutInfoCompat.Builder(context, activityInfo.name).setIntent(intent)
				.setShortLabel(label)
				.setIcon(IconCompat.createWithBitmap(activityInfo.loadIcon(pm).toBitmap())).build()
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