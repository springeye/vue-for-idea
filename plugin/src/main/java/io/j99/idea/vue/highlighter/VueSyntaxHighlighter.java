package io.j99.idea.vue.highlighter;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import io.j99.idea.vue.lexer.VueHighlightingLexer;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/2/26.
 */
public class VueSyntaxHighlighter extends HtmlFileHighlighter {
    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new VueHighlightingLexer(/*FileTypeRegistry.getInstance().findFileTypeByName("CSS")*/);
    }
}
