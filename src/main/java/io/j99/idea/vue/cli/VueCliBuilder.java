package io.j99.idea.vue.cli;

import com.intellij.execution.configurations.GeneralCommandLine;
import io.j99.idea.vue.cli.nodejs.NodeRunner;
import org.jetbrains.annotations.NotNull;

final class VueCliBuilder {

    public static final String V = "-V";
    public static final String LIST = "list";
    private VueCliBuilder() {
    }
    @NotNull
    static GeneralCommandLine list(@NotNull VueSettings settings) {
        GeneralCommandLine commandLine = createCommandLine(settings);
        commandLine.addParameter(LIST);
        return commandLine;
    }

    @NotNull
    static GeneralCommandLine version(@NotNull VueSettings settings) {
        GeneralCommandLine commandLine = createCommandLine(settings);
        commandLine.addParameter(V);
        return commandLine;
    }

    @NotNull
    static GeneralCommandLine createCommandLine(@NotNull VueSettings settings) {
        return NodeRunner.createCommandLine(settings.cwd, settings.node, settings.vueExePath);
    }

}
