package io.j99.idea.vue.sdk;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

/**
 * Created by apple on 16/1/22.
 */
public class VueSdkUtils {
    public static void initVueSdkControls(Project project, TextFieldWithBrowseButton dartSdkPathComponent, TextFieldWithBrowseButton vuePathTextWithBrowse) {
        final TextComponentAccessor<JTextField> nodeComponentAccessor = new TextComponentAccessor<JTextField>() {
            @Override
            public String getText(final JTextField component) {
                String text = component.getText();
                if (StringUtil.isEmpty(text)) {
                    return SystemInfo.isWindows ? "C:\\Program Files\\nodejs" : SystemInfo.isMac ? "/usr/local/Cellar/node/bin/node" : "/usr/local/bin/node";
                }
                return text;
            }

            @Override
            public void setText(final JTextField component, @NotNull String text) {
                if (!text.isEmpty()) {
                    if(new File(text.trim()).isDirectory()) {
                        component.setText(FileUtilRt.toSystemDependentName(text+"/node"));
                    }else{
                        component.setText(FileUtilRt.toSystemDependentName(text));
                    }
                    return;
                }

                component.setText(FileUtilRt.toSystemDependentName(text));
            }
        };
        final TextComponentAccessor<JTextField> vueComponentAccessor = new TextComponentAccessor<JTextField>() {
            @Override
            public String getText(final JTextField component) {
                String text = component.getText();
                if (StringUtil.isEmpty(text)) {
                    return SystemInfo.isWindows ? "C:\\Program Files\\nodejs" : SystemInfo.isMac ? "/usr/local/Cellar/node/bin/vue" : "/usr/local/bin/vue";
                }
                return text;
            }

            @Override
            public void setText(final JTextField component, @NotNull String text) {
                if (!text.isEmpty()) {
                    if(new File(text.trim()).isDirectory()) {
                        component.setText(FileUtilRt.toSystemDependentName(text+"/vue"));
                    }else{
                        component.setText(FileUtilRt.toSystemDependentName(text));
                    }
                    return;
                }

                component.setText(FileUtilRt.toSystemDependentName(text));
            }
        };
        final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseNodeFolderListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>("Select Node path", null, dartSdkPathComponent, project,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        nodeComponentAccessor);
        final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseVueFolderListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>("Select vue path", null, vuePathTextWithBrowse, project,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        vueComponentAccessor);
        dartSdkPathComponent.addBrowseFolderListener(project, browseNodeFolderListener);
        vuePathTextWithBrowse.addBrowseFolderListener(project, browseVueFolderListener);
    }
}
