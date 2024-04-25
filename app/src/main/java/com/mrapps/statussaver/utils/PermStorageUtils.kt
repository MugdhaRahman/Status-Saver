package com.mrapps.statussaver.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermStorageUtils(private val activity: Activity) {

    // Callback interface to inform of permission changes
    interface PermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

    private var callback: PermissionCallback? = null

    fun setPermissionCallback(callback: PermissionCallback) {
        this.callback = callback
    }

    companion object {
        const val STORAGE_REQUEST_CODE = 1402

        fun isStoragePermissionGranted(activity: Activity): Boolean {
            return ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val listOfPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun askStoragePermission() {
        ActivityCompat.requestPermissions(
            activity, listOfPermissions, STORAGE_REQUEST_CODE
        )
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                callback?.onPermissionGranted()
            } else {
                callback?.onPermissionDenied()
            }
        }
    }
}
