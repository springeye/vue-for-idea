package io.j99.idea.vue.lexer;

import com.intellij.lang.Language;
import com.intellij.lexer.HtmlHighlightingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Created by apple on 16/6/13.
 */
public class VueHighlightingLexer extends HtmlHighlightingLexer {
    private String styleType;
    private Lexer embeddedLexer;
    private String htmlType;

    public VueHighlightingLexer() {
        XmlNameHandler value = new XmlNameHandler();
        registerHandler(XmlTokenType.XML_NAME, value);
        registerHandler(XmlTokenType.XML_TAG_NAME, value);
        registerHandler(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, new XmlAttributeValueEndHandler());
        registerHandler(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, new XmlAttributeValueHandler());
    }

    private boolean seenScript;
    private boolean seenStyle;
    private boolean seenTemplate;

    public class XmlNameHandler implements TokenHandler {
        @NonNls
        private static final String TOKEN_TEMPLATE = "template";
        @NonNls
        private static final String TOKEN_SCRIPT = "script";
        @NonNls
        private static final String TOKEN_STYLE = "style";

        @Override
        public void handleElement(Lexer lexer) {
            String name = TreeUtil.getTokenText(lexer);
            seenTemplate = TOKEN_TEMPLATE.equals(name);
            seenScript = TOKEN_SCRIPT.equals(name);
            seenStyle = TOKEN_STYLE.equals(name);
        }
    }

    class XmlAttributeValueHandler implements TokenHandler {
        @Override
        public void handleElement(Lexer lexer) {
            if (seenScript) {
                scriptType = TreeUtil.getTokenText(lexer);
            }
            if (seenStyle) {
                styleType = TreeUtil.getTokenText(lexer).trim();
            }
            if (seenTemplate) {
                htmlType = TreeUtil.getTokenText(lexer).trim();
            }
        }
    }

    class XmlAttributeValueEndHandler implements TokenHandler {
        @Override
        public void handleElement(Lexer lexer) {
            seenStyle = false;
            seenScript = false;
        }
    }

    protected static final Language ourDefaultStyleLanguage = Language.findLanguageByID("CSS");
    private static final int EMBEDDED_LEXER_ON = 0x1 << BASE_STATE_SHIFT;
    private static final int EMBEDDED_LEXER_STATE_SHIFT = BASE_STATE_SHIFT + 1;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        super.start(buffer, startOffset, endOffset, initialState);
        if ((initialState & EMBEDDED_LEXER_ON) != 0) {
            int state = initialState >> EMBEDDED_LEXER_STATE_SHIFT;
            setEmbeddedLexer();
            embeddedLexer.start(buffer, startOffset, skipToTheEndOfTheEmbeddment(), state);
        }
    }


    @Nullable
    protected Language getStyleLanguage() {
        if (styleType != null) {
            String languageName = styleType;
            for (Language language : ourDefaultStyleLanguage.getDialects()) {
                if (languageName.equals(language.getID().toLowerCase(Locale.US))) {
                    return language;
                }
            }
        }
        return ourDefaultStyleLanguage;
    }

    private void setEmbeddedLexer() {
        if (seenStyle) {
            Lexer styleLexer = SyntaxHighlighterFactory.getSyntaxHighlighter(getStyleLanguage(), null, null).getHighlightingLexer();
            embeddedLexer = styleLexer;
        }
    }
}
