package io.j99.idea.vue;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.xhtml.XHTMLLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by apple on 16/6/1.
 */
public class VueLanguageInjector implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!context.isValid() || !(context instanceof XmlText)) {
            return;
        }
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(context, XmlTag.class, true);
        if (xmlTag == null) return;
        String lang = xmlTag.getAttributeValue("lang");
        DomElement domElement = DomUtil.getDomElement(xmlTag);
        if (lang == null) {
            String eleName = xmlTag.getName();
            if (eleName.equals("template")) {
                registrar.startInjecting(XHTMLLanguage.INSTANCE)
                        .addPlace(null, null, (PsiLanguageInjectionHost) context, TextRange.create(0, context.getTextLength()));
            } else if (eleName.equals("script")) {
                registrar.startInjecting(JavascriptLanguage.INSTANCE)
                        .addPlace(null, null, (PsiLanguageInjectionHost) context, TextRange.create(0, context.getTextLength()));
            }
        } else {
            if (lang.startsWith("javascript")) {
                Collection<Language> languages = JavascriptLanguage.getRegisteredLanguages();
                System.out.println(Arrays.toString(languages.toArray()));
                registrar.startInjecting(JavascriptLanguage.INSTANCE)
                        .addPlace(null, null, (PsiLanguageInjectionHost) context, TextRange.create(0, context.getTextLength()));
            } else if (lang.startsWith("html")) {
                registrar.startInjecting(XHTMLLanguage.INSTANCE)
                        .addPlace(null, null, (PsiLanguageInjectionHost) context, TextRange.create(0, context.getTextLength()));
            } else if (lang.startsWith("es6") || lang.startsWith("babel")) {
                registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_6)
                        .addPlace(null, null, (PsiLanguageInjectionHost) context, TextRange.create(0, context.getTextLength()));
            }
        }
    }


    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(XmlText.class);
    }
}
