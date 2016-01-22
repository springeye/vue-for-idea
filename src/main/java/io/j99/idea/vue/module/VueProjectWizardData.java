package io.j99.idea.vue.module;

import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/1/22.
 */
public class VueProjectWizardData {
    static class Sdk {
        @NotNull public final String nodePath;
        @NotNull public final String vuePath;

        public Sdk(@NotNull String nodePath, @NotNull String vuePath) {
            this.nodePath = nodePath;
            this.vuePath = vuePath;
        }
    }
    @NotNull
    public final Sdk sdk;
    @NotNull
    public final VueProjectTemplate myTemplate;

    public VueProjectWizardData(@NotNull Sdk sdk, @NotNull VueProjectTemplate myTemplate) {
        this.sdk = sdk;
        this.myTemplate = myTemplate;
    }
}
