package jp.developer.bbee.richuidemo.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.visitor.AbstractUastVisitor

class MyScaffoldPaddingDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("MyScaffold")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: com.intellij.psi.PsiMethod) {
        if (!isTargetMyScaffold(method)) return

        // Only inline lambdas can be analyzed here. Content passed as a function reference or
        // variable is silently skipped because resolving non-inline callables requires navigating
        // to their definition, which is beyond the scope of this detector.
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
            // Use UAST to detect identifier usage rather than raw text scan to avoid
            // false negatives from string literals or comments containing the param name.
            return isNameReferencedInLambda(lambda, explicitByLambdaText)
        }

        // Kotlin implicit single-parameter lambda uses `it`.
        return isItReferencedInLambda(lambda)
    }

    private fun isNameReferencedInLambda(lambda: ULambdaExpression, name: String): Boolean {
        var found = false
        lambda.body.accept(object : AbstractUastVisitor() {
            override fun visitLambdaExpression(node: ULambdaExpression): Boolean {
                // Stop traversal into nested lambdas that declare a parameter with the same name,
                // which would shadow the outer padding parameter.
                return node.valueParameters.any { it.name == name }
            }
            override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression): Boolean {
                if (node.identifier == name) {
                    found = true
                    return true
                }
                return super.visitSimpleNameReferenceExpression(node)
            }
        })
        return found
    }

    private fun isItReferencedInLambda(lambda: ULambdaExpression): Boolean {
        val outerLambdaPsi = lambda.sourcePsi
        var found = false
        lambda.body.accept(object : AbstractUastVisitor() {
            override fun visitLambdaExpression(node: ULambdaExpression): Boolean {
                // Stop traversal into nested lambdas with explicit parameters — they shadow `it`.
                return node.valueParameters.isNotEmpty()
            }
            override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression): Boolean {
                if (node.identifier == "it") {
                    val resolved = node.resolve()
                    // If `it` resolves to a parameter defined inside a nested lambda (e.g.,
                    // `items(list) { Text(it.name) }` where `it` is the list item), do not
                    // count it as the outer scaffold padding. Parameterless lambdas that simply
                    // capture the outer `it` (e.g., `remember { Modifier.padding(it) }`) will
                    // have `it` resolve to the outer lambda's implicit parameter instead.
                    if (outerLambdaPsi != null && resolved != null &&
                        isDefinedInNestedLambda(resolved, outerLambdaPsi)) {
                        return super.visitSimpleNameReferenceExpression(node)
                    }
                    found = true
                    return true
                }
                return super.visitSimpleNameReferenceExpression(node)
            }
        })
        return found
    }

    private fun isDefinedInNestedLambda(element: PsiElement, outerLambdaPsi: PsiElement): Boolean {
        var current: PsiElement? = element.parent
        while (current != null && current !== outerLambdaPsi) {
            if (current is KtLambdaExpression) return true
            current = current.parent
        }
        return false
    }

    private fun extractExplicitParameterName(text: String): String? {
        val match = Regex("^\\{\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*->").find(text) ?: return null
        return match.groupValues.getOrNull(1)
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

