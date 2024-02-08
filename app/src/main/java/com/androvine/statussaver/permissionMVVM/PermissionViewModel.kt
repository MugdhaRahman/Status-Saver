package com.androvine.statussaver.permissionMVVM

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionViewModel (private val permissionRepository: PermissionRepository) : ViewModel() {

    private val _hasNotificationPermission = MutableLiveData<PermissionStatus>()
    val hasNotificationPermission: LiveData<PermissionStatus> = _hasNotificationPermission

    fun checkNotificationPermission(){
        val hasPermission = permissionRepository.hasNotificationPermission()
        _hasNotificationPermission.value = if (hasPermission) PermissionStatus.GRANTED else PermissionStatus.INITIAL
    }

    fun handleNotificationPermissionResult(granted: Boolean){
        _hasNotificationPermission.value = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
    }


}