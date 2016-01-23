package io.j99.idea.vue.action;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.j99.idea.vue.cli.nodejs.NodeRunner;
import io.j99.idea.vue.module.VueProjectWizardData;
import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/1/23.
 */
public class InstallAction extends AnAction {
    protected VueProjectWizardData.Sdk loadSettings() {
        SettingStorage settingStorage = getSettings();
        return new VueProjectWizardData.Sdk(settingStorage.nodeInterpreter, settingStorage.vueExePath);
    }

    protected static SettingStorage getSettings() {
        return SettingStorage.getInstance();
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        performInstallAction(project);

    }

    private void performInstallAction(Project project) {
        VueProjectWizardData.Sdk settings = loadSettings();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Install Dependencies") {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                VirtualFile baseDir = project.getBaseDir();
                GeneralCommandLine cmd = NodeRunner.createCommandLine(baseDir.getPath(), settings.nodePath, "/usr/local/bin/npm");
                cmd.addParameter("i");
                try {
                    ProcessOutput out = NodeRunner.execute(cmd,new NodeRunner.ProcessListener() {
                        @Override
                        public void onError(OSProcessHandler processHandler, String text) {
                        }

                        @Override
                        public void onOutput(OSProcessHandler processHandler, String text) {
                            progressIndicator.setText(text);
                        }

                        @Override
                        public void onCommand(OSProcessHandler processHandler, String text) {

                        }
                    }, NodeRunner.TIME_OUT * 10);
                    if (out.getExitCode() == 0) {
                        System.out.println(out.getStdout());
                    } else {
                        UsageTrigger.trigger(out.getStderr());
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
