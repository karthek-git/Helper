package com.karthek.android.s.helper.state

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "prefs")

val ACCESSIBILITY_CONSENT_KEY = booleanPreferencesKey("accessibility_consent")
val KILL_STOP_SENSITIVITY_KEY = longPreferencesKey("kill_stop_sensitivity")

@Singleton
class Prefs @Inject constructor(@ApplicationContext context: Context) {

	private val dataStore = context.dataStore

	val prefsFlow = dataStore.data.map { prefs ->
		prefs[ACCESSIBILITY_CONSENT_KEY] == true
	}

	val sensitivityFlow = dataStore.data.map { prefs ->
		prefs[KILL_STOP_SENSITIVITY_KEY] ?: 100L
	}

	suspend fun onAccessibilityConsentChange(showHidden: Boolean) {
		dataStore.edit {
			it[ACCESSIBILITY_CONSENT_KEY] = showHidden
		}
	}

	suspend fun onSensitivityChange(sensitivity: Long) {
		dataStore.edit {
			it[KILL_STOP_SENSITIVITY_KEY] = sensitivity
		}
	}
}
