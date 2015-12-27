package io.j99.idea.vue

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

/**
 * Created by android on 15-12-25.
 */
class VueFileType : LanguageFileType(HTMLLanguage.INSTANCE) {
    override fun getName(): String {
        return VueBundle.message("vue.filetype.name")
    }

    override fun getDescription(): String {
        return VueBundle.message("vue.filetype.description")
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_ASSOCIATED_EXTENSIONS[0]
    }

    override fun getIcon(): Icon? {
        return VueIcons.VUE_ICON
    }

    companion object {
        @JvmField val INSTANCE = VueFileType()
        @NonNls
        val DEFAULT_ASSOCIATED_EXTENSIONS = arrayOf("vue")
    }
}
