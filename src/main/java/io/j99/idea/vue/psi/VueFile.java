package io.j99.idea.vue.psi;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import io.j99.idea.vue.VueFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/2/26.
 */
public class VueFile extends HtmlFileImpl {
    public VueFile(FileViewProvider fileViewProvider) {
        super(fileViewProvider);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return VueFileType.INSTANCE;
    }
}
