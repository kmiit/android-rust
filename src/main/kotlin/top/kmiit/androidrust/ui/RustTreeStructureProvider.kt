package top.kmiit.androidrust.ui

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.facet.FacetManager
import com.intellij.ide.util.treeView.AbstractTreeNode
import top.kmiit.androidrust.rust.RustCargoParser
import top.kmiit.androidrust.rust.RustModuleResolver
import org.jetbrains.kotlin.psi.KtFile
import com.intellij.psi.PsiDirectory

class RustTreeStructureProvider : TreeStructureProvider {
    override fun modify(parent: AbstractTreeNode<*>, children: MutableCollection<AbstractTreeNode<*>>, settings: ViewSettings): MutableCollection<AbstractTreeNode<*>> {
        val project = parent.project ?: return children
        
        val module = getModuleForNode(parent)
        if (module != null) {
            val isRootNode = isModuleRootNode(parent, module)
            
            if (isRootNode && isAndroidModule(module)) {
                val roots = ModuleRootManager.getInstance(module).contentRoots
                var gradleKt: VirtualFile? = null
                var moduleRoot: VirtualFile? = null
                
                for (root in roots) {
                    val f = root.findChild("build.gradle.kts")
                    if (f != null) {
                        gradleKt = f
                        moduleRoot = root
                        break
                    }
                }

                if (gradleKt != null && moduleRoot != null) {
                    val psiFile = PsiManager.getInstance(project).findFile(gradleKt)
                    if (psiFile is KtFile) {
                        val config = RustCargoParser.parse(psiFile)
                        val modulePath = config?.modulePath
                        
                        if (modulePath != null) {
                            val resolvedDir = RustModuleResolver.resolve(project, moduleRoot, modulePath)
                            if (resolvedDir != null) {
                                val psiDir = PsiManager.getInstance(project).findDirectory(resolvedDir)
                                if (psiDir != null) {
                                    if (children.any { it is RustGroupNode }) return children

                                    val rustNode = RustChildDirectoryNode(project, psiDir, settings, modulePath, config.libname)
                                    val groupNode = RustGroupNode(project, settings, listOf(rustNode), resolvedDir)
                                    
                                    val newChildren = ArrayList<AbstractTreeNode<*>>(children.size + 1)
                                    newChildren.add(groupNode)
                                    newChildren.addAll(children)
                                    return newChildren
                                }
                            }
                        }
                    }
                }
            }
        }
        return children
    }

    private fun getModuleForNode(node: AbstractTreeNode<*>): Module? {
        val value = node.value
        if (value is Module) return value
        if (node is ProjectViewModuleNode) return node.value
        if (value is PsiDirectory) {
            return ModuleUtilCore.findModuleForPsiElement(value)
        }
        return null
    }

    private fun isModuleRootNode(node: AbstractTreeNode<*>, module: Module): Boolean {
        if (node is ProjectViewModuleNode) return true
        val value = node.value
        if (value is PsiDirectory) {
            val roots = ModuleRootManager.getInstance(module).contentRoots
            return roots.any { it == value.virtualFile }
        }
        return false
    }

    private fun isAndroidModule(module: Module): Boolean {
        val facetManager = FacetManager.getInstance(module)
        if (facetManager.allFacets.any { it.typeId.toString().contains("android", ignoreCase = true) || it.name.contains("Android") }) return true

        val roots = ModuleRootManager.getInstance(module).contentRoots
        for (r in roots) {
            if (r.findChild("res") != null && r.findChild("src") != null) return true
            if (r.findChild("src")?.findChild("main")?.findChild("AndroidManifest.xml") != null) return true
            if (r.findChild("AndroidManifest.xml") != null) return true
        }
        return false
    }
}
