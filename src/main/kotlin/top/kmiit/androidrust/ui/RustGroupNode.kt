package top.kmiit.androidrust.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.icons.AllIcons

class RustGroupNode(
    project: Project,
    settings: ViewSettings?,
    private val childrenNodes: List<AbstractTreeNode<*>>,
    private val rootDir: VirtualFile?
) : ProjectViewNode<String>(project, "rust", settings) {

    override fun contains(file: VirtualFile): Boolean {
        return childrenNodes.any { child ->
             (child as? ProjectViewNode<*>)?.contains(file) == true
        }
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        return childrenNodes
    }

    override fun update(presentation: PresentationData) {
        presentation.presentableText = "rust"
        presentation.setIcon(AllIcons.Modules.SourceRoot)
    }

    override fun getVirtualFile(): VirtualFile? = rootDir

    override fun getTypeSortWeight(sortByType: Boolean): Int = -1

    override fun isAlwaysExpand(): Boolean = false
    override fun isAutoExpandAllowed(): Boolean = false
    override fun isIncludedInExpandAll(): Boolean = false
}
