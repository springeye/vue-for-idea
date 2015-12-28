package io.j99.idea.vue.psi

import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.impl.source.html.HtmlFileImpl
import io.j99.idea.vue.VueFileType

/**
 * Created by android on 15-12-25.
 */
class VueFile(fileViewProvider: FileViewProvider) : HtmlFileImpl(fileViewProvider) {

    override fun getFileType(): FileType {
        return VueFileType.INSTANCE
    }
}
