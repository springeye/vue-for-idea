package io.j99.idea.vue.component;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.j99.idea.vue.VueBundle;
import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.NotNull;

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

    public VueProjectSettingsComponent() {
        this.project = project;
        settingStorage = SettingStorage.getInstance();
    }

    @Override
    public void projectOpened() {
        isSettingsValid();
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
