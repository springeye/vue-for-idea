package io.j99.idea.vue.cli;

import io.j99.idea.vue.cli.nodejs.NodeFinder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public final class VueFinder {
    public static final String VUE_BASE_NAME = NodeFinder.getBinName("vue");

    private VueFinder() {
    }

    @NotNull
    public static List<File> searchForBin(File projectRoot) {
        return NodeFinder.searchAllScopesForBin(projectRoot, VUE_BASE_NAME);
    }
}