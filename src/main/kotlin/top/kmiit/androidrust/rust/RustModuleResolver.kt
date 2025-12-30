package top.kmiit.androidrust.rust

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

object RustModuleResolver {
    fun resolve(project: Project, gradleModuleDir: VirtualFile?, moduleName: String): VirtualFile? {
        val maybeFile = File(moduleName)
        if (maybeFile.isAbsolute && maybeFile.exists()) {
            return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(maybeFile)
        }

        if (gradleModuleDir != null) {
            val candidate = File(gradleModuleDir.path, moduleName)
            if (candidate.exists()) return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(candidate)
        }

        val base = project.basePath
        if (base != null) {
            val candidate = File(base, moduleName)
            if (candidate.exists()) return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(candidate)
        }

        return null
    }
}

