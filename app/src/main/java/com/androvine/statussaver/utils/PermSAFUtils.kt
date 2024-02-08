package com.androvine.statussaver.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.UriPermission
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import com.androvine.statussaver.databinding.DialogPermissionSafBinding

class PermSAFUtils(
    private val activity: Activity,
    private val requestPermissionLauncher: ActivityResultLauncher<Intent>
) {

    companion object {

        const val permSAFPref = "permSAFPref"
        const val permSAFPrefUri = "uriSAF"

        fun verifySAF(activity: Activity): Boolean {
            val sharedPreferences: SharedPreferences = activity.getSharedPreferences(permSAFPref, 0)
            val mainUri = Uri.parse(sharedPreferences.getString(permSAFPrefUri, ""))
            val list: List<UriPermission> = activity.contentResolver.persistedUriPermissions

            for (i in list.indices) {
                val uriListString = list[i].toString()
                if (uriListString.contains(mainUri.toString())) {
                    return true
                }
            }

            return false
        }

        fun getSAFUri(context: Context): Uri {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(permSAFPref, 0)
            return Uri.parse(sharedPreferences.getString(permSAFPrefUri, ""))
        }
    }


    private val whatsappUriFolder = "primary:Android/media/com.whatsapp/WhatsApp/Media/"
    private val storageAuthority = "com.android.externalstorage.documents"
    private val androidUriStatus: Uri = DocumentsContract.buildDocumentUri(
        storageAuthority, whatsappUriFolder
    )

    private val sharedPreferences: SharedPreferences = activity.getSharedPreferences(permSAFPref, 0)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun addSAFPermission(data: Intent) {
        val treeUri = data.data
        activity.contentResolver.takePersistableUriPermission(
            treeUri!!,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        editor.putString(permSAFPrefUri, treeUri.toString())
        editor.apply()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun askSAFPermission() {

        if (!verifySAF(activity)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, androidUriStatus)
            requestPermissionLauncher.launch(intent)
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun showSAFDialog() {
        val dialog = Dialog(activity)
        val binding: DialogPermissionSafBinding =
            DialogPermissionSafBinding.inflate(activity.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        binding.btnAllow.setOnClickListener {
            dialog.dismiss()
            askSAFPermission()
        }

        dialog.show()

    }

}