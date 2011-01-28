package org.meaningfulweb.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtils {

  private final static Logger LOG = LoggerFactory.getLogger(ProcessUtils.class);

  public static void runSystemCommand(SystemCommand systemCommand) {

    CommandLine commandLine = CommandLine.parse(systemCommand.getCommand(),
      systemCommand.getParams());
    String commandRun = commandLine.toString();
    systemCommand.setCommandRun(commandRun);

    DefaultExecutor executor = new DefaultExecutor();
    int timeoutInSeconds = systemCommand.getTimeoutInSeconds();
    ExecuteWatchdog watchdog = new ExecuteWatchdog((timeoutInSeconds > 0)
      ? timeoutInSeconds * 1000 : -1);
    executor.setWatchdog(watchdog);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
    executor.setStreamHandler(streamHandler);

    File workingDirectory = systemCommand.getWorkingDirectory();
    executor.setWorkingDirectory(workingDirectory != null ? workingDirectory
      : TempDirUtils.getTempDirectory());
    systemCommand.setCommandRun(commandRun);

    int exitValue = -1;
    try {
      LOG.debug("Running command: " + commandRun);
      exitValue = executor.execute(commandLine);
    }
    catch (Exception e) {
      LOG.error("Command failed: " + commandRun, e);
      systemCommand.setException(e);
    }
    finally {
      systemCommand.setExitValue(exitValue);
      boolean failed = executor.isFailure(exitValue);
      systemCommand.setFailed(failed);
      boolean killed = failed && watchdog.killedProcess();
      systemCommand.setKilled(killed);
      systemCommand.setOutput(baos.toString());
    }

    IOUtils.closeQuietly(baos);
  }

  public static ProcessResponse runProcess(List<String> command,
    Map<String, String> envVars, File workingDir)
    throws IOException {

    // create the process including environment variables and working directory
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    if (envVars != null && !envVars.isEmpty()) {
      Map<String, String> env = processBuilder.environment();
      env.putAll(envVars);
    }
    processBuilder.directory(workingDir);
    Process process = processBuilder.start();
    ProcessResponse response = new ProcessResponse();

    // get the error stream content
    InputStream input = process.getErrorStream();
    String errorResponse = IOUtils.toString(input);
    response.setError(errorResponse);

    // get the output stream content
    InputStream output = process.getInputStream();
    String outputResponse = IOUtils.toString(output);
    response.setOutput(outputResponse);

    // wait for the process to exit
    int result = -1;
    try {
      result = process.waitFor();
    }
    catch (InterruptedException ie) {
      // don't do anything on interrupted process
    }
    finally {

      // set the result and close all streams
      response.setResult(result);

      if (process != null) {
        IOUtils.closeQuietly(process.getOutputStream());
        IOUtils.closeQuietly(process.getInputStream());
        IOUtils.closeQuietly(process.getErrorStream());
        process.destroy();
      }
    }

    return response;
  }
}
