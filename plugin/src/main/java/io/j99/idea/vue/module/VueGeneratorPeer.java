package io.j99.idea.vue.module;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import io.j99.idea.vue.VueBundle;
import io.j99.idea.vue.VueIcons;
import io.j99.idea.vue.cli.CmdRunner;
import io.j99.idea.vue.cli.VueFinder;
import io.j99.idea.vue.component.VueProjectSettingsComponent;
import io.j99.idea.vue.network.Network;
import io.j99.idea.vue.network.model.TemplateModel;
import io.j99.idea.vue.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VueGeneratorPeer implements WebProjectGenerator.GeneratorPeer<VueProjectWizardData> {

    private final VueProjectWizardData.Sdk sdk;
    private JPanel myMainPanel;
    private TextFieldWithHistoryWithBrowseButton nodeInterpreterField;
    private JBLabel myVersionLabel;

    private JPanel myTemplatesPanel;
    private JPanel myLoadingTemplatesPanel;
    private JPanel myLoadedTemplatesPanel;
    private JBList myTemplatesList;

    private JBLabel myErrorLabel; // shown in IntelliJ IDEA only
    private JBLabel myVueVersionLable;
    private TextFieldWithHistoryWithBrowseButton vueBinField;
    private java.util.Timer timer1;
    private java.util.Timer timer2;
    private boolean valitedateNode = false;
    private boolean valitedateVue = false;

    public interface RunTaskCallback {
        void onEnd(ProcessOutput output);

        void onFaild(Exception e);
    }

    private class RunTask extends TimerTask {
        public RunTask(GeneralCommandLine cmd, RunTaskCallback callback) {
            this.cmd = cmd;
            this.callback = callback;
        }

        private final GeneralCommandLine cmd;
        private final RunTaskCallback callback;

        @Override
        public void run() {
            try {
                ProcessOutput out = CmdRunner.execute(cmd, CmdRunner.TIME_OUT);
                callback.onEnd(out);
            } catch (ExecutionException e) {
                e.printStackTrace();
                callback.onFaild(e);
            }
        }
    }

    private static TextFieldWithHistory configWithDefaults(TextFieldWithHistoryWithBrowseButton field) {
        TextFieldWithHistory textFieldWithHistory = field.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);
        return textFieldWithHistory;
    }

    private void configVueBinField() {
        configWithDefaults(vueBinField);
        SwingHelper.addHistoryOnExpansion(vueBinField.getChildComponent(), () -> {
            List<File> newFiles = VueFinder.searchForBin(new File("."));
            return FileUtils.toAbsolutePath(newFiles);
        });
        SwingHelper.installFileCompletionAndBrowseDialog(null, vueBinField, "Select Vue Cli", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configNodeField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(nodeInterpreterField);
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, () -> {
            List<File> newFiles = NodeDetectionUtil.listAllPossibleNodeInterpreters();
            return FileUtils.toAbsolutePath(newFiles);
        });
        SwingHelper.installFileCompletionAndBrowseDialog(null, nodeInterpreterField, "Select Node Interpreter", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void update() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        validate();
    }

    private void updateLaterInEDT() {
        UIUtil.invokeLaterIfNeeded(this::update);
    }

    public VueGeneratorPeer(VueProjectWizardData.Sdk sdk) {
        this.sdk = sdk;
        timer1 = new java.util.Timer(true);
        timer2 = new java.util.Timer(true);
        vueBinField.setEnabled(false);
        myLoadingTemplatesPanel.setVisible(false);
        configVueBinField();
        configNodeField();
        DocumentAdapter docAdpNode = new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateLaterInEDT();
                timer1.cancel();
                timer1 = new Timer();
                String path = nodeInterpreterField.getText().trim();
                if (new File(path).isDirectory() || !new File(path).exists()) return;
                GeneralCommandLine cmd = new GeneralCommandLine();
                cmd.setExePath(path);
                cmd.addParameter("--version");
                RunTask task = new RunTask(cmd, new RunTaskCallback() {
                    @Override
                    public void onEnd(ProcessOutput output) {
                        if (output.getExitCode() == 0) {
                            valitedateNode = true;
                            SwingUtilities.invokeLater(() -> {
                                myVersionLabel.setText(output.getStdout().trim());
                                vueBinField.setEnabled(true);
                            });

                        } else {
                            valitedateNode = false;
                            SwingUtilities.invokeLater(() -> vueBinField.setEnabled(false));
                            UsageTrigger.trigger(output.getStderr());
                        }
                    }

                    @Override
                    public void onFaild(Exception e) {

                    }
                });
                timer1.schedule(task, 1000);
            }
        };
        DocumentAdapter docAdpVue = new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateLaterInEDT();
                timer2.cancel();
                timer2 = new Timer();
                String path = vueBinField.getText().trim();
                String nodePath = nodeInterpreterField.getText().trim();
                if (new File(path).isDirectory() || !new File(path).exists()) {
                    myErrorLabel.setVisible(true);
                    return;
                }
                GeneralCommandLine cmd = CmdRunner.createCommandLine(FileUtil.getTempDirectory(), nodePath, path);
                cmd.addParameter("-V");
                RunTask task = new RunTask(cmd, new RunTaskCallback() {
                    @Override
                    public void onEnd(ProcessOutput output) {
                        if (output.getExitCode() == 0) {
                            valitedateVue = true;
                            SwingUtilities.invokeLater(() -> {
                                myErrorLabel.setVisible(false);
                                myVueVersionLable.setText(output.getStdout().trim());
                            });
                            loadVueTemplateList();
                        } else {
                            valitedateVue = false;
                            UsageTrigger.trigger(output.getStderr());
                        }
                    }

                    @Override
                    public void onFaild(Exception e) {

                    }
                });
                timer2.schedule(task, 1000);
            }
        };

        vueBinField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdpVue);
        nodeInterpreterField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdpNode);

        if (sdk != null) {
            if (StringUtil.isNotEmpty(sdk.nodePath)) {
                nodeInterpreterField.setText(sdk.nodePath);
            }
            if (StringUtil.isNotEmpty(sdk.vuePath)) {
                vueBinField.setText(sdk.vuePath);
            }
        }
    }

    private void loadVueTemplateList() {
        SwingUtilities.invokeLater(() -> {
            myLoadingTemplatesPanel.setVisible(true);
            myLoadedTemplatesPanel.setVisible(false);
        });
        AsyncProcessIcon asyncProcessIcon = new AsyncProcessIcon("Vue Template loading");
        myLoadingTemplatesPanel.add(asyncProcessIcon, new GridConstraints());
        asyncProcessIcon.resume();
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<TemplateModel> templates = new Network().listTemplate();
                    asyncProcessIcon.suspend();
                    Disposer.dispose(asyncProcessIcon);
                    SwingUtilities.invokeLater(() -> {
                        myLoadingTemplatesPanel.setVisible(false);
                        myLoadedTemplatesPanel.setVisible(true);
                    });
                    if (templates.size() == 0) {
                        VueProjectSettingsComponent.showNotification("Can't get Vue Templates,Please check your network!", NotificationType.INFORMATION);
                    } else {
                        DefaultListModel<TemplateModel> model = new DefaultListModel<>();
                        for (TemplateModel template : templates) {
                            model.addElement(template);
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                myTemplatesList.setModel(model);
                                myTemplatesList.setCellRenderer(new DefaultListCellRenderer() {
                                    @Override
                                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                        final JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                        final TemplateModel template = (TemplateModel) value;
                                        component.setText(template.getDisplayName());
                                        if (template.getName().startsWith("browserify")) {
                                            component.setIcon(VueIcons.BROWSERIFY_ICON);
                                        } else if (template.getName().startsWith("webpack")) {
                                            component.setIcon(VueIcons.WEBPACK_ICON);
                                        } else {
                                            component.setIcon(VueIcons.VUE_ICON);
                                        }

                                        return component;
                                    }
                                });
                            }
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myMainPanel;
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addExpertField(VueBundle.message("vue.sdk.path"), vueBinField);
        settingsStep.addExpertField(VueBundle.message("node.path"), nodeInterpreterField);
        settingsStep.addSettingsComponent(myTemplatesPanel);
    }

    @NotNull
    @Override
    public VueProjectWizardData getSettings() {
        String vuePath = vueBinField.getText().trim();
        String nodePath = nodeInterpreterField.getText().trim();
        TemplateModel template = (TemplateModel) myTemplatesList.getSelectedValue();
        return new VueProjectWizardData(new VueProjectWizardData.Sdk(nodePath, vuePath), new WebpackTemplate(template.getName(), template.getDescription()));
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        if (!valitedateNode) {
            return new ValidationInfo("Node path error", nodeInterpreterField);
        }
        if (!valitedateVue) {
            return new ValidationInfo("Vue path error", vueBinField);
        }
        return null;
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return false;
    }

    @Override
    public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener stateListener) {
        nodeInterpreterField.getChildComponent().getTextEditor().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(final DocumentEvent e) {
                stateListener.stateChanged(validate() == null);
            }
        });
        vueBinField.getChildComponent().getTextEditor().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(final DocumentEvent e) {
                stateListener.stateChanged(validate() == null);
            }
        });
        myTemplatesList.addListSelectionListener(e -> stateListener.stateChanged(validate() == null));
    }

    public boolean validateInIntelliJ() {
        final ValidationInfo info = validate();

        if (info == null) {
            myErrorLabel.setVisible(false);
            return true;
        } else {
            myErrorLabel.setVisible(true);
            myErrorLabel.setText(XmlStringUtil.wrapInHtml("<font color='#" + ColorUtil.toHex(JBColor.RED) + "'><left>" + info.message + "</left></font>"));
        }
        return false;
    }

}
