package io.j99.idea.vue;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/6/14.
 */
public class VueFile extends PsiFileBase {
    public VueFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, VueLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return VueFileType.INSTANCE;
    }
}
