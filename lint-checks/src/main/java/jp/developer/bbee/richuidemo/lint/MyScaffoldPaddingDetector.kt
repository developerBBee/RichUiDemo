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
        if (!isPaddingUsed(context, node, lambda)) {
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

    private fun isPaddingUsed(context: JavaContext, node: UCallExpression, lambda: ULambdaExpression): Boolean {
        val sourceSnippet = extractCallSnippet(context, node)
        if (!sourceSnippet.isNullOrBlank()) {
            val explicitBySnippet = extractExplicitParameterName(sourceSnippet)
            if (!explicitBySnippet.isNullOrBlank()) {
                if (explicitBySnippet == "_") return false
                val bodyText = sourceSnippet.substringAfter("->", missingDelimiterValue = "")
                return containsIdentifier(bodyText, explicitBySnippet)
            }
            if (containsIdentifier(sourceSnippet, "it")) {
                return true
            }
        }

        val explicitParam = lambda.valueParameters.singleOrNull()
        if (explicitParam != null) {
            val parameterName = explicitParam.name
            if (parameterName == "_") return false

            var used = false
            lambda.body.accept(
                object : AbstractUastVisitor() {
                    override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression): Boolean {
                        if (node.identifier == parameterName && node.resolve() == explicitParam) {
                            used = true
                            return true
                        }
                        return super.visitSimpleNameReferenceExpression(node)
                    }
                },
            )
            return used
        }

        val lambdaText = lambda.sourcePsi?.text ?: lambda.asSourceString()
        val explicitByLambdaText = extractExplicitParameterName(lambdaText)
        if (!explicitByLambdaText.isNullOrBlank()) {
            if (explicitByLambdaText == "_") return false
            val bodyText = lambdaText.substringAfter("->", missingDelimiterValue = "")
            return containsIdentifier(bodyText, explicitByLambdaText)
        }

        val callText = node.sourcePsi?.text
        val explicitByCallText = callText?.let { extractExplicitParameterName(it) }
        if (!explicitByCallText.isNullOrBlank()) {
            if (explicitByCallText == "_") return false
            val bodyText = callText.substringAfter("->", missingDelimiterValue = "")
            return containsIdentifier(bodyText, explicitByCallText)
        }

        // Fallback for cases where UAST drops explicit lambda parameter info under K2.
        if (Regex("\\bpadding\\s*\\(").containsMatchIn(lambdaText)) {
            return true
        }

        // Kotlin implicit single-parameter lambda uses `it`.
        return containsIdentifier(lambdaText, "it")
    }

    private fun extractCallSnippet(context: JavaContext, node: UCallExpression): String? {
        val direct = node.sourcePsi?.text
        if (!direct.isNullOrBlank()) return direct

        val contents = context.getContents() ?: return null
        val start = context.getLocation(node).start?.offset ?: return null
        val end = minOf(contents.length, start + 240)
        return contents.substring(start, end)
    }

    private fun extractExplicitParameterName(text: String): String? {
        val match = Regex("\\{\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*->").find(text) ?: return null
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

