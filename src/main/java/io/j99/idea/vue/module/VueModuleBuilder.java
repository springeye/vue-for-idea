package io.j99.idea.vue.module;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import io.j99.idea.vue.VueIcons;
import io.j99.idea.vue.component.VueProjectSettingsComponent;
import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

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

    private void setupProject(ModifiableRootModel modifiableRootModel, VirtualFile baseDir, VueProjectWizardData wizardData) {
        final String templateName = wizardData.myTemplate.getName();
        UsageTrigger.trigger("VueProjectWizard."+templateName);
        setNodeAndVue(modifiableRootModel,wizardData);
        try {
            wizardData.myTemplate.generateProject(wizardData.sdk,modifiableRootModel.getModule(),baseDir);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private void setNodeAndVue(ModifiableRootModel modifiableRootModel, VueProjectWizardData wizardData) {
            saveSettings(modifiableRootModel.getProject(),wizardData.sdk);
    }

    protected SettingStorage getSettings(Project project) {
        return SettingStorage.getInstance(project);
    }
    protected void saveSettings(Project project, VueProjectWizardData.Sdk sdk) {
        SettingStorage settingStorage = getSettings(project);
        settingStorage.vueExePath = sdk.vuePath;
        settingStorage.nodeInterpreter = sdk.nodePath;
        VueProjectSettingsComponent component = project.getComponent(VueProjectSettingsComponent.class);
        if(component!=null)component.validateSettings();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    protected VueProjectWizardData.Sdk loadSettings(Project project) {
        if(project==null)return null;
        SettingStorage settingStorage = getSettings(project);
        return new VueProjectWizardData.Sdk(settingStorage.nodeInterpreter,settingStorage.vueExePath);
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
        VueModuleWizardStep step = new VueModuleWizardStep(context,loadSettings(context.getProject()));
        Disposer.register(parentDisposable,step);
        return step;
    }
    @Override
    public ModuleType getModuleType() {
        return WebModuleType.getInstance();
    }

}
