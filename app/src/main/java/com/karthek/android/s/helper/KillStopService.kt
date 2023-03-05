package com.karthek.android.s.helper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.karthek.android.s.helper.state.*

class KillStopService : AccessibilityService() {

	private var pos = 0
	private var forceStopName = "Force stop"
	private var dlgOkName = "OK"

	override fun onServiceConnected() {
		super.onServiceConnected()
		accessibilityServiceEnabled = true
		initNames()
		serviceInfo = serviceInfo.apply {
			notificationTimeout = killStopSensitivity
		}
		log("on ser con")
	}

	override fun onAccessibilityEvent(event: AccessibilityEvent) {
		log("on access event $pos $f ${event.source}")
		if (!f) return
		if (event.source == null) goBack()
		val nodeInfo = event.source ?: return
		var nodeInfoList: List<AccessibilityNodeInfo>? = null
		when (pos) {
			0 -> nodeInfoList = nodeInfo.findAccessibilityNodeInfosByText(forceStopName)
			1 -> nodeInfoList = getOk(nodeInfo)
			2 -> goBack()
		}
		log("got here 1")
		if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
			val n = nodeInfoList[0]
			if (!n.performAction(AccessibilityNodeInfo.ACTION_CLICK)) goBack()
			n.recycle()
			pos++
		} else {
			log("nodeInfoList null")
			return
		}
		nodeInfo.recycle()
	}

	override fun onInterrupt() {
		log("on interrupt")
		stopSession()
	}

	override fun onUnbind(intent: Intent): Boolean {
		log("unbind")
		accessibilityServiceEnabled = false
		return super.onUnbind(intent)
	}

	//see https://cs.android.com/android/platform/superproject/+/master:packages/apps/Settings/res/values/strings.xml
	private fun initNames() {
		val packageName = "com.android.settings"
		var res: Resources? = null
		val getName = { s: String ->
			res!!.run {
				val resId = getIdentifier(s, "string", packageName)
				if (resId == 0) throw Resources.NotFoundException()
				getString(resId)
			}
		}
		try {
			res = this.packageManager.getResourcesForApplication(packageName)
		} catch (e: PackageManager.NameNotFoundException) {
			return
		}
		try {
			forceStopName = getName("force_stop")
		} catch (_: Resources.NotFoundException) {
		}

		try {
			dlgOkName = getName("dlg_ok")
		} catch (_: Resources.NotFoundException) {
		}
	}

	private fun getOk(nodeInfo: AccessibilityNodeInfo): List<AccessibilityNodeInfo>? {
		var n = nodeInfo.findAccessibilityNodeInfosByText(dlgOkName)
		if (n.isEmpty()) {
			n = nodeInfo.findAccessibilityNodeInfosByText(getString(android.R.string.ok))
		}
		if (n.isEmpty()) {
			n = nodeInfo.findAccessibilityNodeInfosByText(forceStopName)
			n = n.filter { it.text.equals(forceStopName) }
		}
		return n
	}

	private fun goBack() {
		log("back " + performGlobalAction(GLOBAL_ACTION_BACK).toString())
		pos = 0
		nextStop(this)
	}

	private fun log(s: String) = Log.d("kill stop log", s)
}