package dev.vadzimv.jetpack.navigation.example.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dev.vadzimv.jetpack.navigation.example.databinding.FragmentBottomMenuBinding

class BottomMenuHostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentBottomMenuBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bindings = FragmentBottomMenuBinding.bind(view)
        bindings.navView.setupWithNavController(findNavController())
    }
}