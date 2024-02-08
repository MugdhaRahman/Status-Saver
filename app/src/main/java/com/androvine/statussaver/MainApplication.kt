package com.androvine.statussaver

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelStore
import androidx.preference.PreferenceManager
import com.androvine.statussaver.permissionMVVM.permissionModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class MainApplication : Application() {

    private val viewModelStore: ViewModelStore by lazy { ViewModelStore() }

    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(permissionModule)
        }

        // set app dark or light mode based on user preference
        val themeMode = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("pref_dark_theme", "system_default")


        when (themeMode) {
            "light_theme" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            "dark_theme" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

    }
}