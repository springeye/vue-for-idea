package io.j99.idea.vue.settings;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.JTextComponent;

public class ValidationInfo {
    public static final String LINK_TEMPLATE = "{{LINK}}";
    private static final Logger LOG = Logger.getInstance(ValidationInfo.class);
    private final JTextComponent textComponent;
    private final String errorHtmlDescription;
    private final String linkText;

    public ValidationInfo(@Nullable JTextComponent textComponent, @NotNull String errorHtmlDescriptionTemplate, @NotNull String linkText) {
        this.textComponent = textComponent;
        if (!errorHtmlDescriptionTemplate.contains(LINK_TEMPLATE)) {
            LOG.warn("Cannot find {{LINK}} in " + errorHtmlDescriptionTemplate);
        }
        String linkHtml = "<a href='" + linkText + "'>" + linkText + "</a>";
        this.errorHtmlDescription = errorHtmlDescriptionTemplate.replace(LINK_TEMPLATE, linkHtml);
        this.linkText = linkText;
    }

    @Nullable
    public JTextComponent getTextComponent() {
        return this.textComponent;
    }

    @NotNull
    public String getErrorHtmlDescription() {
        return this.errorHtmlDescription;
    }

    @Nullable
    public String getLinkText() {
        return this.linkText;
    }
}