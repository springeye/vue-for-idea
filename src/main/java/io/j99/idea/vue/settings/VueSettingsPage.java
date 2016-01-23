package io.j99.idea.vue.settings;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.execution.ExecutionException;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.NotNullProducer;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.ui.SwingHelper;
import io.j99.idea.vue.component.VueProjectSettingsComponent;
import io.j99.idea.vue.cli.VueFinder;
import io.j99.idea.vue.cli.VueRunner;
import io.j99.idea.vue.cli.VueSettings;
import io.j99.idea.vue.ui.PackagesNotificationPanel;
import io.j99.idea.vue.utils.FileUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VueSettingsPage implements Configurable {
    public static final String FIX_IT = "Fix it";
    public static final String HOW_TO_USE_RT = "How to Use vue-cli";
    public static final String HOW_TO_USE_LINK = "https://github.com/vuejs/vue-cli";
    protected Project project;

    private JPanel panel;
    private JPanel errorPanel;
    private TextFieldWithHistoryWithBrowseButton rtBinField;
    private TextFieldWithHistoryWithBrowseButton nodeInterpreterField;
    private HyperlinkLabel usageLink;
    private JLabel pathToRTBinLabel;
    private JLabel nodeInterpreterLabel;
    private JLabel versionLabel;
    private final PackagesNotificationPanel packagesNotificationPanel;

    public VueSettingsPage(@NotNull final Project project) {
        this.project = project;
        configRTBinField();
        configNodeField();


        this.packagesNotificationPanel = new PackagesNotificationPanel(project);
        errorPanel.add(this.packagesNotificationPanel.getComponent(), BorderLayout.CENTER);

        DocumentAdapter docAdp = new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateLaterInEDT();
            }
        };
        rtBinField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        nodeInterpreterField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
    }

    private File getProjectPath() {
        return new File(project.getBaseDir().getPath());
    }

    private void updateLaterInEDT() {
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                VueSettingsPage.this.update();
            }
        });
    }

    private void update() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        validate();
    }

    private void validateField(List<ValidationInfo> errors, TextFieldWithHistoryWithBrowseButton field, boolean allowEmpty, String message) {
        if (!ValidationUtils.validatePath(project, field.getChildComponent().getText(), allowEmpty)) {
            ValidationInfo error = new ValidationInfo(field.getChildComponent().getTextEditor(), message, FIX_IT);
            errors.add(error);
        }
    }

    private void validate() {
        List<ValidationInfo> errors = new ArrayList<ValidationInfo>();
        validateField(errors, rtBinField, false, "Path to vue-cli is invalid {{LINK}}");
        validateField(errors, nodeInterpreterField, false, "Path to node interpreter is invalid {{LINK}}");
        if (errors.isEmpty()) {
            getVersion();
        }
        packagesNotificationPanel.processErrors(errors);
    }

    private VueSettings settings;

    private void getVersion() {
        if (settings != null &&
                areEqual(nodeInterpreterField, settings.node) &&
                areEqual(rtBinField, settings.vueExePath) &&
                settings.cwd.equals(project.getBasePath())
                ) {
            return;
        }
        settings = new VueSettings();
        settings.node = nodeInterpreterField.getChildComponent().getText();
        settings.vueExePath = rtBinField.getChildComponent().getText();
        settings.cwd = project.getBasePath();
        try {
            String version = VueRunner.runVersion(settings);
            versionLabel.setText("vue-cli version: "+version.trim());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static TextFieldWithHistory configWithDefaults(TextFieldWithHistoryWithBrowseButton field) {
        TextFieldWithHistory textFieldWithHistory = field.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);
        return textFieldWithHistory;
    }

    private void configRTBinField() {
        configWithDefaults(rtBinField);
        SwingHelper.addHistoryOnExpansion(rtBinField.getChildComponent(), new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = VueFinder.searchForRTBin(getProjectPath());
                return FileUtils.toAbsolutePath(newFiles);
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, rtBinField, "Select vue cli", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configNodeField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(nodeInterpreterField);
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = NodeDetectionUtil.listAllPossibleNodeInterpreters();
                return FileUtils.toAbsolutePath(newFiles);
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, nodeInterpreterField, "Select Node interpreter", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "vue-cli";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        loadSettings();
        return panel;
    }

    private static boolean areEqual(TextFieldWithHistoryWithBrowseButton field, String value) {
        return field.getChildComponent().getText().equals(value);
    }

    private static boolean areEqual(JComboBox<?> field, String value) {
        Object item = field.getSelectedItem();
        return item == null && value == null || item != null && item.equals(value);
    }

    private static boolean areEqual(JCheckBox field, boolean value) {
        return field.isSelected() == value;
    }

    @Override
    public boolean isModified() {
        SettingStorage s = getSettings();
        return !areEqual(rtBinField, s.vueExePath) ||
                !areEqual(nodeInterpreterField, s.nodeInterpreter);
    }

    @Override
    public void apply() throws ConfigurationException {
        saveSettings();
        PsiManager.getInstance(project).dropResolveCaches();
        ProjectView.getInstance(project).refresh();
    }

    protected void saveSettings() {
        SettingStorage settingStorage = getSettings();
        settingStorage.vueExePath = rtBinField.getChildComponent().getText();
        settingStorage.nodeInterpreter = nodeInterpreterField.getChildComponent().getText();
        VueProjectSettingsComponent component = project.getComponent(VueProjectSettingsComponent.class);
        if(component!=null)component.validateSettings();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    protected void loadSettings() {
        SettingStorage settingStorage = getSettings();
        rtBinField.getChildComponent().setText(settingStorage.vueExePath);
        nodeInterpreterField.getChildComponent().setText(settingStorage.nodeInterpreter);
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {
    }

    protected SettingStorage getSettings() {
        return SettingStorage.getInstance();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        usageLink = com.intellij.util.ui.SwingHelper.createWebHyperlink(HOW_TO_USE_RT, HOW_TO_USE_LINK);
    }
}
