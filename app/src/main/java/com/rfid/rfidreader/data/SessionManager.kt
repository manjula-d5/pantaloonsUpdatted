package com.rfid.rfidreader.data

import android.content.Context
import android.content.SharedPreferences
import com.rfid.rfidreader.util.AppLogger

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "rfid_reader_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_STORE_ID = "store_id"
        private const val KEY_TRIAL_ROOM_ID = "trial_room_id"
        private const val KEY_TRIAL_ROOM_NAME = "trial_room_name"
    }

    var trialRoomId: Int
        get() = prefs.getInt(KEY_TRIAL_ROOM_ID, -1)
        set(value) {
            AppLogger.log("SessionManager: Updating trialRoomId to $value")
            prefs.edit().putInt(KEY_TRIAL_ROOM_ID, value).apply()
        }

    var trialRoomName: String?
        get() = prefs.getString(KEY_TRIAL_ROOM_NAME, null)
        set(value) {
            AppLogger.log("SessionManager: Updating trialRoomName to $value")
            prefs.edit().putString(KEY_TRIAL_ROOM_NAME, value).apply()
        }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) {
            AppLogger.log("SessionManager: Setting isLoggedIn to $value")
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
        }

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            AppLogger.log("SessionManager: Updating authToken (length: ${value?.length ?: 0})")
            prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()
        }

    var storeId: String?
        get() = prefs.getString(KEY_STORE_ID, null)
        set(value) {
            AppLogger.log("SessionManager: Updating storeId to $value")
            prefs.edit().putString(KEY_STORE_ID, value).apply()
        }

    fun logout() {
        AppLogger.log("SessionManager: Performing logout/clearing session")
        prefs.edit().clear().apply()
    }
}
