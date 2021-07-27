package dev.vadzimv.jetpack.navigation.lintrules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class UnsafeNavigationDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("navigate")
    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        super.visitMethodCall(context, node, method)
        val evaluator = context.evaluator
        if (evaluator.isMemberInClass(method, "androidx.navigation.NavController")) {
            context.report(
                UNSAFE_NAVIGATION_ISSUE,
                scope = node,
                location = context.getCallLocation(
                    call = node,
                    includeReceiver = false,
                    includeArguments = true
                ),
                message = "Can cause IllegalArgumentException, use navigateSafe instead"
            )
        }
    }

    companion object {
        private val IMPLEMENTATION = Implementation(
            UnsafeNavigationDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        val UNSAFE_NAVIGATION_ISSUE = Issue.create(
            id = "UnsafeNavigation",
            briefDescription = "Can cause IllegalArgumentException",
            explanation = "When user triggers 2 navigations simultaneously source changes," +
                "and nav lib crashes with IllegralArgumentException for second navigation",
            category = Category.CORRECTNESS,
            priority = 10,
            severity = Severity.ERROR,
            androidSpecific = true,
            implementation = IMPLEMENTATION
        )
    }
}