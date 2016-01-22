package io.j99.idea.vue.cli;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import io.j99.idea.vue.component.VueProjectSettingsComponent;
import io.j99.idea.vue.cli.build.VerifyMessage;
import io.j99.idea.vue.cli.nodejs.NodeRunner;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
/**
 * Created by apple on 16/1/22.
 */
public final class VueRunner {
    private VueRunner() {
    }

    public static final String G = "-g";

    private static final Logger LOG = Logger.getInstance(VueRunner.class);

    private static final int TIME_OUT = (int) TimeUnit.SECONDS.toMillis(120L);
    private static void handleError(String msg, Exception e) {
        LOG.warn(msg, e);
        VueProjectSettingsComponent.showNotification(msg, NotificationType.WARNING);
        e.printStackTrace();
    }

    @NotNull
    private static ProcessOutput version(@NotNull VueSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = VueCliBuilder.version(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }
    public static ProcessOutput template(@NotNull VueSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = VueCliBuilder.list(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }
    public static List<String> listTemplate(@NotNull VueSettings settings) throws ExecutionException {
        ProcessOutput out = template(settings);
        ArrayList<String> list=new ArrayList<>();
        if (out.getExitCode() == 0) {
            list.addAll(out.getStdoutLines());
        }else{
            UsageTrigger.trigger(out.getStderr());
        }
        return list;
    }
    @NotNull
    public static String runVersion(@NotNull VueSettings settings) throws ExecutionException {
        if (!new File(settings.vueExePath).exists()) {
            handleError("Calling version with invalid vue exe " + settings.vueExePath,new RuntimeException());
            return "";
        }
        ProcessOutput out = version(settings);
        if (out.getExitCode() == 0) {
            return out.getStdout().trim();
        }
        return "";
    }

    private static List<VerifyMessage> parse(String json) {
        GsonBuilder builder = new GsonBuilder();
        Gson g = builder.setPrettyPrinting().create();
        Type listType = new TypeToken<ArrayList<VerifyMessage>>() {}.getType();
        return g.fromJson(json, listType);
    }
}
