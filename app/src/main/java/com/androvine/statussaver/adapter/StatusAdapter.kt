package com.androvine.statussaver.adapter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.androvine.statussaver.R
import com.androvine.statussaver.databinding.SingleStatusItemBinding
import com.androvine.statussaver.model.StatusModel
import com.androvine.statussaver.utils.BuildVersion
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class StatusAdapter(
    private val context: Context,
    private val listOfString: MutableList<StatusModel>,
) :
    RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SingleStatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val statusModel = listOfString[position]
        Glide.with(holder.binding.statusImage.context).load(statusModel.uri)
            .into(holder.binding.statusImage)

        val item = listOfString[position]

        if (statusModel.isVideo) {
            holder.binding.ivVideo.visibility = View.VISIBLE
        } else {
            holder.binding.ivVideo.visibility = View.GONE
        }

        if (statusModel.isSaved) {
            holder.binding.downloadLayout.visibility = View.GONE
        } else {
            holder.binding.downloadLayout.visibility = View.VISIBLE
        }

        holder.binding.downloadLayout.setOnClickListener {

            val folderName = holder.binding.root.context.resources.getString(R.string.app_name)
            val secondaryPath =
                holder.binding.root.context.resources.getString(R.string.folder_status_saver)

            val storagePath: String = if (BuildVersion.isAndroidR()) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + File.separator + folderName + File.separator + secondaryPath
            } else {
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + folderName + File.separator + secondaryPath
            }

            if (BuildVersion.isAndroidR()) {
                val inputUri = Uri.parse(statusModel.uri)
                Log.d("TAG", "onBindViewHolder: $inputUri")

                val docFile = DocumentFile.fromSingleUri(holder.binding.root.context, inputUri)
                val fileName = docFile?.name

                val dir = File(storagePath)
                if (!dir.exists()) {
                    Log.e("TAG", "make dir: " + dir.mkdirs())
                }

                val newFile = File(storagePath + File.separator + fileName)
                val inputStream =
                    holder.binding.root.context.contentResolver.openInputStream(inputUri)
                val fileOutputStream: OutputStream = FileOutputStream(newFile)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(inputStream!!, fileOutputStream)
                }
                Toast.makeText(
                    holder.binding.root.context, "Saved successfully!", Toast.LENGTH_LONG
                ).show()
                // remove from list
                listOfString.removeAt(position)
                notifyDataSetChanged()
                inputStream?.close()

            } else {

                try {
                    val oldFilePath =
                        Uri.parse(statusModel.uri).path // Remove 'file:' scheme if needed
                    val oldFile = File(oldFilePath!!)

                    if (oldFile.exists()) {
                        val newFile = File(storagePath + File.separator + oldFile.name)
                        oldFile.copyTo(newFile)
                        Toast.makeText(
                            holder.binding.root.context, "Saved successfully!", Toast.LENGTH_SHORT
                        ).show()

                        // remove from list
                        listOfString.removeAt(position)
                        notifyDataSetChanged()

                    } else {
                        Log.e("TAG", "File does not exist at: $oldFilePath")
                        Toast.makeText(
                            holder.binding.root.context, "File does not exist!", Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "Error occurred: ${e.message}")
                    Toast.makeText(
                        holder.binding.root.context, "Error occurred!", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        holder.binding.statusImage.setOnClickListener {
            try {
                val file =
                    File(Uri.parse(item.uri).path.toString())
                val fileUri = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    file
                )

                val openFileIntent = Intent(Intent.ACTION_VIEW)
                openFileIntent.setDataAndType(
                    fileUri,
                    if (item.isVideo) "video/*" else "image/*"
                )
                openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(
                    openFileIntent, PackageManager.MATCH_DEFAULT_ONLY
                )

                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                holder.binding.statusImage.context.startActivity(
                    Intent.createChooser(
                        openFileIntent,
                        "Open File"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfString.size
    }

    fun updateList(newList: MutableList<StatusModel>) {
        listOfString.clear()
        listOfString.addAll(newList)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: SingleStatusItemBinding) : RecyclerView.ViewHolder(binding.root)


}