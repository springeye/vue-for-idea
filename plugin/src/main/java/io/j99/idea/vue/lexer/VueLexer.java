package io.j99.idea.vue.lexer;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.lexer._HtmlLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/6/14.
 */
public class VueLexer extends BaseHtmlLexer {

    public static final String INLINE_STYLE_NAME = "css-ruleset-block";


    private IElementType myTokenType;
    private int myTokenStart;
    private int myTokenEnd;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myTokenType = null;
        super.start(buffer, startOffset, endOffset, initialState);
    }

    public void advance() {
        myTokenType = null;
        super.advance();
    }

    public IElementType getTokenType() {
        if (myTokenType != null) return myTokenType;
        IElementType tokenType = super.getTokenType();

        myTokenStart = super.getTokenStart();
        myTokenEnd = super.getTokenEnd();

        if (hasSeenStyle()) {
            if (hasSeenTag() && isStartOfEmbeddmentTagContent(tokenType)) {
                Language stylesheetLanguage = getStyleLanguage();
                if (stylesheetLanguage == null || LanguageUtil.isInjectableLanguage(stylesheetLanguage)) {
                    myTokenEnd = skipToTheEndOfTheEmbeddment();
                    IElementType currentStylesheetElementType = getCurrentStylesheetElementType();
                    tokenType = currentStylesheetElementType == null ? XmlTokenType.XML_DATA_CHARACTERS : currentStylesheetElementType;
                }
            }
        } else if (hasSeenScript()) {
            if (hasSeenTag() && isStartOfEmbeddmentTagContent(tokenType)) {
                Language scriptLanguage = getScriptLanguage();
                if (scriptLanguage == null || LanguageUtil.isInjectableLanguage(scriptLanguage)) {
                    myTokenEnd = skipToTheEndOfTheEmbeddment();
                    IElementType currentScriptElementType = getCurrentScriptElementType();
                    tokenType = currentScriptElementType == null ? XmlTokenType.XML_DATA_CHARACTERS : currentScriptElementType;
                }
            } else if (hasSeenAttribute() && isStartOfEmbeddmentAttributeValue(tokenType)) {
                myTokenEnd = skipToTheEndOfTheEmbeddment();
            }
        }

        return myTokenType = tokenType;
    }

    private static boolean isStartOfEmbeddmentAttributeValue(final IElementType tokenType) {
        return tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
    }

    private static boolean isStartOfEmbeddmentTagContent(final IElementType tokenType) {
        return (tokenType == XmlTokenType.XML_DATA_CHARACTERS ||
                tokenType == XmlTokenType.XML_CDATA_START ||
                tokenType == XmlTokenType.XML_COMMENT_START ||
                tokenType == XmlTokenType.XML_REAL_WHITE_SPACE || tokenType == TokenType.WHITE_SPACE
        );
    }

    public VueLexer() {
        this(new MergingLexerAdapter(new FlexAdapter(new _HtmlLexer()), TOKENS_TO_MERGE), true);
    }

    protected VueLexer(Lexer _baseLexer, boolean _caseInsensitive) {
        super(_baseLexer, _caseInsensitive);
    }

    protected boolean isHtmlTagState(int state) {
        return state == _HtmlLexer.START_TAG_NAME || state == _HtmlLexer.END_TAG_NAME;
    }

    public int getTokenStart() {
        if (myTokenType != null) {
            return myTokenStart;
        }
        return super.getTokenStart();
    }

    public int getTokenEnd() {
        if (myTokenType != null) {
            return myTokenEnd;
        }
        return super.getTokenEnd();
    }
}
