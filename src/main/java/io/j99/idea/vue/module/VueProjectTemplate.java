package io.j99.idea.vue.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by apple on 16/1/22.
 */
public abstract class VueProjectTemplate {
    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    private final String name;
    private final String description;

    public VueProjectTemplate(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public abstract Collection<VirtualFile> generateProject(@NotNull final VueProjectWizardData.Sdk sdk,
                                                            @NotNull final Module module,
                                                            @NotNull final VirtualFile baseDir)
            throws IOException;
}
