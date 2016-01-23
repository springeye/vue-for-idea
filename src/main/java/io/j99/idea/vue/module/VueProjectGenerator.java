package io.j99.idea.vue.module;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/1/22.
 */
public class VueProjectGenerator extends WebProjectTemplate<VueProjectWizardData> implements Comparable<VueProjectGenerator> {
    @Override
    public int compareTo(VueProjectGenerator o) {
        return 0;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void generateProject(@NotNull final Project project,
                                @NotNull final VirtualFile baseDir,
                                @NotNull final VueProjectWizardData data,
                                @NotNull final Module module) {
        ApplicationManager.getApplication().runWriteAction(
                new Runnable() {
                    public void run() {
                        final ModifiableRootModel modifiableModel = ModifiableModelsProvider.SERVICE.getInstance().getModuleModifiableModel(module);
                        VueModuleBuilder.setupProject(modifiableModel, baseDir, data);
                        ModifiableModelsProvider.SERVICE.getInstance().commitModuleModifiableModel(modifiableModel);

                    }
                });
    }

    @NotNull
    @Override
    public GeneratorPeer<VueProjectWizardData> createPeer() {
        return null;
    }
}
