package io.j99.idea.vue.component;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import io.j99.idea.vue.VueBundle;
import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.openapi.roots.ModuleRootModificationUtil.updateExcludedFolders;

public class VueProjectSettingsComponent implements ProjectComponent {
    public static final String FIX_CONFIG_HREF = "\n<a href=\"#\">Fix Configuration</a>";
    protected Project project;
    public SettingStorage settingStorage;
    protected boolean settingValidStatus;
    protected String settingValidVersion;
    protected String settingVersionLastShowNotification;

    private static final Logger LOG = Logger.getInstance(VueBundle.LOG_ID);

    public String rtExecutable;
    public String nodeInterpreter;
    public boolean treatAsWarnings;

    public static final String PLUGIN_NAME = "vue-for-idea";

    public VueProjectSettingsComponent(Project project) {
        this.project = project;
        settingStorage = SettingStorage.getInstance();
    }

    @Override
    public void projectOpened() {
        isSettingsValid();
        StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
            public void run() {
                    excludePlatformFolders();
            }
        });
    }

    private void excludePlatformFolders() {
        VirtualFile idea = project.getBaseDir().findChild(".idea");
        excludeFolderOrFile(project, idea);
    }

    public static void excludeFolderOrFile(Project project, VirtualFile excludeFile) {
        Module module = ModuleUtilCore.findModuleForFile(excludeFile, project);
        if (module == null) {
            return;
        }
        VirtualFile contentRoot = getContentRoot(module, excludeFile);
        if (contentRoot == null) return;

        Collection<String> oldExcludedFolders = getOldExcludedFolders(module, excludeFile);

        if (oldExcludedFolders.size() == 1 && oldExcludedFolders.contains(excludeFile.getUrl())) return;
        updateExcludedFolders(module, contentRoot, oldExcludedFolders, ContainerUtil.newHashSet(excludeFile.getUrl()));
    }
    private static Collection<String> getOldExcludedFolders(Module module, final VirtualFile root) {
        return ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(), new Condition<String>() {
            public boolean value(final String url) {
                return url.startsWith(root.getUrl());
            }
        });
    }

    private static VirtualFile getContentRoot(Module module, VirtualFile root) {
        return root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
    }
    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
            isSettingsValid();
    }

    @Override
    public void disposeComponent() {

    }

    public static VueProjectSettingsComponent getInstace(Project project) {
        return project.getComponent(VueProjectSettingsComponent.class);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "VueProjectSettingsComponent";
    }

    public boolean isSettingsValid() {
        if (!settingStorage.getVersion().equals(settingValidVersion)) {
            validateSettings();
            settingValidVersion = settingStorage.getVersion();
        }
        return settingValidStatus;
    }

    public boolean validateSettings() {
        boolean status = validateField("Node Interpreter", settingStorage.nodeInterpreter, true, false, true);
        if (!status) {
            return false;
        }
//        status = validateField("Rules", settingStorage.rulesPath, false, true, false);
//        if (!status) {
//            return false;
//        }
        status = validateField("vue-cli bin", settingStorage.vueExePath, false, false, true);
        if (!status) {
            return false;
        }
        status = validateField("Builtin rules", settingStorage.builtinRulesPath, false, true, false);
        if (!status) {
            return false;
        }

        rtExecutable = settingStorage.vueExePath;
        nodeInterpreter = settingStorage.nodeInterpreter;
        treatAsWarnings = settingStorage.treatAllIssuesAsWarnings;

        settingValidStatus = true;
        return true;
    }

    private boolean validateField(String fieldName, String value, boolean shouldBeAbsolute, boolean allowEmpty, boolean isFile) {
        return true;
    }

    public static void showNotification(String content, NotificationType type) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type);
        Notifications.Bus.notify(errorNotification);
    }
}
