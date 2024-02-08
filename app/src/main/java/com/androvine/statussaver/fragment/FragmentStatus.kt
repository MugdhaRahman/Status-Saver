package com.androvine.statussaver.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.androvine.statussaver.R
import com.androvine.statussaver.adapter.StatusAdapter
import com.androvine.statussaver.databinding.FragmentStatusBinding
import com.androvine.statussaver.model.StatusModel
import com.androvine.statussaver.utils.BuildVersion
import com.androvine.statussaver.utils.BuildVersion.Companion.isAndroidR
import com.androvine.statussaver.utils.PermSAFUtils
import com.androvine.statussaver.utils.PermStorageUtils
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.util.concurrent.Executors


class FragmentStatus : Fragment() {

    private val binding: FragmentStatusBinding by lazy {
        FragmentStatusBinding.inflate(layoutInflater)
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var permSAFUtils: PermSAFUtils
    private lateinit var permStorageUtils: PermStorageUtils

    private var statusList: MutableList<StatusModel> = mutableListOf()
    private var downloadList: MutableList<StatusModel> = mutableListOf()

    private val statusAdapter by lazy {
        StatusAdapter(requireContext(),arrayListOf())
    }

    private var statusDocFile: DocumentFile? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAndroidR()) {
            val safUri = PermSAFUtils.getSAFUri(requireActivity())
            statusDocFile = findStatusesFolder(requireActivity(), safUri)!!
        }

        binding.rvStatus.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.rvStatus.adapter = statusAdapter

        setupTabLayout()

        setupIntentLauncher()

        checkPermissions()

