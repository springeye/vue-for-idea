package io.j99.idea.vue.module;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import io.j99.idea.vue.VueIcons;
import io.j99.idea.vue.cli.nodejs.NodeRunner;
import io.j99.idea.vue.component.VueProjectSettingsComponent;
import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by apple on 16/1/22.
 */
public class VueModuleBuilder extends ModuleBuilder {
    private VueProjectWizardData myWizardData;

    void setWizardData(final VueProjectWizardData wizardData) {
        myWizardData = wizardData;
    }

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);
        final VirtualFile baseDir = contentEntry == null ? null : contentEntry.getFile();
        if (baseDir != null) {
            setupProject(modifiableRootModel, baseDir, myWizardData);
        }
    }

    static void setupProject(ModifiableRootModel modifiableRootModel,final VirtualFile baseDir, VueProjectWizardData wizardData) {
        final String templateName = wizardData.myTemplate.getName();
        UsageTrigger.trigger("VueProjectWizard." + templateName);
        setNodeAndVue(modifiableRootModel, wizardData);
        try {
            Collection<VirtualFile> files = wizardData.myTemplate.generateProject(wizardData, modifiableRootModel.getModule(), baseDir);
            ProgressManager.getInstance().run(new Task.Backgroundable(modifiableRootModel.getProject(),"Install Dependencies"){
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    final FileEditorManager manager = FileEditorManager.getInstance(modifiableRootModel.getModule().getProject());
                    for (VirtualFile file : files) {
                        if("package.json".equals(file.getName())){
                            GeneralCommandLine cmd = NodeRunner.createCommandLine(baseDir.getPath(), wizardData.sdk.nodePath, "/usr/local/bin/npm");
                            cmd.addParameter("i");
                            try {
                                ProcessOutput out = NodeRunner.execute(cmd, NodeRunner.TIME_OUT*10);
                                if(out.getExitCode()==0){
                                    System.out.println(out.getStdout());
                                }else{
                                    UsageTrigger.trigger(out.getStderr());
                                }
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }


    static void setNodeAndVue(ModifiableRootModel modifiableRootModel, VueProjectWizardData wizardData) {
        saveSettings(modifiableRootModel.getProject(), wizardData.sdk);
    }

    protected static SettingStorage getSettings(Project project) {
        return SettingStorage.getInstance(project);
    }

    protected static void saveSettings(Project project, VueProjectWizardData.Sdk sdk) {
        SettingStorage settingStorage = getSettings(project);
        settingStorage.vueExePath = sdk.vuePath;
        settingStorage.nodeInterpreter = sdk.nodePath;
        VueProjectSettingsComponent component = project.getComponent(VueProjectSettingsComponent.class);
        if (component != null) component.validateSettings();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    protected VueProjectWizardData.Sdk loadSettings(Project project) {
        if (project == null) return null;
        SettingStorage settingStorage = getSettings(project);
        return new VueProjectWizardData.Sdk(settingStorage.nodeInterpreter, settingStorage.vueExePath);
    }

    @Override
    public String getParentGroup() {
        return WebModuleBuilder.GROUP_NAME;
    }

    @Override
    public Icon getNodeIcon() {
        return VueIcons.VUE_ICON;
    }

    @Override
    public Icon getBigIcon() {
        return VueIcons.VUE_ICON;
    }

    @Override
    public String getName() {
        return "Vue";
    }

    @Override
    public String getPresentableName() {
        return "Vue";
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        VueModuleWizardStep step = new VueModuleWizardStep(context, loadSettings(context.getProject()));
        Disposer.register(parentDisposable, step);
        return step;
    }

    @Override
    public ModuleType getModuleType() {
        return WebModuleType.getInstance();
    }

    static void runWhenNonModalIfModuleNotDisposed(@NotNull final Runnable runnable, @NotNull final Module module) {
        StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
            @Override
            public void run() {
                if (ApplicationManager.getApplication().getCurrentModalityState() == ModalityState.NON_MODAL) {
                    runnable.run();
                } else {
                    ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL, new Condition() {
                        @Override
                        public boolean value(final Object o) {
                            return module.isDisposed();
                        }
                    });
                }
            }
        });
    }
}
