package dev.vadzimv.jetpack.navigation.example.navigation

import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

fun <T : Parcelable> NavController.handleResult(
    lifecycleOwner: LifecycleOwner,
    @IdRes currentDestinationId: Int,
    @IdRes vararg childrenDestinationsIds: Int,
    handler: (T) -> Unit
) {
    // `getCurrentBackStackEntry` doesn't work in case of recovery from the process death when dialog is opened.
    val currentEntry = getBackStackEntry(currentDestinationId)
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            tryHandleResult(childrenDestinationsIds, currentEntry, handler)
        }
    }
    currentEntry.lifecycle.addObserver(observer)
    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            currentEntry.lifecycle.removeObserver(observer)
        }
    })
}

private fun <T : Parcelable> tryHandleResult(
    childrenDestinationsIds: IntArray,
    currentEntry: NavBackStackEntry,
    handler: (T) -> Unit
) {
    for (childrenDestination in childrenDestinationsIds) {
        val expectedResultKey = resultName(childrenDestination)
        if (currentEntry.savedStateHandle.contains(expectedResultKey)) {
            val result = currentEntry.savedStateHandle.get<T>(expectedResultKey)
            handler(result!!)
            currentEntry.savedStateHandle.remove<T>(expectedResultKey)
        }
    }
}

fun <T : Parcelable> NavController.finishWithResult(result: T) {
    val currentDestinationId = currentDestination?.id
    if (currentDestinationId != null) {
        previousBackStackEntry?.savedStateHandle?.set(resultName(currentDestinationId), result)
    }
    popBackStack()
}

private fun resultName(resultSourceId: Int) = "result-$resultSourceId"