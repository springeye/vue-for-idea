package io.j99.idea.vue;

import com.intellij.lang.xml.XMLLanguage;

/**
 * Created by apple on 16/6/1.
 */
public class VueLanguage extends XMLLanguage {
    public static final VueLanguage INSTANCE = new VueLanguage();

    private VueLanguage() {
        super(XMLLanguage.INSTANCE, "VUE");
    }
}
