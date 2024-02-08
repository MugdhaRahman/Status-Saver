package com.androvine.statussaver.permissionMVVM
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val permissionModule = module {
    single { PermissionRepository(androidContext()) }
    viewModel { PermissionViewModel(get()) }
}