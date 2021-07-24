package dev.vadzimv.jetpack.navigation.example.ui.pick

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class PickIntervalResult : Parcelable {
    DAILY, WEEKLY
}