        setupPermission()


    }

    private fun setupTabLayout() {

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Images"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Videos"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        showStatusPhoto()
                        Toast.makeText(context, "Images", Toast.LENGTH_SHORT).show()
                    }

                    1 -> {
                        showStatusVideo()
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

    private fun checkPermissions() {

        if (BuildVersion.isAndroidR()) {
            if (PermSAFUtils.verifySAF(requireActivity())) {
                binding.permissionLayout.visibility = View.GONE
                binding.llMedia.visibility = View.VISIBLE
            } else {
                binding.permissionLayout.visibility = View.VISIBLE
                binding.llMedia.visibility = View.GONE
            }
        } else {
            if (PermStorageUtils.isStoragePermissionGranted(requireActivity())) {
                binding.permissionLayout.visibility = View.GONE
                binding.llMedia.visibility = View.VISIBLE
            } else {
                binding.permissionLayout.visibility = View.VISIBLE
                binding.llMedia.visibility = View.GONE
            }
        }
    }

    private fun setupPermission() {

        binding.btnAllow.setOnClickListener {
            if (BuildVersion.isAndroidR()) {
                permSAFUtils.showSAFDialog()
            } else {
                permStorageUtils.askStoragePermission()
            }
        }

        permStorageUtils.setPermissionCallback(object : PermStorageUtils.PermissionCallback {
            override fun onPermissionGranted() {
                binding.permissionLayout.visibility = View.GONE
                binding.llMedia.visibility = View.VISIBLE
            }

            override fun onPermissionDenied() {
                binding.imgPermission.setImageResource(R.drawable.img_permission_saf_2)
                binding.tvPermission.text =
                    "Storage permission is required to access media files \nplease try again."
                binding.tvAllow.text = "Try Again"

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
        permSAFUtils = PermSAFUtils(requireActivity(), requestPermissionLauncher)
        permStorageUtils = PermStorageUtils(requireActivity())

    }


    private fun showStatusPhoto() {
        statusList.clear()
        Executors.newSingleThreadExecutor().execute {
            if (isAndroidR()) {
                if (statusDocFile == null) {
                    val safUri = PermSAFUtils.getSAFUri(requireActivity())
                    statusDocFile = findStatusesFolder(requireActivity(), safUri)

                }
                val docFile = statusDocFile

                if (docFile != null && docFile.exists() && docFile.isDirectory) {
                    // List folders and files within .Statuses folder
                    val statusesFiles = docFile.listFiles()

                    // Access files in .Statuses folder
                    for (file in statusesFiles) {

                        if (!file.isDirectory && checkIfImageFile(file)) {
                            val uri = file.uri.toString()
                            val statusModel = StatusModel(
                                uri, aboveAndroidR = true, isVideo = false, isSaved = false
                            )

                            statusList.add(statusModel)
                        }

                    }
                } else {
                    Log.e("TAG", ".Statuses folder not found")
                }

            } else {
                val folderName = "WhatsApp/Media/.Statuses"
                val mFile = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + folderName + File.separator
                )


                val files = mFile.listFiles()
                if (files != null && files.isNotEmpty()) {
                    for (file in files) {
                        if (!file.isDirectory && checkIfImageFile(file)) {
                            val uri = file.toURI().toString()
                            val statusModel = StatusModel(
                                uri, aboveAndroidR = false, isVideo = false, isSaved = false
                            )
                            statusList.add(statusModel)
                        }
                    }
                }
            }

            requireActivity().runOnUiThread {

                val revisedList = removeSaved(statusList)

                if (revisedList.isEmpty()) {
                    binding.llNoMedia.visibility = View.VISIBLE
                } else {
                    binding.llNoMedia.visibility = View.GONE
                }
                statusAdapter.updateList(revisedList)
            }

        }

    }

    private fun showStatusVideo() {
        statusList.clear()


        Executors.newSingleThreadExecutor().execute {
            if (isAndroidR()) {

                if (statusDocFile == null) {
                    val safUri = PermSAFUtils.getSAFUri(requireActivity())
                    statusDocFile = findStatusesFolder(requireActivity(), safUri)
                    Log.e("TAG", "showStatusVideo: null")
                }

                val docFile = statusDocFile

                if (docFile != null && docFile.exists() && docFile.isDirectory) {
                    // List folders and files within .Statuses folder
                    val statusesFiles = docFile.listFiles()

                    // Access files in .Statuses folder
                    for (file in statusesFiles) {

                        if (!file.isDirectory && checkIfVideoFile(file)) {
                            val uri = file.uri.toString()
                            val statusModel = StatusModel(
                                uri, aboveAndroidR = true, isVideo = true, isSaved = false
                            )
                            statusList.add(statusModel)
                        }

                    }
                } else {
                    Log.e("TAG", ".Statuses folder not found")
                }

            } else {
                val folderName = "WhatsApp/Media/.Statuses"
                val mFile = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + folderName + File.separator
                )


                val files = mFile.listFiles()
                if (files != null && files.isNotEmpty()) {
                    for (file in files) {
                        if (!file.isDirectory && checkIfVideoFile(file)) {
                            val uri = file.toURI().toString()
                            val statusModel = StatusModel(
                                uri, aboveAndroidR = false, isVideo = true, isSaved = false
                            )
                            statusList.add(statusModel)
                        }
                    }
                }


            }

            requireActivity().runOnUiThread {

                val revisedList = removeSaved(statusList)

                if (revisedList.isEmpty()) {
                    binding.llNoMedia.visibility = View.VISIBLE
                } else {
                    binding.llNoMedia.visibility = View.GONE
                }
                statusAdapter.updateList(revisedList)
            }

        }


    }

    private fun removeSaved(statusList: MutableList<StatusModel>): MutableList<StatusModel> {

        // download list to filename list
        val downloadFileNameList = mutableListOf<String>()
        downloadList.forEach {
            val uri = Uri.parse(it.uri)
            val file = File(uri.path!!)
            downloadFileNameList.add(file.name)
        }

        val newList = mutableListOf<StatusModel>()

        if (isAndroidR()) {
            statusList.forEach {
                val docFile = DocumentFile.fromSingleUri(requireActivity(), Uri.parse(it.uri))
                val fileName = docFile?.name
                if (fileName != null && !downloadFileNameList.contains(fileName)) {
                    newList.add(it)
                }
            }
        } else {
            statusList.forEach {
                val uri = Uri.parse(it.uri)
                val file = File(uri.path!!)
                if (!downloadFileNameList.contains(file.name)) {
                    newList.add(it)
                }
            }
        }

        return newList
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

    private fun checkIfImageFile(file: DocumentFile?): Boolean {
        var boolean = false

        if (file != null) {
            val name = file.uri.toString()
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

    private fun checkIfVideoFile(file: DocumentFile?): Boolean {
        var boolean = false

        if (file != null) {
            val name = file.uri.toString()
            val lastPathSegment = name.substring(name.lastIndexOf("."))
            if (lastPathSegment == ".mp4" || lastPathSegment == ".mkv" || lastPathSegment == ".avi" || lastPathSegment == ".webm") {
                boolean = true
            }
        }

        return boolean

    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        showStatusPhoto()
        binding.tabLayout.getTabAt(0)?.select()
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