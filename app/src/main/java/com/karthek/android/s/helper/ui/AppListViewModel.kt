package com.karthek.android.s.helper.ui

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.helper.state.AppAccess
import com.karthek.android.s.helper.state.db.App
import com.karthek.android.s.helper.state.db.AppComparator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val application: Application,
    private val repo: AppAccess
) : ViewModel() {
    private val pm: PackageManager = application.packageManager
    private val am = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    var loading by mutableStateOf(true)
    var appData = mutableStateListOf<App>()
    private var origAppData = mutableListOf<App>()
    var memUsage = flow {
        val m = ActivityManager.MemoryInfo()
        while (true) {
            emit(getMemUsage(m))
            delay(1000)
        }
    }
    var showSystem by mutableStateOf(false)
    var query by mutableStateOf("")
    private var searchJob: Job? = null
    var sheetNav = mutableStateOf(0)
    var selectedApp = mutableStateOf(App("com"))


    fun onShowSystem() {
        showSystem = !showSystem
        viewModelScope.launch { refresh() }
    }

    fun removeUninstalledApp() {
        viewModelScope.launch {
            appData.remove(selectedApp.value)
            yield()
            origAppData.remove(selectedApp.value)
        }
    }


    private suspend fun refresh() {
        loading = true
        if (origAppData.isEmpty()) loadApps()
        appData.clear()
        appData += withContext(Dispatchers.Default) {
            if (showSystem) {
                origAppData.run { if (query.isNotEmpty()) filter(::queryCompare) else this }
            } else {
                origAppData.filter {
                    var res = ((it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) == 0)
                    if (query.isNotEmpty()) res = res && queryCompare(it)
                    res
                }
            }
        }
        loading = false
    }

    private fun queryCompare(app: App): Boolean {
        return app.label!!.contains(query, true)
                || app.packageName.contains(query, true)
    }

    fun search(q: String) {
        query = q
        //if (query.length < 3) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(150)
            refresh()
        }
    }

    fun insertApp(a: App, isChecked: Boolean) = viewModelScope.launch {
        if (isChecked) repo.insert(a) else repo.delete(a)
    }

    fun killAll() {
        viewModelScope.launch {
            val a = withContext(Dispatchers.Default) {
                val m = ActivityManager.MemoryInfo()
                am.getMemoryInfo(m)
                val b = m.availMem
                origAppData.forEach { am.killBackgroundProcesses(it.packageName) }
                am.getMemoryInfo(m)
                (m.availMem - b) / 1048576
            }
            Toast.makeText(application, "Freed ${a}MB", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun loadApps() {
        origAppData += withContext(Dispatchers.Default) {
            val infoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            val iterator = infoList.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().applicationInfo == null) iterator.remove()
            }
            val sAppList = repo.sApps().map { it.packageName }
            infoList.map {
                App(
                    it,
                    it.applicationInfo.loadLabel(pm).toString(),
                    sAppList.contains(it.packageName)
                )
            }.sortedWith(AppComparator())
        }
    }

    private fun getMemUsage(m: ActivityManager.MemoryInfo) = run {
        am.getMemoryInfo(m)
        val used = m.totalMem - m.availMem
        val u = (used / m.totalMem.toFloat() * 100).toInt()
        val a = String.format(
            Locale.ENGLISH,
            "%.2f GB / %.2f GB",
            used / 1073741824f,
            m.totalMem / 1073741824f
        )
        val f = String.format(Locale.ENGLISH, "%.2f GB", m.availMem / 1073741824f)
        arrayOf(u.toString(), a, f)
    }

    init {
        viewModelScope.launch { refresh() }
    }

}

