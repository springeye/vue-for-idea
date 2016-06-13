package io.j99.idea.vue;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.psi.JSEmbeddedContent;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.css.CssStylesheet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.sass.SASSLanguage;
import org.jetbrains.plugins.scss.SCSSLanguage;

import java.util.Arrays;
import java.util.List;

/**
 * Created by apple on 16/6/1.
 */
public class VueLanguageInjector2 implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!context.isValid() || (!(context instanceof JSEmbeddedContent) && !(context instanceof CssStylesheet) && !(context instanceof XmlTag))) {
            System.out.println("continue LanguagesToInject");
            return;
        }
        if (context instanceof XmlTag) {
            injectTemplate(registrar, context);
        } else if (context instanceof JSEmbeddedContent) {
            injectScript(registrar, context);
        } else {
            injectStyle(registrar, context);
        }
    }

    private void injectStyle(MultiHostRegistrar registrar, PsiElement context) {
        XmlTag tag = PsiTreeUtil.getParentOfType(context, XmlTag.class);
        final String lang;
        if (tag != null) {
            System.out.println(tag);
            XmlAttribute langAttr = tag.getAttribute("lang");
            if (langAttr != null) {
                lang = langAttr.getValue();
            } else {
                lang = "css";
            }
        } else {
            lang = null;
        }
        Language language = null;
        if ("scss".equals(lang)) {
            language = SCSSLanguage.INSTANCE;
        } else if ("sass".equals(lang)) {
            language = SASSLanguage.INSTANCE;
        }
        if (language != null) {
            System.out.println("Inject language " + language.getDisplayName());
            registrar.startInjecting(language)
                    .addPlace(null, null, (PsiLanguageInjectionHost) tag, TextRange.create(0, tag.getTextLength()));
        }

    }

    private Language injectScript(MultiHostRegistrar registrar, PsiElement context) {
        JSEmbeddedContent jsContent = PsiTreeUtil.getParentOfType(context, JSEmbeddedContent.class);
        return null;
    }

    private Language injectTemplate(MultiHostRegistrar registrar, PsiElement context) {
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(context, XmlTag.class);
        return null;
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Arrays.asList(XmlTag.class, JSEmbeddedContent.class, CssStylesheet.class);
    }
}
