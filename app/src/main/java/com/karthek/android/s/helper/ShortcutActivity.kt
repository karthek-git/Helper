package com.karthek.android.s.helper

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karthek.android.s.helper.state.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class ShortcutActivity : AppCompatActivity() {

    @Inject
    lateinit var repo: AppAccess

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopAll()
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

    private fun stopAll() {
        f = true
        if (!checkServiceStatus()) return
        setAppsToKill()
        nextStop(this)
    }


    private fun setAppsToKill() {
        val sAppList = runBlocking { repo.sApps().map { it.packageName } }
        packageManager.getInstalledApplications(0).forEach {
            if (
                (it.flags and ApplicationInfo.FLAG_STOPPED == 0)
                && (sAppList.contains(it.packageName))
                && (it.packageName != packageName)
            ) {
                appStack.push(it.packageName)
            }
        }
    }

    private fun checkServiceStatus(): Boolean {
        return if (!accessibilityServiceEnabled) {
            showAccessibilityDialog()
            accessibilityServiceEnabled
        } else {
            true
        }
    }

    private fun showAccessibilityDialog() {
        Toast.makeText(this, R.string.enable_as, Toast.LENGTH_SHORT).show()
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}