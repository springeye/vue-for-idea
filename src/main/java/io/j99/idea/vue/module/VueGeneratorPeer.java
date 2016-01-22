package io.j99.idea.vue.module;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import io.j99.idea.vue.cli.VueRunner;
import io.j99.idea.vue.cli.VueSettings;
import io.j99.idea.vue.cli.nodejs.NodeRunner;
import io.j99.idea.vue.sdk.VueSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataListener;
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
                            myVersionLabel.setText(output.getStdout().trim());
                            vuePathTextWithBrowse.setEnabled(true);
                        } else {
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
                            myErrorLabel.setVisible(false);
                            myVueVersionLable.setText(output.getStdout().trim());
                            loadVueTemplateList();
                        }else{
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
        String vuePath = vuePathTextWithBrowse.getText().trim();
        String nodePath = nodePathTextWithBrowse.getText().trim();
        try {
            List<String> list = VueRunner.listTemplate(VueSettings.build(FileUtil.getTempDirectory(), nodePath, vuePath));
            myLoadingTemplatesPanel.setVisible(false);
            myLoadedTemplatesPanel.setVisible(true);
            DefaultListModel<String> model = new DefaultListModel<>();
            for(int i=1;i<list.size();i++){
                String element = list.get(i).split(" - ")[0].replaceFirst(" ★ ","");
                System.out.println(element);
                model.addElement(element);
            }

            myTemplatesList.setModel(model);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myMainPanel;
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {

    }

    @NotNull
    @Override
    public VueProjectWizardData getSettings() {
        return null;
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        return null;
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return false;
    }

    @Override
    public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener settingsStateListener) {

    }
    public boolean validateInIntelliJ() {
        return true;
    }
}
