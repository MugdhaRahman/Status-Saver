package com.mrapps.statussaver.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mrapps.statussaver.R
import com.mrapps.statussaver.constants.Constants
import com.mrapps.statussaver.databinding.ActivityPermissionsBinding
import com.mrapps.statussaver.utils.BuildVersion
import com.mrapps.statussaver.utils.PermSAFUtils
import com.mrapps.statussaver.utils.PermStorageUtils

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

        if (!isWhatsappInstalled()) {
            showWhatsappNotInstalledDialog()
        }

        setupIntentLauncher()

        checkPermissions()

        setupPermission()

        binding.btnPrivacyPolicy.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW, Uri.parse(Constants.privacyPolicyUrl)
            )
            startActivity(browserIntent)
        }
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
    }

    private fun deniedUI() {
        binding.permissionTitle.text = "Permission Denied"
        binding.permissionSubTitle.text = "We need permission to save WhatsApp status"
        binding.permissionImg.setImageResource(R.drawable.permission_img)
        binding.permissionDescription.text = "Permission is required to save WhatsApp status"
        binding.btnAllow.text = "Try Again"
    }

    private fun goHomeAboveR() {
        if (BuildVersion.isAndroidR() && !PermSAFUtils.verifySAF(this)) {
            permSAFUtils.showSAFDialog()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun goHomeBelowR() {
        if (!PermStorageUtils.isStoragePermissionGranted(this)) {
            permStorageUtils.askStoragePermission()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun isWhatsappInstalled(): Boolean {
        val pm = this.packageManager
        var appInstalled = false
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            appInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("TAG", "isWhatsappInstalled: ", e)
        }
        return appInstalled
    }

    private fun showWhatsappNotInstalledDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_whatsapp_not_installed)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val okButton = dialog.findViewById<Button>(R.id.btnOk)
        okButton.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()

    }

    override fun onResume() {
        super.onResume()
        checkPermissions()

    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permStorageUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}