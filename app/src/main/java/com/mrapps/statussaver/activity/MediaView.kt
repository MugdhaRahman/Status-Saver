package com.mrapps.statussaver.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.mrapps.statussaver.R
import com.mrapps.statussaver.databinding.ActivityMediaViewBinding
import com.mrapps.statussaver.databinding.DialogDeleteFilesBinding
import com.mrapps.statussaver.databinding.DialogInfoFilesBinding
import com.mrapps.statussaver.utils.BuildVersion.Companion.isAndroidR
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MediaView : AppCompatActivity() {

    private val binding: ActivityMediaViewBinding by lazy {
        ActivityMediaViewBinding.inflate(layoutInflater)
    }

    private lateinit var uri: Uri
    private var isVideo = false
    private var isSaved = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        uri = Uri.parse(intent.getStringExtra("uri"))
        isVideo = intent.getBooleanExtra("isVideo", false)
        isSaved = intent.getBooleanExtra("isSaved", false)

        Log.e("StatusViewer", "Uri: $uri \n isVideo: $isVideo  \n isSaved: $isSaved")

        if (isVideo) {
            binding.videoViewLayout.visibility = View.VISIBLE
            binding.imageView.visibility = View.GONE
            showVideo()
        } else {
            binding.videoViewLayout.visibility = View.GONE
            binding.imageView.visibility = View.VISIBLE
            showImage()
        }

        if (isSaved) {
            binding.saveLayout.visibility = View.GONE
            binding.deleteLayout.visibility = View.VISIBLE
        } else {
            binding.saveLayout.visibility = View.VISIBLE
            binding.deleteLayout.visibility = View.GONE
        }


        if (isAndroidR() && isSaved) {
            val file = uri.path?.let { File(it) }
            if (file != null) {
                binding.tvName.text = resizeName(file.name + "")
            }

        } else if (!isAndroidR() && isSaved) {
            val file = File(uri.path.toString())
            binding.tvName.text = resizeName(file.name + "")

        } else if (isAndroidR() && !isSaved) {
            val docFile = DocumentFile.fromSingleUri(this, uri)
            val fileName = docFile?.name
            binding.tvName.text = resizeName(fileName + "")

        } else if (!isAndroidR() && !isSaved) {
            val file = File(uri.path.toString())
            binding.tvName.text = resizeName(file.name + "")
        }


        setUpOnClickListeners()

    }

    private fun resizeName(s: String): String {
        val first = s.substring(0, 10)
        val last = s.substring(s.length - 10, s.length)
        return "$first...$last"
    }

    @SuppressLint("SetTextI18n")
    private fun setUpOnClickListeners() {

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.share.setOnClickListener {
            if (isAndroidR() && !isSaved) {
                try {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.clipData = ClipData.newRawUri("", uri)
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    shareIntent.type = contentResolver.getType(uri)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    val resInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
                        shareIntent, PackageManager.MATCH_DEFAULT_ONLY
                    )
                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        grantUriPermission(
                            packageName,
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share File"))


                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                Log.e("StatusViewer", "Uri: ${uri.path}")
                try {
                    val file = File(uri.path.toString())
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.type = "image/*"
                    val fileUri = FileProvider.getUriForFile(
                        this, applicationContext.packageName + ".provider", file
                    )
                    shareIntent.clipData = ClipData.newRawUri("", fileUri)
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(shareIntent, "Share File"))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.delete.setOnClickListener {
            val dialog = Dialog(this)
            val binding = DialogDeleteFilesBinding.inflate(layoutInflater)
            dialog.setContentView(binding.root)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            binding.delete.setOnClickListener {
                dialog.dismiss()
                if (isAndroidR() && !isSaved) {
                    try {
                        val docFile = DocumentFile.fromSingleUri(this, uri)
                        docFile?.delete()
                        Toast.makeText(this, "Deleted successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Can't delete this file!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    try {
                        val file = File(uri.path.toString())
                        file.delete()
                        Toast.makeText(this, "Deleted successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Can't delete this file!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.cancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()

        }

        binding.info.setOnClickListener {
            val dialog = Dialog(this)
            val binding = DialogInfoFilesBinding.inflate(layoutInflater)
            dialog.setContentView(binding.root)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)


            if (isAndroidR() && !isSaved) {
                val docFile = DocumentFile.fromSingleUri(this, uri)
                val fileName = docFile?.name
                binding.fileName.text = fileName
                binding.fileSize.text = formattedFileSize(docFile?.length()!!)
                binding.folderPath.text =
                    "Android/Media/com.whatsapp/WhatsApp/Media/.Statuses/$fileName"
                binding.fileDate.text = formattedDate(docFile.lastModified())
                binding.fileType.text = docFile.type


            } else {
                val file = File(uri.path.toString())
                binding.fileName.text = file.name
                binding.fileSize.text = formattedFileSize(file.length())
                binding.folderPath.text = file.path
                binding.fileDate.text = formattedDate(file.lastModified())
                binding.fileType.text = file.extension
            }

            binding.btnDone.setOnClickListener {
                dialog.dismiss()
            }



            dialog.show()

        }

        binding.save.setOnClickListener {

            val folderName = getString(R.string.app_name)
            val secondaryPath =
                resources.getString(R.string.folder_status_saver)

            val storagePath: String = if (isAndroidR()) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + File.separator + folderName + File.separator + secondaryPath
            } else {
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + folderName + File.separator + secondaryPath
            }

            if (isAndroidR()) {
                val inputUri = uri
                Log.d("TAG", "onBindViewHolder: $inputUri")

                val docFile = DocumentFile.fromSingleUri(this, inputUri)
                val fileName = docFile?.name

                val dir = File(storagePath)
                if (!dir.exists()) {
                    Log.e("TAG", "make dir: " + dir.mkdirs())
                }

                val newFile = File(storagePath + File.separator + fileName)
                val inputStream =
                    contentResolver.openInputStream(inputUri)
                val fileOutputStream: OutputStream = FileOutputStream(newFile)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(inputStream!!, fileOutputStream)
                }
                Toast.makeText(
                    this, "Saved successfully!", Toast.LENGTH_LONG
                ).show()

                binding.saveLayout.visibility = View.GONE

                inputStream?.close()

            } else {

                try {
                    val oldFilePath =
                        uri.path // Remove 'file:' scheme if needed
                    val oldFile = File(oldFilePath!!)

                    if (oldFile.exists()) {
                        val newFile = File(storagePath + File.separator + oldFile.name)
                        oldFile.copyTo(newFile)
                        Toast.makeText(
                            this, "Saved successfully!", Toast.LENGTH_SHORT
                        ).show()

                        binding.saveLayout.visibility = View.GONE


                    } else {
                        Log.e("TAG", "File does not exist at: $oldFilePath")
                        Toast.makeText(
                            this, "File does not exist!", Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "Error occurred: ${e.message}")
                    Toast.makeText(
                        this, "Error occurred!", Toast.LENGTH_SHORT
                    ).show()
                }


            }


        }


    }

    @SuppressLint("SimpleDateFormat")
    private fun formattedDate(lastModified: Long): String {
        // to day/month/year
        val date = java.util.Date(lastModified)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
        return sdf.format(date)
    }

    private fun formattedFileSize(length: Long): String {
        // to kb, mb , gb
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        return if (length >= gb) {
            String.format("%.1f GB", length / gb.toFloat())
        } else if (length >= mb) {
            String.format("%.1f MB", length / mb.toFloat())
        } else if (length >= kb) {
            String.format("%.1f KB", length / kb.toFloat())
        } else {
            String.format("%d B", length)
        }
    }

    private fun showImage() {
        Glide.with(this).load(uri).into(binding.imageView)
    }

    private fun showVideo() {
        setVideoAreaSize()

    }


    private fun setVideoAreaSize() {
        binding.videoViewLayout.post {
            val width: Int = binding.videoViewLayout.width
            val videoLayoutParams: ViewGroup.LayoutParams = binding.videoViewLayout.layoutParams
            videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            videoLayoutParams.height = (width * 405f / 720f).toInt()
            binding.videoViewLayout.layoutParams = videoLayoutParams
            binding.videoView.setVideoURI(uri)
            binding.videoView.requestFocus()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            runOnUiThread {
                binding.videoView.start()
            }
        }, 1000)


    }

}
