package com.androvine.statussaver.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.androvine.statussaver.R
import com.androvine.statussaver.databinding.ActivityPermissionsBinding
import com.androvine.statussaver.utils.BuildVersion
import com.androvine.statussaver.utils.PermSAFUtils
import com.androvine.statussaver.utils.PermStorageUtils

class Permissions : AppCompatActivity() {

    private val binding: ActivityPermissionsBinding by lazy {
        ActivityPermissionsBinding.inflate(layoutInflater)
    }


    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var permSAFUtils: PermSAFUtils
    private lateinit var permStorageUtils: PermStorageUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupIntentLauncher()

        checkPermissions()

        setupPermission()

    }


    private fun checkPermissions() {

        if (BuildVersion.isAndroidR() && PermSAFUtils.verifySAF(this)) {
            allowedUI()
        } else if (PermStorageUtils.isStoragePermissionGranted(this)) {
            allowedUI()
        }
    }

    private fun setupPermission() {

        binding.btnAllow.setOnClickListener {
            if (BuildVersion.isAndroidR()) {
                goHomeAboveR()
            } else {
                goHomeBelowR()
            }
        }

        permStorageUtils.setPermissionCallback(object : PermStorageUtils.PermissionCallback {
            override fun onPermissionGranted() {
                allowedUI()
            }

            override fun onPermissionDenied() {
                deniedUI()
            }
        })


    }

    private fun setupIntentLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                permSAFUtils.addSAFPermission(result.data!!)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    permSAFUtils.showSAFDialog()
                }
            }
        }
        permSAFUtils = PermSAFUtils(this, requestPermissionLauncher)
        permStorageUtils = PermStorageUtils(this)

    }

    private fun allowedUI() {
        binding.permissionTitle.text = "Permission Granted"
        binding.permissionSubTitle.text = "We all set to save WhatsApp status"
        binding.permissionImg.setImageResource(R.drawable.ic_checkmark_permission)
        binding.permissionDescription.text = "You have granted the permission"
        binding.btnAllow.text = "Let's Go"
        binding.btnHowToAllow.visibility = View.INVISIBLE
    }

    private fun deniedUI() {
        binding.permissionTitle.text = "Permission Denied"
        binding.permissionSubTitle.text = "We need permission to save WhatsApp status"
        binding.permissionImg.setImageResource(R.drawable.permission_img)
        binding.permissionDescription.text = "Permission is required to save WhatsApp status"
        binding.btnAllow.text = "Try Again"
        binding.btnHowToAllow.visibility = View.VISIBLE
    }

    private fun goHomeAboveR() {
        if (BuildVersion.isAndroidR() && !PermSAFUtils.verifySAF(this)) {
            permSAFUtils.showSAFDialog()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun goHomeBelowR() {
        if (!PermStorageUtils.isStoragePermissionGranted(this)) {
            permStorageUtils.askStoragePermission()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permStorageUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}