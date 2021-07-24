package dev.vadzimv.jetpack.navigation.example.ui.pick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.vadzimv.jetpack.navigation.example.R
import dev.vadzimv.jetpack.navigation.example.databinding.FragmentPickNotificationIntervalBinding
import dev.vadzimv.jetpack.navigation.example.navigation.finishWithResult

class PickNotificationIntervalFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pick_notification_interval, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentPickNotificationIntervalBinding.bind(view).apply {
            redButton.apply {
                setOnClickListener {
                    findNavController().finishWithResult(PickIntervalResult.WEEKLY)
                }
                text = PickIntervalResult.WEEKLY.toString()
            }
            blueButton.apply {
                setOnClickListener {
                    findNavController().finishWithResult(PickIntervalResult.DAILY)
                }
                text = PickIntervalResult.DAILY.toString()
            }
        }
    }
}