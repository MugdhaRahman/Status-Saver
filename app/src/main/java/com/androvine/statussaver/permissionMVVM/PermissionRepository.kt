package com.androvine.statussaver.permissionMVVM

import android.content.Context
import android.content.Intent
import android.provider.Settings

class PermissionRepository(private val context: Context) {

    fun hasNotificationPermission(): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        )
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = name.split("/")
                if (cn[0] == packageName) {
                    return true
                }
            }
        }
        return false

    }

    fun getNotificationPermissionIntent(): Intent {
        return Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    }


}