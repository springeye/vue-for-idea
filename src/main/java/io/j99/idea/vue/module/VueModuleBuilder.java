package io.j99.idea.vue.module;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
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
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import io.j99.idea.vue.VueIcons;
import io.j99.idea.vue.cli.nodejs.NodeRunner;
import io.j99.idea.vue.component.VueProjectSettingsComponent;
import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by apple on 16/1/22.
 */
public class VueModuleBuilder extends ModuleBuilder {
    private VueProjectWizardData myWizardData;
    private static final Logger LOG = Logger.getInstance(VueModuleBuilder.class);
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
    protected static File createTemp() throws IOException {
        return FileUtil.createTempDirectory("intellij-vue-generator", null, false);
    }
    protected static void deleteTemp(File tempProject) {
        if (!FileUtil.delete(tempProject)) {
            LOG.warn("Cannot delete " + tempProject);
        }
        else {
            LOG.info("Successfully deleted " + tempProject);
        }
    }
    static void setupProject(final ModifiableRootModel modifiableRootModel,final VirtualFile baseDir, final VueProjectWizardData wizardData) {
        final String templateName = wizardData.myTemplate.getName();
        final Module module = modifiableRootModel.getModule();
        final String moduleName = module.getName();
        UsageTrigger.trigger("VueProjectWizard." + templateName);
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            @Override
            public void run() {

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
                    ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
                    progressIndicator.setText("Creating...");
                    File tempProject = createTemp();
                    GeneralCommandLine cmd = NodeRunner.createCommandLine(tempProject.getPath(), wizardData.sdk.nodePath, wizardData.sdk.vuePath);
                    cmd.addParameter("init");
                    cmd.addParameter(wizardData.myTemplate.getName());
                    cmd.addParameter(moduleName);


                    String fullMessage = "Creating project files, please waiting ...";
                    String title = "Create Vue Project";
                    Notifications.Bus.notify(
                            new Notification("Vue Generator", title, fullMessage, NotificationType.INFORMATION)
                    );

                    ProcessOutput out = NodeRunner.execute(cmd,listener, NodeRunner.TIME_OUT);
                    if(out.getExitCode()==0){
                        setNodeAndVue(modifiableRootModel, wizardData);
                        File[] array = tempProject.listFiles();
                        if (array != null && array.length != 0) {
                            File from = ContainerUtil.getFirstItem(ContainerUtil.newArrayList(array));
                            assert from != null;
                            FileUtil.copyDir(from, new File(baseDir.getPath()));
                            deleteTemp(tempProject);
                            //使用vue init创建app之后可能需要对创建好的文件进行修改
                            wizardData.myTemplate.generateProject(wizardData,module,baseDir);
                        }
                    }else{
                        UsageTrigger.trigger(out.getStderr());
                        showErrorMessage(out.getStderr());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        },"Create Project",false,module.getProject());

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                final Project project = module.getProject();
                baseDir.refresh(true, true, new Runnable() {
                    @Override
                    public void run() {
                        VirtualFile idea = project.getBaseDir().findChild(".idea");
                        VueProjectSettingsComponent.excludeFolderOrFile(project,idea);
                        install(baseDir,module,wizardData);
                    }
                });
            }
        });

    }
    private static void showErrorMessage(@NotNull String message) {
        String fullMessage = "Error creating vue-loader App. " + message;
        String title = "Create Vue Project";
        Notifications.Bus.notify(
                new Notification("Vue Generator", title, fullMessage, NotificationType.ERROR)
        );
    }
    private static void install(final VirtualFile cwd,final Module module,final VueProjectWizardData data) {
        ProgressManager.getInstance().run(new Task.Backgroundable(module.getProject(), "Install Dependencies",false) {
            @Override
            public void run(@NotNull final ProgressIndicator progressIndicator) {
                VirtualFile baseDir = module.getProject().getBaseDir();
                GeneralCommandLine cmd = NodeRunner.createCommandLine(baseDir.getPath(), data.sdk.nodePath, "/usr/local/bin/npm");
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
                            progressIndicator.setText(text);
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
