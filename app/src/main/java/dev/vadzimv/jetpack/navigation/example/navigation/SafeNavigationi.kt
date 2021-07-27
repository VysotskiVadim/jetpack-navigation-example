package dev.vadzimv.jetpack.navigation.example.navigation

import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.navigation.NavController

private const val NAVIGATION_SAFE_TAG = "SAFE_NAVIGATION"

fun NavController.navigateSafe(@IdRes action: Int, args: Bundle? = null): Boolean {
    return try {
        navigate(action, args)
        true
    } catch (t: Throwable) {
        Log.e(NAVIGATION_SAFE_TAG, "navigation error for action $action")
        false
    }
}