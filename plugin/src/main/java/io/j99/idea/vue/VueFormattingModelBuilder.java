package io.j99.idea.vue;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.xml.XmlFormattingModel;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.xml.XmlBlock;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import io.j99.idea.vue.formatter.VuePolicy;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/6/15.
 */
public class VueFormattingModelBuilder implements FormattingModelBuilder {
    @Override
    @NotNull
    public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
        final PsiFile psiFile = element.getContainingFile();
        final FormattingDocumentModelImpl documentModel = FormattingDocumentModelImpl.createOn(psiFile);
        return new XmlFormattingModel(psiFile,
                new XmlBlock(SourceTreeToPsiMap.psiElementToTree(psiFile),
                        null, null, new VuePolicy(settings, documentModel), null, null, false),
                documentModel);
    }

    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }
}
