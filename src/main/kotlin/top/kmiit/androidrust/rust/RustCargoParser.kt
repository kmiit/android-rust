package top.kmiit.androidrust.rust

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

data class RustCargoConfig(
    val modulePath: String? = null,
    val libname: String? = null
)

object RustCargoParser {
    fun parse(psiFile: PsiFile): RustCargoConfig? {
        if (psiFile !is KtFile) return null

        var modulePath: String? = null
        var libname: String? = null
        var foundCargo = false

        psiFile.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                if (modulePath != null && libname != null) return
                super.visitCallExpression(expression)

                if (expression.isCallByName("cargo")) {
                    foundCargo = true
                    val lambdaBody = expression.lambdaArguments.firstOrNull()?.getLambdaExpression()?.bodyExpression
                    lambdaBody?.statements?.forEach { stmt ->
                        if (stmt is KtBinaryExpression && stmt.operationToken == KtTokens.EQ) {
                            if (stmt.left?.text == "module") {
                                modulePath = stmt.right?.asStringLiteral()
                            } else if (stmt.left?.text == "libname") {
                                libname = stmt.right?.asStringLiteral()
                            }
                        }
                        else if (stmt is KtCallExpression) {
                             if (stmt.isCallByName("module")) {
                                modulePath = stmt.firstStringArgument()
                            } else if (stmt.isCallByName("libname")) {
                                libname = stmt.firstStringArgument()
                            }
                        }
                        else if (stmt is KtDotQualifiedExpression && stmt.selectorExpression?.isCallByName("set") == true) {
                            if (stmt.receiverExpression.text == "module") {
                                modulePath = (stmt.selectorExpression as? KtCallExpression)?.firstStringArgument()
                            } else if (stmt.receiverExpression.text == "libname") {
                                libname = (stmt.selectorExpression as? KtCallExpression)?.firstStringArgument()
                            }
                        }
                    }
                }
            }

            override fun visitBinaryExpression(expression: KtBinaryExpression) {
                if (modulePath != null && libname != null) return
                super.visitBinaryExpression(expression)

                if (expression.operationToken == KtTokens.EQ) {
                    if (expression.left?.text == "cargo.module") {
                        foundCargo = true
                        modulePath = expression.right?.asStringLiteral()
                    }
                    if (expression.left?.text == "cargo.libname") {
                        foundCargo = true
                        libname = expression.right?.asStringLiteral()
                    }
                }
            }
        })
        if (modulePath == null && libname == null && !foundCargo) return null
        
        return RustCargoConfig(modulePath, libname)
    }

    private fun KtExpression?.isCallByName(name: String): Boolean {
        return this is KtCallExpression && this.calleeExpression?.text == name
    }

    private fun KtExpression.asStringLiteral(): String? {
        if (this !is KtStringTemplateExpression) return null
        if (entries.any { it !is KtLiteralStringTemplateEntry }) return null
        return entries.joinToString("") { it.text }
    }

    private fun KtCallExpression.firstStringArgument(): String? {
        return valueArguments.firstOrNull()?.getArgumentExpression()?.asStringLiteral()
    }
}
