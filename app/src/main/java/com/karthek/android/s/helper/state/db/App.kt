package com.karthek.android.s.helper.state.db

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.runtime.mutableStateOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.Collator

@Entity
data class App(@PrimaryKey @ColumnInfo(name = "package_name") val packageName: String) {

    @Ignore
    var applicationInfo: ApplicationInfo? = null

    @Ignore
    var packageInfo: PackageInfo? = null

    @Ignore
    var label: String? = null

    @Ignore
    var isSelected = mutableStateOf(false)

    constructor(packageInfo: PackageInfo, label: String?, isSelected: Boolean) : this(
        packageInfo.packageName
    ) {
        this.packageInfo = packageInfo
        this.applicationInfo = packageInfo.applicationInfo
        this.label = label
        this.isSelected.value = isSelected
    }
}

class AppComparator : Comparator<App> {

    private val collator = Collator.getInstance()

    override fun compare(o1: App?, o2: App?): Int {
        if (o1 == null || o2 == null) return 0
        var result = collator.compare(o1.label, o2.label)
        if (result == 0) {
            result = o1.packageName.compareTo(o2.packageName)
        }
        return result
    }
}
