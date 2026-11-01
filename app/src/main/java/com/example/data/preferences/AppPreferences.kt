package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.remote.AppsScriptBackendTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pramanik_bari_prefs")

class AppPreferences(private val context: Context) {
    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_APPS_SCRIPT_URL = stringPreferencesKey("apps_script_url")
        val KEY_LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
        val KEY_INTRO_SHOWN = booleanPreferencesKey("intro_shown")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "bn" // Default Bengali
    }

    val userRoleFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ROLE] ?: "UNRESTRICTED"
    }

    val appsScriptUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val saved = prefs[KEY_APPS_SCRIPT_URL]
        if (saved.isNullOrBlank()) AppsScriptBackendTemplate.DEFAULT_WEB_APP_URL else saved
    }

    val lastSyncTimeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_SYNC_TIME] ?: ""
    }

    val introShownFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_INTRO_SHOWN] ?: false
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    suspend fun setAppsScriptUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APPS_SCRIPT_URL] = url.trim()
        }
    }

    suspend fun setLastSyncTime(timeStr: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SYNC_TIME] = timeStr
        }
    }

    suspend fun setIntroShown(shown: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INTRO_SHOWN] = shown
        }
    }
}
