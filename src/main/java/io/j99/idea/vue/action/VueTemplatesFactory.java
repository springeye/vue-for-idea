package io.j99.idea.vue.action;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import io.j99.idea.vue.VueFileType;
import org.jetbrains.annotations.NonNls;

import java.util.Properties;

/**
 * Created by android on 2016/1/12.
 */
public class VueTemplatesFactory {
    public static final String NEW_SCRIPT_FILE_NAME = "Vue File.vue";
    public static PsiFile createFromTemplate(final PsiDirectory directory, final String name,
                                             String fileName, String templateName,
                                             @NonNls String... parameters) throws IncorrectOperationException {

        final FileTemplate template = FileTemplateManager.getDefaultInstance().getTemplate(templateName);

        Properties properties = new Properties(FileTemplateManager.getDefaultInstance().getDefaultProperties());

        Project project = directory.getProject();
        properties.setProperty("PROJECT_NAME", project.getName());
        properties.setProperty("NAME", fileName);


        String text;

        try {
            text = template.getText(properties);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template for " +
                    FileTemplateManager.getInstance().internalTemplateToSubject(templateName), e);
        }

        final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());

        final PsiFile file = factory.createFileFromText(fileName, VueFileType.INSTANCE, text);

        return (PsiFile) directory.add(file);
    }
}
