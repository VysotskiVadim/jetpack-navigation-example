package dev.vadzimv.jetpack.navigation.example.navigation

import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController

fun <T : Parcelable> NavController.handleResult(
    lifecycleOwner: LifecycleOwner,
    @IdRes currentDestinationId: Int,
    @IdRes vararg handleResultFrom: Int,
    handler: (T) -> Unit
) {
    // `getCurrentBackStackEntry` doesn't work in case of recovery from the process death when dialog is opened.
    val currentEntry = getBackStackEntry(currentDestinationId)
    val expectedResultKeys = handleResultFrom.map { resultName(it) }
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            for (expectedResultKey in expectedResultKeys) {
                if (currentEntry.savedStateHandle.contains(expectedResultKey)) {
                    val result = currentEntry.savedStateHandle.get<T>(expectedResultKey)
                    handler(result!!)
                    currentEntry.savedStateHandle.remove<T>(expectedResultKey)
                }
            }
        }
    }
    currentEntry.lifecycle.addObserver(observer)
    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            currentEntry.lifecycle.removeObserver(observer)
        }
    })
}

fun <T : Parcelable> NavController.finishWithResult(result: T) {
    val currentDestinationId = currentDestination?.id
    if (currentDestinationId != null) {
        previousBackStackEntry?.savedStateHandle?.set(resultName(currentDestinationId), result)
    }
    popBackStack()
}

private fun resultName(resultSourceId: Int) = "result-$resultSourceId"