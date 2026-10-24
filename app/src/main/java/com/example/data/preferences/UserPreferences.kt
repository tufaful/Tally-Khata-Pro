package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode
import com.example.domain.model.AutoBackupSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tally_khata_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_ACTIVE_KHATA_ID = longPreferencesKey("active_khata_id")
        val KEY_BUSINESS_NAME = stringPreferencesKey("business_name")
        val KEY_OWNER_NAME = stringPreferencesKey("owner_name")
        val KEY_PHONE = stringPreferencesKey("phone")
        val KEY_ADDRESS = stringPreferencesKey("address")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val KEY_PIN_CODE = stringPreferencesKey("pin_code")
        val KEY_AUTO_BACKUP = stringPreferencesKey("auto_backup")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val activeKhataId: Flow<Long> = context.dataStore.data.map { it[KEY_ACTIVE_KHATA_ID] ?: 1L }
    val businessName: Flow<String> = context.dataStore.data.map { it[KEY_BUSINESS_NAME] ?: "আমার দোকান" }
    val ownerName: Flow<String> = context.dataStore.data.map { it[KEY_OWNER_NAME] ?: "" }
    val phone: Flow<String> = context.dataStore.data.map { it[KEY_PHONE] ?: "" }
    val address: Flow<String> = context.dataStore.data.map { it[KEY_ADDRESS] ?: "" }
    val language: Flow<AppLanguage> = context.dataStore.data.map {
        if (it[KEY_LANGUAGE] == "en") AppLanguage.ENGLISH else AppLanguage.BANGLA
    }
    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map {
        when (it[KEY_THEME]) {
            "light" -> AppThemeMode.LIGHT
            "dark" -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    }
    val privacyMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_PRIVACY_MODE] ?: false }
    val pinEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_PIN_ENABLED] ?: false }
    val pinCode: Flow<String> = context.dataStore.data.map { it[KEY_PIN_CODE] ?: "" }
    val autoBackupSchedule: Flow<AutoBackupSchedule> = context.dataStore.data.map {
        when (it[KEY_AUTO_BACKUP]) {
            "daily" -> AutoBackupSchedule.DAILY
            "weekly" -> AutoBackupSchedule.WEEKLY
            "monthly" -> AutoBackupSchedule.MONTHLY
            else -> AutoBackupSchedule.OFF
        }
    }
    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    suspend fun setActiveKhataId(id: Long) {
        context.dataStore.edit { it[KEY_ACTIVE_KHATA_ID] = id }
    }

    suspend fun setBusinessDetails(name: String, owner: String, phone: String, address: String) {
        context.dataStore.edit {
            it[KEY_BUSINESS_NAME] = name
            it[KEY_OWNER_NAME] = owner
            it[KEY_PHONE] = phone
            it[KEY_ADDRESS] = address
        }
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang.code }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit {
            it[KEY_THEME] = when (mode) {
                AppThemeMode.LIGHT -> "light"
                AppThemeMode.DARK -> "dark"
                AppThemeMode.SYSTEM -> "system"
            }
        }
    }

    suspend fun setPrivacyMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PRIVACY_MODE] = enabled }
    }

    suspend fun setPin(pin: String, enabled: Boolean) {
        context.dataStore.edit {
            it[KEY_PIN_CODE] = pin
            it[KEY_PIN_ENABLED] = enabled
        }
    }

    suspend fun setAutoBackup(schedule: AutoBackupSchedule) {
        context.dataStore.edit {
            it[KEY_AUTO_BACKUP] = when (schedule) {
                AutoBackupSchedule.DAILY -> "daily"
                AutoBackupSchedule.WEEKLY -> "weekly"
                AutoBackupSchedule.MONTHLY -> "monthly"
                AutoBackupSchedule.OFF -> "off"
            }
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

}
