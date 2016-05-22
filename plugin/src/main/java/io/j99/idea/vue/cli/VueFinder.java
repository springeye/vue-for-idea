package io.j99.idea.vue.cli;

import io.j99.idea.vue.cli.nodejs.NodeFinder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public final class VueFinder {
    public static final String RT_BASE_NAME = NodeFinder.getBinName("vue");

    private VueFinder() {
    }

    @NotNull
    public static List<File> searchForRTBin(File projectRoot) {
        return NodeFinder.searchAllScopesForBin(projectRoot, RT_BASE_NAME);
    }
}