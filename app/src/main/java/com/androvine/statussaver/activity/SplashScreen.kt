package com.androvine.statussaver.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.androvine.statussaver.R
import com.androvine.statussaver.utils.BuildVersion
import com.androvine.statussaver.utils.IntroUtils
import com.androvine.statussaver.utils.PermSAFUtils
import com.androvine.statussaver.utils.PermStorageUtils

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({

            val introUtils = IntroUtils(this)

            if (BuildVersion.isAndroidR() && introUtils.isFirstTimeLaunch()) {
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            } else if (introUtils.isFirstTimeLaunch()) {
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            } else if (BuildVersion.isAndroidR() && !PermSAFUtils.verifySAF(this)) {
                startActivity(Intent(this, Permissions::class.java))
                finish()
            } else if (!PermStorageUtils.isStoragePermissionGranted(this)) {
                startActivity(Intent(this, Permissions::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }, 2000)

    }
}