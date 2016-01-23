package io.j99.idea.vue.cli.nodejs;

import com.google.common.base.Charsets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class NodeRunner {
    private NodeRunner() {
    }

    private static final Logger LOG = Logger.getInstance(NodeRunner.class);

    public static final int TIME_OUT = (int) TimeUnit.SECONDS.toMillis(120L);

    /**
     * @param cwd working directory
     * @param node node interpreter path
     * @param exe node executable to run
     * @return command line to sdk
     */
    @NotNull
    public static GeneralCommandLine createCommandLine(@NotNull String cwd, String node, String exe) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        if (!new File(cwd).exists()) {
            throw new IllegalArgumentException("cwd: "+cwd +" doesn't exist");
        }
        if (!new File(exe).exists()) {
            throw new IllegalArgumentException("exe: "+exe +" doesn't exist");

        }
        commandLine.setWorkDirectory(cwd);
        if (SystemInfo.isWindows) {
            commandLine.setExePath(exe);
        } else {
            if (!new File(node).exists()) {
                throw new IllegalArgumentException("path doesn't exist");
            }
            commandLine.setExePath(node);
            commandLine.addParameter(exe);
        }
        return commandLine;
    }
    public interface ProcessListener{
        void onError(OSProcessHandler processHandler ,String text);
        void onOutput(OSProcessHandler processHandler ,String text);
        void onCommand(OSProcessHandler processHandler , String text);
    }
    /**
     * @param commandLine command line to sdk
     * @param timeoutInMilliseconds timeout
     * @return process output
     * @throws ExecutionException
     */
    @NotNull
    public static ProcessOutput execute(@NotNull GeneralCommandLine commandLine,ProcessListener listener, int timeoutInMilliseconds) throws ExecutionException {
        LOG.info("Running node command: " + commandLine.getCommandLineString());
        Process process = commandLine.createProcess();
        OSProcessHandler processHandler = new ColoredProcessHandler(process, commandLine.getCommandLineString(), Charsets.UTF_8);
        final ProcessOutput output = new ProcessOutput();
        processHandler.addProcessListener(new ProcessAdapter() {
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType.equals(ProcessOutputTypes.STDERR)) {
                    output.appendStderr(event.getText());
                    if(listener!=null){
                        listener.onError(processHandler,event.getText());
                    }
                } else if (!outputType.equals(ProcessOutputTypes.SYSTEM)) {
                    output.appendStdout(event.getText());
                    if(listener!=null){
                        listener.onOutput(processHandler,event.getText());
                    }
                }else if(outputType.equals(ProcessOutputTypes.SYSTEM)){
                    if(listener!=null){
                        listener.onCommand(processHandler,event.getText());
                    }
                }
            }
        });
        processHandler.startNotify();
        if (processHandler.waitFor(timeoutInMilliseconds)) {
            output.setExitCode(process.exitValue());
        } else {
            processHandler.destroyProcess();
            output.setTimeout();
        }
        if (output.isTimeout()) {
            throw new ExecutionException("Command '" + commandLine.getCommandLineString() + "' is timed out.");
        }
        return output;
    }
    /**
     * @param commandLine command line to sdk
     * @param timeoutInMilliseconds timeout
     * @return process output
     * @throws ExecutionException
     */
    @NotNull
    public static ProcessOutput execute(@NotNull GeneralCommandLine commandLine, int timeoutInMilliseconds) throws ExecutionException {
        return execute(commandLine,null,timeoutInMilliseconds);
    }
}