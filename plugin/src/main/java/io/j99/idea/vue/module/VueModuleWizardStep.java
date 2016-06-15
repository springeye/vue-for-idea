package io.j99.idea.vue.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import io.j99.idea.vue.VueIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by apple on 16/1/22.
 */
public class VueModuleWizardStep extends ModuleWizardStep implements Disposable {
    private final WizardContext myContext;
    private final VueGeneratorPeer myPeer;

    public VueModuleWizardStep(WizardContext myContext, VueProjectWizardData.Sdk sdk) {
        this.myContext = myContext;
        this.myPeer = new VueGeneratorPeer(sdk);

    }

    @Override
    public JComponent getComponent() {
        return myPeer.getComponent();
//        return new JLabel("aaa");
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return VueIcons.VUE_ICON;
    }

    @Override
    public void updateDataModel() {
        final ProjectBuilder projectBuilder = myContext.getProjectBuilder();
        if (projectBuilder instanceof VueModuleBuilder) {
            ((VueModuleBuilder) projectBuilder).setWizardData(myPeer.getSettings());
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return myPeer.validateInIntelliJ();
    }

    @Override
    public void dispose() {

    }
}
