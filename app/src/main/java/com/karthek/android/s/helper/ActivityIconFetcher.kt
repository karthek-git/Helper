package com.karthek.android.s.helper

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.UserHandle
import androidx.annotation.Px
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.os.UserHandleCompat
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.request.Options
import me.zhanghai.android.appiconloader.iconloaderlib.BaseIconFactory
import me.zhanghai.android.appiconloader.iconloaderlib.BitmapInfo
import java.util.concurrent.ConcurrentLinkedQueue

class AppIconFetcher(
	private val appIconLoader: AppIconLoader,
	private val applicationInfo: ApplicationInfo
) : Fetcher {

	override suspend fun fetch(): FetchResult {
		return DrawableResult(appIconLoader.loadIcon(applicationInfo), true, DataSource.DISK)
	}

	class Factory(private val appIconLoader: AppIconLoader) : Fetcher.Factory<PackageInfo> {
		override fun create(
			data: PackageInfo,
			options: Options,
			imageLoader: ImageLoader
		): Fetcher {
			return AppIconFetcher(appIconLoader, data.applicationInfo)
		}
	}

}

class ActivityIconFetcher(
	private val appIconLoader: AppIconLoader,
	private val activityInfo: ActivityInfo
) : Fetcher {

	override suspend fun fetch(): FetchResult {
		return DrawableResult(appIconLoader.loadIcon(activityInfo), true, DataSource.DISK)
	}

	class Factory(private val appIconLoader: AppIconLoader) : Fetcher.Factory<ActivityInfo> {
		override fun create(
			data: ActivityInfo,
			options: Options,
			imageLoader: ImageLoader
		): Fetcher {
			return ActivityIconFetcher(appIconLoader, data)
		}

	}
}

class AppIconKeyer : Keyer<PackageInfo> {
	override fun key(data: PackageInfo, options: Options): String {
		return "${data.packageName}:${PackageInfoCompat.getLongVersionCode(data)}"
	}
}

class ActivityIconKeyer : Keyer<ActivityInfo> {
	override fun key(data: ActivityInfo, options: Options): String {
		return data.packageName + data.iconResource
	}
}


class AppIconLoader(
	@Px private val iconSize: Int,
	private val shrinkNonAdaptiveIcons: Boolean,
	private val context: Context
) {
	private val iconFactoryPool: ConcurrentLinkedQueue<IconFactory> = ConcurrentLinkedQueue()

	fun loadIcon(applicationInfo: ApplicationInfo): BitmapDrawable {
		val icon: Drawable = applicationInfo.loadIcon(context.packageManager)
		val userHandle = UserHandleCompat.getUserHandleForUid(applicationInfo.uid)
		return loadIcon(icon, userHandle)
	}

	fun loadIcon(activityInfo: ActivityInfo): BitmapDrawable {
		val icon: Drawable = activityInfo.loadIcon(context.packageManager)
		val userHandle = UserHandleCompat.getUserHandleForUid(activityInfo.applicationInfo.uid)

		return loadIcon(icon, userHandle)
	}

	private fun loadIcon(icon: Drawable, userHandle: UserHandle): BitmapDrawable {
		val iconFactory: IconFactory = iconFactoryPool.poll() ?: IconFactory(iconSize, context)
		val bitmap = try {
			iconFactory.createBadgedIconBitmap(icon, userHandle, shrinkNonAdaptiveIcons, false).icon
		} finally {
			iconFactoryPool.offer(iconFactory)
		}
		return BitmapDrawable(context.resources, bitmap)
	}

	private class IconFactory(@Px iconBitmapSize: Int, context: Context) :
		BaseIconFactory(context, context.resources.configuration.densityDpi, iconBitmapSize, true) {
		private val mTempScale = FloatArray(1)
		fun createBadgedIconBitmap(
			icon: Drawable, user: UserHandle?,
			shrinkNonAdaptiveIcons: Boolean,
			isInstantApp: Boolean
		): BitmapInfo {
			return super.createBadgedIconBitmap(
				icon, user, shrinkNonAdaptiveIcons, isInstantApp,
				mTempScale
			)
		}
	}
}