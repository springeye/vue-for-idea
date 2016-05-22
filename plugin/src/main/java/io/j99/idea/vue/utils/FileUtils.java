package io.j99.idea.vue.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import io.j99.idea.vue.VueBundle;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import com.scss.ScssLintBundle;

public final class FileUtils {
    private FileUtils() {
    }

    private static final Logger LOG = Logger.getInstance(VueBundle.LOG_ID);

    // WildcardFileNameMatcher w = new WildcardFileNameMatcher("**/.jscsrc");

    public static String makeRelative(VirtualFile root, VirtualFile absolutePath) {
        //FileUtil.getRelativePath(path, file.getPath().replace('/', File.separatorChar), File.separatorChar)
        return VfsUtil.getRelativePath(absolutePath, root, File.separatorChar);
    }

    public static String makeRelative(Project project, VirtualFile absolutePath) {
        //FileUtil.getRelativePath(path, file.getPath().replace('/', File.separatorChar), File.separatorChar)
        return makeRelative(project.getBaseDir(), absolutePath);
    }

    public static String relativePath(String root, String path) {
        return FileUtil.getRelativePath(new File(root), new File(path));
    }

    public static String relativePath(Project project, VirtualFile absolutePath) {
        return FileUtil.getRelativePath(new File(project.getBasePath()), new File(absolutePath.getPath()));
    }

    public static String relativePath(PsiFile file) {
        return FileUtil.getRelativePath(new File(file.getProject().getBasePath()), new File(file.getVirtualFile().getPath()));
    }

    public static String getExtensionWithDot(VirtualFile file){
        String ext = StringUtil.notNullize(file.getExtension());
        if (!ext.startsWith(".")) {
            ext = '.' + ext;
        }
        return ext;
    }

    public static String makeRelative(File project, File absolutePath) {
        return FileUtil.getRelativePath(project, absolutePath);
    }

    public static boolean hasExtension(String file, String extension) {
        return file.endsWith(extension);
    }

    public static String removeExtension(String file) {
        int i = file.lastIndexOf('.');
        return file.substring(0, i);
    }

    //file.substring(0, file.length() - ".js".length())

