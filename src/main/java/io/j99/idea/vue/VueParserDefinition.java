package io.j99.idea.vue;

import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import io.j99.idea.vue.psi.VueFile;

/**
 * Created by android on 15-12-25.
 */
public class VueParserDefinition extends com.intellij.lang.xhtml.XHTMLParserDefinition {
    @Override
    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new VueFile(fileViewProvider);
    }
}
