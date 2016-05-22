package io.j99.idea.vue.cli;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/5/15.
 */
public class NpmUtils {
    public static void packageInstall(ProgressIndicator progressIndicator, @NotNull String cwd, String node, String exe){
        GeneralCommandLine cmd = CmdRunner.createCommandLine(cwd, node, exe);
        cmd.addParameter("i");
        try {
            ProcessOutput out = CmdRunner.execute(cmd, new CmdRunner.ProcessListener() {
                @Override
                public void onError(OSProcessHandler processHandler, String text) {
                }

                @Override
                public void onOutput(OSProcessHandler processHandler, String text) {
                    progressIndicator.setText(text);
                }

                @Override
                public void onCommand(OSProcessHandler processHandler, String text) {

                }
            }, CmdRunner.TIME_OUT * 10);
            if (out.getExitCode() == 0) {
                System.out.println(out.getStdout());
            } else {
                UsageTrigger.trigger(out.getStderr());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
