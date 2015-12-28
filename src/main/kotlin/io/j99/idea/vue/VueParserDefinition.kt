package io.j99.idea.vue

import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import io.j99.idea.vue.psi.VueFile

/**
 * Created by android on 15-12-25.
 */
class VueParserDefinition : com.intellij.lang.xhtml.XHTMLParserDefinition() {
    override fun createFile(fileViewProvider: FileViewProvider): PsiFile {
        return VueFile(fileViewProvider)
    }
}
