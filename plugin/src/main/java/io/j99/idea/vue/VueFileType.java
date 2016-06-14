package io.j99.idea.vue;

import com.intellij.lang.xhtml.XHTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by apple on 16/2/26.
 */
public class VueFileType extends LanguageFileType {
    public static final VueFileType INSTANCE = new VueFileType();
    public static final String[] DEFAULT_ASSOCIATED_EXTENSIONS = {"vue"};

    protected VueFileType() {
        super(VueLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return VueBundle.message("vue.filetype.name");
    }

    @NotNull
    @Override
    public String getDescription() {
        return VueBundle.message("vue.filetype.description");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_ASSOCIATED_EXTENSIONS[0];
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return VueIcons.VUE_ICON;
    }
}
