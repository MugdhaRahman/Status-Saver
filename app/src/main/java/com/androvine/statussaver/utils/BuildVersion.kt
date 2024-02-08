package com.androvine.statussaver.utils

class BuildVersion {

    companion object {
        fun isAndroidR(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R
        }
    }

}