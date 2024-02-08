package com.androvine.statussaver.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.androvine.statussaver.R
import com.androvine.statussaver.adapter.StatusAdapter
import com.androvine.statussaver.databinding.FragmentSavedBinding
import com.androvine.statussaver.model.StatusModel
import com.androvine.statussaver.utils.BuildVersion.Companion.isAndroidR
import com.androvine.statussaver.utils.PermSAFUtils
import com.google.android.material.tabs.TabLayout
import java.io.File

class FragmentSaved : Fragment() {

    private val binding: FragmentSavedBinding by lazy {
        FragmentSavedBinding.inflate(layoutInflater)
    }

    private var statusList: MutableList<StatusModel> = mutableListOf()

    private val statusAdapter by lazy {
        StatusAdapter(requireContext(), arrayListOf())
    }

    private var statusDocFile: DocumentFile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAndroidR()) {
            val safUri = PermSAFUtils.getSAFUri(requireActivity())
            statusDocFile = findStatusesFolder(requireActivity(), safUri)!!
        }

        binding.rvSaved.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.rvSaved.adapter = statusAdapter

        setupTabLayout()

    }

    private fun setupTabLayout() {

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Images")
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Videos")
        )

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        Toast.makeText(context, "Images", Toast.LENGTH_SHORT).show()

                        showSavedPhoto()

                    }

                    1 -> {

                        showSavedVideo()

                        Toast.makeText(context, "Videos", Toast.LENGTH_SHORT).show()

                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun showSavedPhoto() {
        statusList.clear()

        val folderName = resources.getString(R.string.app_name)
        val secondaryPath = resources.getString(R.string.folder_status_saver)

        val storagePath: String = if (isAndroidR()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .toString() + File.separator + folderName + File.separator + secondaryPath
        } else {
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + folderName + File.separator + secondaryPath
        }

        val file = File(storagePath)
        if (!file.isDirectory) {
            return
        }

        val arrayOfFiles = dirListByAscendingDate(file)

        arrayOfFiles?.forEach {
            if (!it.isDirectory && checkIfImageFile(it)) {
                val uri = it.toURI().toString()
                val statusModel = StatusModel(
                    uri, aboveAndroidR = false, isVideo = false, isSaved = true
                )
                statusList.add(statusModel)
            }
        }


        if (statusList.isEmpty()) {
            binding.llNoMedia.visibility = View.VISIBLE
        } else {
            binding.llNoMedia.visibility = View.GONE
        }


        statusAdapter.updateList(statusList)

        val photoCount = statusList.filter { !it.isVideo }.size
        binding.tabLayout.getTabAt(0)?.text = "Images : $photoCount"
    }

    private fun showSavedVideo() {
        statusList.clear()

        val folderName = resources.getString(R.string.app_name)
        val secondaryPath = resources.getString(R.string.folder_status_saver)

        val storagePath: String = if (isAndroidR()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .toString() + File.separator + folderName + File.separator + secondaryPath
        } else {
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + folderName + File.separator + secondaryPath
        }

        val file = File(storagePath)

        try {
            file.mkdirs()
        } catch (e: Exception) {
            Log.e("TAG", "showSavedVideo: $e")
        }

        if (!file.isDirectory) {
            return
        }

        val listfilemedia = dirListByAscendingDate(file)


        listfilemedia?.forEach {
            if (!it.isDirectory && checkIfVideoFile(it)) {
                val uri = it.toURI().toString()
                val statusModel = StatusModel(
                    uri, aboveAndroidR = false, isVideo = true, isSaved = true
                )
                statusList.add(statusModel)
            }
        }


        if (statusList.isEmpty()) {
            binding.llNoMedia.visibility = View.VISIBLE
        } else {
            binding.llNoMedia.visibility = View.GONE
        }


        statusAdapter.updateList(statusList)

        val videoCount = statusList.filter { it.isVideo }.size
        binding.tabLayout.getTabAt(1)?.text = "Videos : $videoCount"
    }

    private fun dirListByAscendingDate(folder: File): Array<File>? {
        if (!folder.isDirectory) {
            return null
        }
        val sortedByDate = folder.listFiles() ?: return null
        if (sortedByDate.isEmpty() || sortedByDate.size <= 1) {
            return sortedByDate
        }
        sortedByDate.sortWith { object1, object2 ->
            (object1.lastModified() - object2.lastModified()).toInt()
        }
        return sortedByDate
    }

    private fun findStatusesFolder(context: Context, rootUri: Uri): DocumentFile? {
        val root = DocumentFile.fromTreeUri(context, rootUri)
        val allFiles = root?.listFiles()
        return allFiles?.find { it.name == ".Statuses" }
    }

    private fun checkIfImageFile(file: File?): Boolean {
        var boolean = false

        if (file != null) {
            val name = file.toURI().toString()
            val lastPathSegment = name.substring(name.lastIndexOf("."))
            if (lastPathSegment == ".jpg" || lastPathSegment == ".jpeg" || lastPathSegment == ".png" || lastPathSegment == ".gif") {
                boolean = true
            }
        }

        return boolean
    }

    private fun checkIfVideoFile(file: File?): Boolean {
        var boolean = false

        if (file != null) {
            val name = file.toURI().toString()
            val lastPathSegment = name.substring(name.lastIndexOf("."))
            if (lastPathSegment == ".mp4" || lastPathSegment == ".mkv" || lastPathSegment == ".avi" || lastPathSegment == ".webm") {
                boolean = true
            }
        }

        return boolean

    }

    fun updateUI() {
        showSavedPhoto()
        binding.tabLayout.getTabAt(0)?.select()
    }

    override fun onResume() {
        super.onResume()
        showSavedPhoto()
        binding.tabLayout.getTabAt(0)?.select()
        updateUI()
    }


}