package dev.vadzimv.jetpack.navigation.example.navigation

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dev.vadzimv.jetpack.navigation.example.ui.BottomMenuHostFragment
import dev.vadzimv.jetpack.navigation.example.R

class CustomBottomMenuNavHostFragment : NavHostFragment() {

    override fun onCreateNavController(navController: NavController) {
        super.onCreateNavController(navController)
        initializeBottomMenuFragmentIfNeeded()
        navController.navigatorProvider.addNavigator(
            CustomFragmentNavigator(
                requireContext(),
                innerNavigationContainer = {
                    NavigationContainer(
                        R.id.innerNavigationContainer,
                        getBottomMenuFragment()?.childFragmentManager
                            ?: error("no bottom navigation fragment in child fragment manager")
                    )
                },
                outerNavigationContainer = NavigationContainer(
                    id,
                    childFragmentManager
                )
            )
        )
    }

    private fun initializeBottomMenuFragmentIfNeeded() {
        if (childFragmentManager.fragments.isEmpty()) {
            childFragmentManager.beginTransaction()
                .replace(id, BottomMenuHostFragment())
                .commitNow()
        }
    }

    private fun getBottomMenuFragment(): Fragment? {
        val bottomMenuHostFragment = findBottomMenuFragmentOnTheStack()
        if (bottomMenuHostFragment == null && childFragmentManager.executePendingTransactions()) {
            // if there are some unfinished transactions fragment can be unavailable
            return findBottomMenuFragmentOnTheStack()
        }
        return bottomMenuHostFragment
    }

    private fun findBottomMenuFragmentOnTheStack(): Fragment? =
        childFragmentManager.fragments.singleOrNull { it is BottomMenuHostFragment }
}