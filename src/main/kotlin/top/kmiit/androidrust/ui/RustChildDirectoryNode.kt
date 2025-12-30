package top.kmiit.androidrust.ui

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ide.projectView.PresentationData
import com.intellij.icons.AllIcons

class RustChildDirectoryNode(
    project: Project, 
    val directory: PsiDirectory, 
    settings: ViewSettings?, 
    private val modulePath: String,
    private val libname: String?
) : ProjectViewNode<PsiDirectory>(project, directory, settings) {

    val ignoredNodes = listOf(
        "target",
        "Cargo.lock",
        ".gitignore"
    )

    override fun contains(file: VirtualFile): Boolean {
        return VfsUtilCore.isAncestor(directory.virtualFile, file, false)
    }

    override fun getVirtualFile(): VirtualFile = directory.virtualFile

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        val children = PsiDirectoryNode(project, directory, settings).children
        
        return children.filter { node ->
            when (node) {
                is PsiFileNode,
                is PsiDirectoryNode -> {
                    if (node.value.name in ignoredNodes) return@filter false
                }
            }
            true
        }
    }

    override fun update(presentation: PresentationData) {
        val displayName = if (libname != null) "$libname ($modulePath)" else modulePath
        presentation.presentableText = displayName
        presentation.setIcon(AllIcons.Nodes.Package)
    }

    override fun getTypeSortWeight(sortByType: Boolean): Int = 0

    override fun isAlwaysExpand(): Boolean = false
    override fun isAutoExpandAllowed(): Boolean = false
    override fun isIncludedInExpandAll(): Boolean = false
}
