package io.j99.idea.vue.psi;

import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import io.j99.idea.vue.VueLanguage;
import io.j99.idea.vue.lexer.VueLexer;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/6/14.
 */
public class VueParserDefinition extends HTMLParserDefinition {

    public static final IStubFileElementType elementType = new IStubFileElementType(VueLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new VueLexer();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return elementType;
    }

    @Override
    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new VueFileImpl(fileViewProvider);
    }
}
