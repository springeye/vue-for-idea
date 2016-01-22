package io.j99.idea.vue.module;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.xml.util.XmlStringUtil;
import io.j99.idea.vue.VueBundle;
import io.j99.idea.vue.cli.VueRunner;
import io.j99.idea.vue.cli.VueSettings;
import io.j99.idea.vue.cli.nodejs.NodeRunner;
import io.j99.idea.vue.sdk.VueSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VueGeneratorPeer implements WebProjectGenerator.GeneratorPeer<VueProjectWizardData>{

    private final VueProjectWizardData.Sdk sdk;
    private JPanel myMainPanel;
    private TextFieldWithBrowseButton nodePathTextWithBrowse;
    private JBLabel myVersionLabel;

    private JPanel myTemplatesPanel;
    private JPanel myLoadingTemplatesPanel;
    private JPanel myLoadedTemplatesPanel;
    private JBList myTemplatesList;

    private JBLabel myErrorLabel; // shown in IntelliJ IDEA only
    private JBLabel myVueVersionLable;
    private TextFieldWithBrowseButton vuePathTextWithBrowse;
    private java.util.Timer timer;
    private boolean valitedateNode=false;
    private boolean valitedateVue=false;
    public interface RunTaskCallback{
        void onEnd(ProcessOutput output);
        void onFaild(Exception e);
    }
    private class RunTask extends TimerTask{
        public RunTask(GeneralCommandLine cmd, RunTaskCallback callback) {
            this.cmd = cmd;
            this.callback = callback;
        }

        private final GeneralCommandLine cmd;
        private final RunTaskCallback callback;
        @Override
        public void run() {
            try {
                ProcessOutput out = NodeRunner.execute(cmd, NodeRunner.TIME_OUT);
                callback.onEnd(out);
            } catch (ExecutionException e) {
                e.printStackTrace();
                callback.onFaild(e);
            }
        }
    }
    public VueGeneratorPeer(VueProjectWizardData.Sdk sdk){
        this.sdk=sdk;
        timer=new java.util.Timer(true);
        vuePathTextWithBrowse.setEnabled(false);
        myLoadingTemplatesPanel.setVisible(false);
        nodePathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {
                timer.cancel();
                timer=new Timer();
                String path = nodePathTextWithBrowse.getText().trim();
                if(new File(path).isDirectory()||!new File(path).exists())return;
                GeneralCommandLine cmd = new GeneralCommandLine();
                cmd.setExePath(path);
                cmd.addParameter("--version");
                RunTask task = new RunTask(cmd, new RunTaskCallback() {
                    @Override
                    public void onEnd(ProcessOutput output) {
                        if (output.getExitCode() == 0) {
                            valitedateNode=true;
                            myVersionLabel.setText(output.getStdout().trim());
                            vuePathTextWithBrowse.setEnabled(true);
                        } else {
                            valitedateNode=false;
                            vuePathTextWithBrowse.setEnabled(false);
                            UsageTrigger.trigger(output.getStderr());
                        }
                    }

                    @Override
                    public void onFaild(Exception e) {

                    }
                });
                timer.schedule(task,1000);

            }
        });
        vuePathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {
                timer.cancel();
                timer=new Timer();
                String path = vuePathTextWithBrowse.getText().trim();
                String nodePath = nodePathTextWithBrowse.getText().trim();
                if(new File(path).isDirectory() || !new File(path).exists()){
                    myErrorLabel.setVisible(true);
                    return;
                }
                GeneralCommandLine cmd = NodeRunner.createCommandLine(FileUtil.getTempDirectory(), nodePath, path);
                cmd.addParameter("-V");
                RunTask task=new RunTask(cmd, new RunTaskCallback() {
                    @Override
                    public void onEnd(ProcessOutput output){
                        if(output.getExitCode()==0){
                            valitedateVue=true;
                            myErrorLabel.setVisible(false);
                            myVueVersionLable.setText(output.getStdout().trim());
                            loadVueTemplateList();
                        }else{
                            valitedateVue=false;
                            UsageTrigger.trigger(output.getStderr());
                        }
                    }

                    @Override
                    public void onFaild(Exception e) {

                    }
                });
                timer.schedule(task,1000);
            }
        });
        if(sdk!=null) {
            nodePathTextWithBrowse.setText(sdk.nodePath);
            vuePathTextWithBrowse.setText(sdk.vuePath);
        }
        VueSdkUtils.initVueSdkControls(null,nodePathTextWithBrowse,vuePathTextWithBrowse);
    }
    private void loadVueTemplateList() {
        myLoadingTemplatesPanel.setVisible(true);
        myLoadedTemplatesPanel.setVisible(false);
        AsyncProcessIcon asyncProcessIcon=new AsyncProcessIcon("Vue Template loading");
        myLoadingTemplatesPanel.add(asyncProcessIcon,new GridConstraints());
        asyncProcessIcon.resume();
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                String vuePath = vuePathTextWithBrowse.getText().trim();
                String nodePath = nodePathTextWithBrowse.getText().trim();
                try {
                    List<String> list = VueRunner.listTemplate(VueSettings.build(FileUtil.getTempDirectory(), nodePath, vuePath));
                    asyncProcessIcon.suspend();
                    Disposer.dispose(asyncProcessIcon);
                    myLoadingTemplatesPanel.setVisible(false);
                    myLoadedTemplatesPanel.setVisible(true);
                    DefaultListModel<VueProjectTemplate> model = new DefaultListModel<>();
                    for(int i=1;i<list.size();i++){

                        String[] split = list.get(i).split(" - ");
                        String name = split[0].replaceFirst("  ★  ","");
                        String desc = split[1];
                        if("browserify".equals(name)){
                            model.addElement(new WebpackTemplate(name,desc));
                        }else if("browserify-simple".equals(name)){
                            model.addElement(new WebpackTemplate(name,desc));
                        }else if("webpack".equals(name)){
                            model.addElement(new WebpackTemplate(name,desc));
                        }else if("webpack-simple".equals(name)){
                            model.addElement(new WebpackTemplate(name,desc));
                        }

                    }

                    myTemplatesList.setModel(model);
                    myTemplatesList.setCellRenderer(new DefaultListCellRenderer(){
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            final JLabel component = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            final VueProjectTemplate template = (VueProjectTemplate)value;
                            final String text = template.getDescription().isEmpty()
                                    ? template.getName()
                                    : template.getName() + " - " + StringUtil.decapitalize(template.getDescription());
                            component.setText(text);
                            return component;
                        }
                    });
                } catch (ExecutionException e) {
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
        settingsStep.addExpertField(VueBundle.message("vue.sdk.path"),vuePathTextWithBrowse);
        settingsStep.addExpertField(VueBundle.message("node.path"),nodePathTextWithBrowse);
        settingsStep.addSettingsComponent(myTemplatesPanel);
    }

    @NotNull
    @Override
    public VueProjectWizardData getSettings() {
        String vuePath = vuePathTextWithBrowse.getText().trim();
        String nodePath = nodePathTextWithBrowse.getText().trim();
        VueProjectTemplate template = (VueProjectTemplate)myTemplatesList.getSelectedValue();
        return new VueProjectWizardData(new VueProjectWizardData.Sdk(nodePath, vuePath), template);
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        if(!valitedateNode){
            return new ValidationInfo("Node path error",nodePathTextWithBrowse);
        }
        if(!valitedateVue){
            return new ValidationInfo("Vue path error",vuePathTextWithBrowse);
        }
        return null;
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return false;
    }

    @Override
    public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener stateListener) {
        nodePathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(final DocumentEvent e) {
                stateListener.stateChanged(validate() == null);
            }
        });
        vuePathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(final DocumentEvent e) {
                stateListener.stateChanged(validate() == null);
            }
        });
        myTemplatesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                stateListener.stateChanged(validate() == null);
            }
        });
    }
    public boolean validateInIntelliJ() {
        final ValidationInfo info = validate();

        if (info == null) {
            myErrorLabel.setVisible(false);
            return true;
        }
        else {
            myErrorLabel.setVisible(true);
            myErrorLabel.setText(XmlStringUtil.wrapInHtml("<font color='#" + ColorUtil.toHex(JBColor.RED) + "'><left>" + info.message + "</left></font>"));
        }
        return false;
    }
}
