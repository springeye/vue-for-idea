package io.j99.idea.vue;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.navigation.NavigationItemFileStatus;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import io.j99.idea.vue.psi.VueFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by apple on 16/5/22.
 */
public class VueNode extends ProjectViewNode<VueFile> {
    private final Collection<BasePsiNode<? extends PsiFile>> children;

    protected VueNode(Project project, VueFile value, ViewSettings viewSettings, Collection<BasePsiNode<? extends PsiFile>> children) {
        super(project, value, viewSettings);
        this.children=children;
    }
    @Override
    public Comparable getTypeSortKey() {
        return new PsiFileNode.ExtensionSortKey(VueFileType.INSTANCE.getDefaultExtension());
    }
    @Override
    public boolean contains(@NotNull VirtualFile file) {
        for (final BasePsiNode<? extends PsiFile> child : children) {
            ProjectViewNode treeNode = (ProjectViewNode) child;
            if (treeNode.contains(file)) return true;
        }
        return false;
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode> getChildren() {
        return children;
    }

    @Override
    protected void update(PresentationData presentation) {
        if (getValue() == null || !getValue().isValid()) {
            setValue(null);
        } else {
            presentation.setPresentableText(getValue().getName());
            presentation.setIcon(VueIcons.VUE_ICON);
        }
    }
    @Override
    public FileStatus getFileStatus() {
        for (BasePsiNode<? extends PsiFile> child : children) {
            final PsiFile value = child.getValue();
            if (value == null || !value.isValid()) continue;
            final FileStatus fileStatus = NavigationItemFileStatus.get(child);
            if (!fileStatus.equals(FileStatus.NOT_CHANGED)) {
                return fileStatus;
            }
        }
        return FileStatus.NOT_CHANGED;
    }
    @Override
    public boolean canHaveChildrenMatching(final Condition<PsiFile> condition) {
        for (BasePsiNode<? extends PsiFile> child : children) {
            if (condition.value(child.getValue().getContainingFile())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public VirtualFile getVirtualFile() {
        if (getValue() != null ) {
            return getValue().getVirtualFile();
        }
        return null;
    }
}
