package dev.vadzimv.jetpack.navigation.lintrules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

class ExampleIssueRegistry() : IssueRegistry() {

    override val api: Int = com.android.tools.lint.detector.api.CURRENT_API

    override val issues: List<Issue> = listOf(
        UnsafeNavigationDetector.UNSAFE_NAVIGATION_ISSUE
    )
}