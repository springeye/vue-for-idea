package io.j99.idea.vue;

import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Created by apple on 16/6/1.
 */
public class VueLanguageInjector extends VueLanguageInjector2 {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement element) {
        if (!element.isValid() || !(element instanceof XmlTag)) {
            System.out.println("continue LanguagesToInject");
            return;
        }
        ASTNode fristChild = element.getNode();
        System.out.println(fristChild.getElementType());
    }


    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(XmlTag.class);
    }
}
