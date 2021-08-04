package dev.vadzimv.jetpack.navigation.example.navigation

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import dev.vadzimv.jetpack.navigation.example.R
import java.util.ArrayDeque

private const val KEY_BACK_STACK_IDS = "customFragmentNavigator:backStackIds"
private const val KEY_OUTER_FRAGMENT_ID = "customFragmentNavigator:outerFragmentId"
private const val OUTER_FRAGMENT_ID_DEFAULT_VALUE = -1

@Navigator.Name("customFragment")
class CustomFragmentNavigator(
    private val context: Context,
    private val innerNavigationContainer: () -> NavigationContainer,
    private val outerNavigationContainer: NavigationContainer
) : Navigator<CustomFragmentNavigator.Destination>() {

    private val backStack = ArrayDeque<Int>()

    private var outerFragmentBarrierId: Int? = null

    @NavDestination.ClassType(Fragment::class)
    class Destination(navigator: CustomFragmentNavigator) : NavDestination(navigator) {

        var className: String = ""
            private set

        var outer: Boolean = false
            private set
        var add: Boolean = false
            private set

        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)
            val a = context.resources.obtainAttributes(
                attrs,
                R.styleable.customFragmentNavigator
            )
            className = a.getString(R.styleable.customFragmentNavigator_android_name).orEmpty()
            outer = a.getBoolean(R.styleable.customFragmentNavigator_outer, false)
            add = a.getBoolean(R.styleable.customFragmentNavigator_add, false)
            a.recycle()
        }
    }

    @Suppress("ComplexMethod", "ComplexCondition")
    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        val navigationContainer = chooseNavigationContainerAndUpdateOuterBarrier(destination)
        if (navigationContainer.fragmentManager.isStateSaved) {
            return null
        }

        val frag: Fragment = createFragment(destination, navigationContainer)
        frag.arguments = args

        val fragmentManager = navigationContainer.fragmentManager
        val ft: FragmentTransaction = fragmentManager.beginTransaction()

        var enterAnim = navOptions?.enterAnim ?: -1
        var exitAnim = navOptions?.exitAnim ?: -1
        var popEnterAnim = navOptions?.popEnterAnim ?: -1
        var popExitAnim = navOptions?.popExitAnim ?: -1
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = if (enterAnim != -1) enterAnim else 0
            exitAnim = if (exitAnim != -1) exitAnim else 0
            popEnterAnim = if (popEnterAnim != -1) popEnterAnim else 0
            popExitAnim = if (popExitAnim != -1) popExitAnim else 0
            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
        }

        if (destination.add) {
            ft.add(navigationContainer.viewId, frag)
        } else {
            ft.replace(navigationContainer.viewId, frag)
        }
        ft.setPrimaryNavigationFragment(frag)

        @IdRes val destId = destination.id
        val initialNavigation: Boolean = backStack.isEmpty()

        val isSingleTopReplacement = (navOptions != null &&
                !initialNavigation && navOptions.shouldLaunchSingleTop() &&
                backStack.peekLast() == destId)

        val isAdded: Boolean = when {
            initialNavigation -> {
                true
            }
            isSingleTopReplacement -> {
                // Single Top means we only want one instance on the back stack
                if (backStack.size > 1) {
                    // If the Fragment to be replaced is on the FragmentManager's
                    // back stack, a simple replace() isn't enough so we
                    // remove it from the back stack and put our replacement
                    // on the back stack in its place
                    fragmentManager.popBackStack(
                        generateBackStackName(backStack.size, backStack.peekLast()),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    ft.addToBackStack(generateBackStackName(backStack.size, destId))
                }
                false
            }
            else -> {
                ft.addToBackStack(generateBackStackName(backStack.size + 1, destId))
                true
            }
        }
        ft.setReorderingAllowed(true)
        ft.commit()
        // The commit succeeded, update our view of the world
        return if (isAdded) {
            backStack.add(destId)
            destination
        } else {
            null
        }
    }

    private fun createFragment(
        destination: Destination,
        navigationContainer: NavigationContainer
    ): Fragment {
        var className: String = destination.className
        if (className[0] == '.') {
            className = context.packageName + className
        }
        val frag: Fragment = instantiateFragment(
            context,
            navigationContainer.fragmentManager,
            className
        )
        return frag
    }

    private fun chooseNavigationContainerAndUpdateOuterBarrier(
        destination: Destination
    ): NavigationContainer {
        val isOuter = destination.outer || outerFragmentBarrierId != null
        if (destination.outer && outerFragmentBarrierId == null) {
            outerFragmentBarrierId = destination.id
        }
        return if (isOuter) {
            outerNavigationContainer
        } else {
            innerNavigationContainer()
        }
    }

    private fun chooseFragmentManagerForMovingBackAndUpdateOuterBarrier(topDestinationId: Int): FragmentManager {
        return if (outerFragmentBarrierId != null) {
            if (topDestinationId == outerFragmentBarrierId && isOnlyOneOuterBarrierOnTheBackStack()) {
                outerFragmentBarrierId = null
            }
            outerNavigationContainer.fragmentManager
        } else innerNavigationContainer().fragmentManager
    }

    private fun isOnlyOneOuterBarrierOnTheBackStack() =
        backStack.count { it == outerFragmentBarrierId } <= 1

    override fun onSaveState(): Bundle {
        val b = Bundle()
        val backStackCopyToSave = IntArray(backStack.size)
        var index = 0
        for (id in backStack) {
            backStackCopyToSave[index++] = id
        }
        b.putIntArray(KEY_BACK_STACK_IDS, backStackCopyToSave)
        b.putInt(KEY_OUTER_FRAGMENT_ID, outerFragmentBarrierId ?: OUTER_FRAGMENT_ID_DEFAULT_VALUE)
        return b
    }

    override fun onRestoreState(savedState: Bundle) {
        val savedBackStack = savedState.getIntArray(KEY_BACK_STACK_IDS)
        if (savedBackStack != null) {
            backStack.clear()
            for (destId in savedBackStack) {
                backStack.add(destId)
            }
        }

        val outerFragmentId =
            savedState.getInt(KEY_OUTER_FRAGMENT_ID, OUTER_FRAGMENT_ID_DEFAULT_VALUE)
        outerFragmentBarrierId = if (outerFragmentId == OUTER_FRAGMENT_ID_DEFAULT_VALUE) {
            null
        } else {
            outerFragmentId
        }
    }

    private fun instantiateFragment(
        context: Context,
        fragmentManager: FragmentManager,
        className: String
    ): Fragment = fragmentManager.fragmentFactory.instantiate(context.classLoader, className)

    override fun createDestination(): Destination = Destination(this)

    override fun popBackStack(): Boolean {
        if (backStack.isEmpty()) {
            return false
        }
        val topDestination = backStack.last
        val fragmentManager =
            chooseFragmentManagerForMovingBackAndUpdateOuterBarrier(topDestination)
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(
            generateBackStackName(backStack.size, topDestination),
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        backStack.removeLast()
        return true
    }

    private fun generateBackStackName(backStackIndex: Int, destId: Int): String {
        return "$backStackIndex-$destId"
    }
}

data class NavigationContainer(
    @IdRes val viewId: Int,
    val fragmentManager: FragmentManager
)