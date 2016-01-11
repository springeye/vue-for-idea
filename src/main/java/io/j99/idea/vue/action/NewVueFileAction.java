package io.j99.idea.vue.action;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.j99.idea.vue.VueBundle;
import io.j99.idea.vue.VueIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Created by android on 2016/1/12.
 */
public class NewVueFileAction extends NewVueActionBase{
    public NewVueFileAction() {
        super(VueBundle.message("newfile.menu.action.text"),
                VueBundle.message("newfile.menu.action.description"),
                VueIcons.VUE_ICON);
    }

    @Override
    protected String getDialogPrompt() {
        return VueBundle.message("newfile.dialog.prompt");
    }

    @Override
    protected String getDialogTitle() {
        return VueBundle.message("newfile.dialog.title");
    }

    @NotNull
    @Override
    protected PsiElement[] doCreate(String newName, PsiDirectory directory) {
        PsiFile file = createFileFromTemplate(directory, newName, VueTemplatesFactory.NEW_SCRIPT_FILE_NAME);
        PsiElement child = file.getLastChild();
        return child != null ? new PsiElement[]{file, child} : new PsiElement[]{file};
    }

    @Override
    protected String getCommandName() {
        return VueBundle.message("newfile.command.name");
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s) {
        return VueBundle.message("newfile.menu.action.text");
    }
}
