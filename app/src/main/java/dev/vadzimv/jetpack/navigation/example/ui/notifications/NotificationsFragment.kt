package dev.vadzimv.jetpack.navigation.example.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.vadzimv.jetpack.navigation.example.R
import dev.vadzimv.jetpack.navigation.example.databinding.FragmentNotificationsBinding
import dev.vadzimv.jetpack.navigation.example.navigation.handleResult
import dev.vadzimv.jetpack.navigation.example.ui.pick.PickIntervalResult

class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNotificationsBinding.inflate(inflater, container, false).apply {
            textNotifications.text = PickIntervalResult.DAILY.toString()
            changeButton.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_notifications_to_pickNotificationIntervalFragment)
            }
        }

        findNavController().handleResult<PickIntervalResult>(
            viewLifecycleOwner,
            R.id.navigation_notifications,
            R.id.pickNotificationIntervalFragment
        ) { result ->
            binding.textNotifications.text = result.toString()
        }

        return binding.root
    }
}