    /**
     * resolve a relative or absolute path
     * @param project parent path
     * @param path path to file / folder
     * @return
     */
    public static String resolvePath(Project project, String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists()) {
                return null;
            }
            return path;
        } else {
            if (project == null) {
                return null;
            }
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists()) {
                return null;
            }
            return child.getPath();
        }
    }

    @NotNull
    public static List<String> displayDirectoryContents(@NotNull File projectRoot, @NotNull File dir, @NotNull FilenameFilter filter) {
        List<String> ret = listFiles(projectRoot, dir, filter);
        File[] files = dir.listFiles();
        for (final File file : files) {
            if (file.isDirectory()) {
                ret.addAll(displayDirectoryContents(projectRoot, file, filter));
//            } else {
//                listFiles(file, filter, allFiles);
            }
        }
        return ret;
    }

    @NotNull
    public static List<String> recursiveVisitor(@NotNull File dir, @NotNull FilenameFilter filter) {
//        String[] ret = dir.list(filter);
        List<String> retList = new ArrayList<String>();
//        Collections.addAll(retList, ret);
        File[] files = dir.listFiles();
        for (final File file : files) {
            if (file.isDirectory()) {
                retList.addAll(recursiveVisitor(file, filter));
            } else {
                if (filter.accept(file.getParentFile(), file.getName())){
                    retList.add(file.getAbsolutePath());
                }
            }
        }
        return retList;
    }

    public static List<String> toAbsolutePath(List<File> newFiles) {
        return ContainerUtil.map(newFiles, new Function<File, String>() {
            public String fun(File file) {
                return file.getAbsolutePath();
            }
        });
    }

    @NotNull
    public static List<String> listFiles(@NotNull final File projectRoot, @NotNull final File dir, @NotNull FilenameFilter filter) {
        String[] curFiles = dir.list(filter);
        //        allFiles.addAll(ret); //Arrays.asList(curFiles));
        return ContainerUtil.map(curFiles, new Function<String, String>() {
            public String fun(String curFile) {
                // TODO replace with makeRelative
//                return makeRelative();
                return new File(dir, curFile).getAbsolutePath().substring(projectRoot.getAbsolutePath().length() + 1);
            }
        });
    }

    @NotNull
    private static List<File> findExeFilesInPath(@Nullable String pathEnvVarValue,
                                                 @NotNull String fileBaseName,
                                                 boolean stopAfterFirstMatch,
                                                 boolean logDetails) {
        if (logDetails) {
            LOG.info("Finding files in PATH (base name=" + fileBaseName + ", PATH=" + StringUtil.notNullize(pathEnvVarValue) + ").");
        }
        if (pathEnvVarValue == null) {
            return Collections.emptyList();
        }
        List<File> result = new SmartList<File>();
        List<String> paths = StringUtil.split(pathEnvVarValue, File.pathSeparator, true, true);
        for (String path : paths) {
            File dir = new File(path);
            if (logDetails) {
                File file = new File(dir, fileBaseName);
                LOG.info("path:" + path + ", path.isAbsolute:" + dir.isAbsolute() + ", path.isDirectory:" + dir.isDirectory()
                        + ", file.isFile:" + file.isFile() + ", file.canExecute:" + file.canExecute());
            }
            if (dir.isAbsolute() && dir.isDirectory()) {
                File file = new File(dir, fileBaseName);
                if (file.isFile() && file.canExecute()) {
                    result.add(file);
                    if (stopAfterFirstMatch) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    /**
     *
     * @param project containing project
     * @param path path to path to file / folder
     * @param allowEmpty allow empty path
     * @param isFile should be file or folder
     * @return validation status
     */
    public static ValidationStatus validateProjectPath(Project project, String path, boolean allowEmpty, boolean isFile) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty ? ValidationStatus.VALID : ValidationStatus.IS_EMPTY;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists()) {
                return ValidationStatus.DOES_NOT_EXIST;
            }
            if (isFile) {
                if (!filePath.isFile()) {
                    return ValidationStatus.NOT_A_FILE;
                }
            } else {
                if (!filePath.isDirectory()) {
                    return ValidationStatus.NOT_A_DIRECTORY;
                }
            }
        } else {
            if (project == null) {
                return ValidationStatus.DOES_NOT_EXIST;
            }
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists()) {
                return ValidationStatus.DOES_NOT_EXIST;
            }
            if (isFile) {
                if (child.isDirectory()) {
                    return ValidationStatus.NOT_A_FILE;
                }
            } else {
                if (!child.isDirectory()) {
                    return ValidationStatus.NOT_A_DIRECTORY;
                }
            }
        }
        return ValidationStatus.VALID;
    }

    //    public static class ValidationError {
    //        public boolean a;
    //    }
    //
    public enum ValidationStatus {
        VALID, IS_EMPTY, DOES_NOT_EXIST, NOT_A_DIRECTORY, NOT_A_FILE
    }

    public static boolean fileExists(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.isFile() && file.exists();
    }

    public static List<String> getAllFilesInDirectory(VirtualFile directory, String target, String replacement) {
        List<String> files = new ArrayList<String>();
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child instanceof VirtualDirectoryImpl) {
                files.addAll(getAllFilesInDirectory(child, target, replacement));
            } else if (child instanceof VirtualFileImpl) {
                files.add(child.getPath().replace(target, replacement));
            }
        }
        return files;
    }

    public static VirtualFile findFileByPath(VirtualFile path, String valuePath) {
        VirtualFile file = path.findFileByRelativePath(valuePath);
        if (null == file || file.isDirectory()) {
            file = path.findFileByRelativePath(valuePath + ".js");
        }
        return file;
    }


    public static String join(String file, String ext) {
        return file + '/' + ext;
    }

    public static String removeExt(String file, String ext) {
        if (file.endsWith(ext)) {
            return file.replace(ext, "");
        }
        return file;
    }

    public static String relativePath(VirtualFile root, VirtualFile file) {
        // get project relative path
        return file.getPath().substring(root.getPath().length() + 1);
    }

    public static String getNormalizedPath(int doubleDotCount, String[] pathsOfPath) {
        StringBuilder newValuePath = new StringBuilder();
        for (int i = 0; i < pathsOfPath.length - doubleDotCount; i++) {
            if (0 != i) {
                newValuePath.append('/');
            }
            newValuePath.append(pathsOfPath[i]);
        }
        return newValuePath.toString();
    }

    public static int getDoubleDotCount(String valuePath) {
        int doubleDotCount = (valuePath.length() - valuePath.replaceAll("\\.\\.", "").length()) / 2;
        boolean doubleDotCountTrues = false;

        while (!doubleDotCountTrues && 0 != doubleDotCount) {
            if (valuePath.startsWith(StringUtil.repeat("../", doubleDotCount))) {
                doubleDotCountTrues = true;
            } else if (valuePath.startsWith(StringUtil.repeat("../", doubleDotCount - 1) + "..")) {
                doubleDotCountTrues = true;
            } else {
                doubleDotCount--;
            }
        }
        return doubleDotCount;
    }
}
