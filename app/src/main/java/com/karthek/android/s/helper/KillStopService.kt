package com.karthek.android.s.helper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.karthek.android.s.helper.state.accessibilityServiceEnabled
import com.karthek.android.s.helper.state.f
import com.karthek.android.s.helper.state.nextStop
import com.karthek.android.s.helper.state.pos
import com.karthek.android.s.helper.state.stopSession

class KillStopService : AccessibilityService() {

	//private var pos = 0
	private var forceStopName = "Force stop"
	private var dlgOkName = "OK"

	override fun onServiceConnected() {
		super.onServiceConnected()
		accessibilityServiceEnabled = true
		initNames()
		serviceInfo = serviceInfo.apply {
		//	notificationTimeout = killStopSensitivity
		}
		log("on ser con")
	}

	override fun onAccessibilityEvent(event: AccessibilityEvent) {
		log("on access event $pos $f ${event.source}")
		if (!f) return
		//if (event.source == null) goBack()
		val nodeInfo = event.source ?: return
		var nodeInfoList: List<AccessibilityNodeInfo>? = null
		when (pos) {
			0 -> {
				log("Finding $forceStopName")
				nodeInfoList = nodeInfo.findAccessibilityNodeInfosByText(forceStopName)
				if (nodeInfoList.isNullOrEmpty())
					nodeInfoList = getNode(nodeInfo, "Force stop")
			}

			1 -> {
				log("Finding ok")
				nodeInfoList = getOk(nodeInfo)
			}

			2 -> {
				goBack()
			}
		}
		log("got here 1")
		if (nodeInfoList != null && nodeInfoList.isNotEmpty()) {
			val n = nodeInfoList[0]
			log("got here 2")
			val clicked=n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
			Thread.sleep(300)
			if (!clicked) {
				goBack()
			}
			n.recycle()
			pos++
		} else {
			val lll = rootInActiveWindow.findAccessibilityNodeInfosByText("Force stop")
			//logNodeTree(nodeInfo)
			log("nodeInfoList null $lll")
			pos = 0
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
		} catch (_: PackageManager.NameNotFoundException) {
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

	private fun getNode(
		nodeInfo: AccessibilityNodeInfo?,
		text: String,
	): List<AccessibilityNodeInfo>? {
		if (nodeInfo == null) return null
		if (nodeInfo.text != null && nodeInfo.text.toString().equals(text, ignoreCase = true)) {
			log("Match found: ${nodeInfo.text} ${nodeInfo.isClickable}")
			if (!nodeInfo.isClickable) return listOf(nodeInfo.parent)
			return listOf(nodeInfo)
		}

		for (i in 0 until nodeInfo.childCount) {
			val child = nodeInfo.getChild(i)
			val result = getNode(child, text)
			if (result != null) return result
		}

		return null
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
		if (n.isEmpty())
			n = getNode(nodeInfo, dlgOkName)
		return n
	}

	private fun goBack() {
		pos = 0
		log("Going back " + performGlobalAction(GLOBAL_ACTION_BACK).toString())
		Thread.sleep(300)
		nextStop(this)
	}

	private fun logNodeTree(node: AccessibilityNodeInfo, indent: Int = 0) {
		log("${" ".repeat(indent)}Text: ${node.text} | Class: ${node.className}")
		for (i in 0 until node.childCount) {
			node.getChild(i)?.let { logNodeTree(it, indent + 2) }
		}
	}

	private fun log(s: String) = Log.d("kill stop log", s)
}