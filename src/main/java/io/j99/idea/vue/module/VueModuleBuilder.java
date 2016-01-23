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
import java.io.OutputStream;
import java.util.Collection;
import java.util.TimerTask;

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
//        try {
//            Collection<VirtualFile> files = wizardData.myTemplate.generateProject(wizardData, modifiableRootModel.getModule(), baseDir);

            ProgressManager.getInstance().run(new Task.Backgroundable(modifiableRootModel.getProject(),"Create Files"){

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    GeneralCommandLine cmd = NodeRunner.createCommandLine(baseDir.getPath(), wizardData.sdk.nodePath, wizardData.sdk.vuePath);
                    cmd.addParameter("init");
                    cmd.addParameter(wizardData.myTemplate.getName());
                    Module module = modifiableRootModel.getModule();
                    cmd.addParameter(module.getName());
                    try {
                        NodeRunner.ProcessListener listener=new NodeRunner.ProcessListener() {
                            @Override
                            public void onError(OSProcessHandler processHandler, String text) {

                            }

                            @Override
                            public void onOutput(OSProcessHandler processHandler, String text) {
                                if(text.startsWith("Project name") || text.startsWith("Project description:")||text.startsWith("Author")||text.startsWith("private")){
                                    try {
                                        System.out.println(text);
                                        OutputStream processInput = processHandler.getProcessInput();
                                        processInput.write(System.getProperty("line.separator").getBytes());
                                        processInput.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onCommand(OSProcessHandler processHandler, String text) {

                            }
                        };
                        ProcessOutput out = NodeRunner.execute(cmd,listener, NodeRunner.TIME_OUT);
                        if(out.getExitCode()==0){
                            System.out.println(out.getStdout());

                            install(baseDir.getPath()+module.getName(),module,wizardData);
                        }else{
                            UsageTrigger.trigger(out.getStderr());
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
            saveSettings(wizardData.sdk);
    }

    private static void install(String cwd,Module module,VueProjectWizardData data) {
        GeneralCommandLine cmd = NodeRunner.createCommandLine(cwd, data.sdk.nodePath, "/usr/local/bin/npm");
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


    static void setNodeAndVue(ModifiableRootModel modifiableRootModel, VueProjectWizardData wizardData) {
        saveSettings(wizardData.sdk);
    }

    protected static void saveSettings(VueProjectWizardData.Sdk sdk) {
        SettingStorage settingStorage = SettingStorage.getInstance();
        settingStorage.vueExePath = sdk.vuePath;
        settingStorage.nodeInterpreter = sdk.nodePath;
    }

    protected VueProjectWizardData.Sdk loadSettings() {
        SettingStorage settingStorage = SettingStorage.getInstance();
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
        VueModuleWizardStep step = new VueModuleWizardStep(context, loadSettings());
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
