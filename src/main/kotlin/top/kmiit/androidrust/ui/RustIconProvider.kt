package top.kmiit.androidrust.ui

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiDirectory
import javax.swing.Icon

class RustIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        when (element) {
            is PsiFile -> {
                if (element.name.endsWith(".rs")) {
                    return AllIcons.Language.Rust
                }
            }
            is PsiDirectory -> {
            }
        }
        return null
    }
}
