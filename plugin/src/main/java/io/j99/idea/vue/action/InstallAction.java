package io.j99.idea.vue.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.j99.idea.vue.cli.NpmUtils;
import io.j99.idea.vue.module.VueProjectWizardData;
import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/1/23.
 */
public class InstallAction extends AnAction {
    protected VueProjectWizardData.Sdk loadSettings() {
        SettingStorage settingStorage = getSettings();
        return new VueProjectWizardData.Sdk(settingStorage.nodeExePath, settingStorage.npmExePath, settingStorage.vueExePath);
    }

    protected static SettingStorage getSettings() {
        return SettingStorage.getInstance();
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        performInstallAction(project);

    }

    public void performInstallAction(Project project) {
        VueProjectWizardData.Sdk settings = loadSettings();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Install Dependencies") {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                VirtualFile baseDir = project.getBaseDir();
                if (StringUtil.isNotEmpty(settings.npmPath)) {
                    NpmUtils.packageInstall(progressIndicator, baseDir.getPath(), settings.nodePath, settings.npmPath);
                }
            }
        });
    }
}
