package io.j99.idea.vue;

import com.intellij.lang.Language;

/**
 * Created by apple on 16/6/14.
 */
public class VueLanguage extends Language {
    public static final VueLanguage INSTANCE = new VueLanguage();

    private VueLanguage() {
        super("Vue");
    }
}
