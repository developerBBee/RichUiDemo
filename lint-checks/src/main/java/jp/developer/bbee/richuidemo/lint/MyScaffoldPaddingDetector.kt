package jp.developer.bbee.richuidemo.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.visitor.AbstractUastVisitor
import java.util.regex.Pattern

class MyScaffoldPaddingDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("MyScaffold")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: com.intellij.psi.PsiMethod) {
        if (!isTargetMyScaffold(method)) return

        val lambda = node.valueArguments.lastOrNull { it is ULambdaExpression } as? ULambdaExpression ?: return
        if (!isPaddingUsed(lambda)) {
            context.report(
                issue = ISSUE,
                location = context.getNameLocation(node),
                message = "Content padding parameter is not used. Pass and apply the provided PaddingValues.",
            )
        }
    }

    private fun isTargetMyScaffold(method: com.intellij.psi.PsiMethod): Boolean {
        val owner = method.containingClass?.qualifiedName ?: return false
        return owner == "jp.developer.bbee.richuidemo.component.MyScaffoldKt"
    }

    private fun isPaddingUsed(lambda: ULambdaExpression): Boolean {
        val explicitParam = lambda.valueParameters.singleOrNull()
        if (explicitParam != null) {
            val parameterName = explicitParam.name
            if (parameterName == "_") return true

            var used = false
            lambda.body.accept(
                object : AbstractUastVisitor() {
                    override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression): Boolean {
                        val resolved = node.resolve()
                        if (node.identifier == parameterName &&
                            (resolved == explicitParam.javaPsi || resolved == explicitParam.sourcePsi)) {
                            used = true
                            return true
                        }
                        return super.visitSimpleNameReferenceExpression(node)
                    }
                },
            )
            return used
        }

        // Fallback for K2 where UAST may not populate valueParameters: parse from lambda source.
        val lambdaText = lambda.sourcePsi?.text ?: lambda.asSourceString()
        val explicitByLambdaText = extractExplicitParameterName(lambdaText)
        if (!explicitByLambdaText.isNullOrBlank()) {
            if (explicitByLambdaText == "_") return true
            val bodyText = lambdaText.substringAfter("->", missingDelimiterValue = "")
            return containsIdentifier(bodyText, explicitByLambdaText)
        }

        // Kotlin implicit single-parameter lambda uses `it`.
        return isItReferencedInLambda(lambda)
    }

    private fun isItReferencedInLambda(lambda: ULambdaExpression): Boolean {
        var found = false
        lambda.body.accept(object : AbstractUastVisitor() {
            override fun visitLambdaExpression(node: ULambdaExpression): Boolean {
                // Only stop traversal into nested lambdas that have their own parameters,
                // which would shadow the outer `it`. Parameterless lambdas (e.g., remember { })
                // can still capture the outer `it`.
                return node.valueParameters.isNotEmpty()
            }
            override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression): Boolean {
                if (node.identifier == "it") {
                    found = true
                    return true
                }
                return super.visitSimpleNameReferenceExpression(node)
            }
        })
        return found
    }

    private fun extractExplicitParameterName(text: String): String? {
        val match = Regex("^\\{\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*->").find(text) ?: return null
        return match.groupValues.getOrNull(1)
    }


    private fun containsIdentifier(text: String, identifier: String): Boolean {
        val pattern = Pattern.compile("(?<![A-Za-z0-9_])${Pattern.quote(identifier)}(?![A-Za-z0-9_])")
        return pattern.matcher(text).find()
    }

    companion object {
        val ISSUE: Issue = Issue.create(
            id = "MyScaffoldUnusedContentPaddingParameter",
            briefDescription = "MyScaffold content padding parameter is not used",
            explanation = "MyScaffold forwards Material3 Scaffold content padding. Ignore it only when intentional; otherwise, apply it to avoid overlapped content.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                MyScaffoldPaddingDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}

