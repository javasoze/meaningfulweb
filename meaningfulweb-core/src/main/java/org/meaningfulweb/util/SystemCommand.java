package org.meaningfulweb.util;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SystemCommand
  implements Serializable, Cloneable {

  private LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
  private String command = null;
  private int timeoutInSeconds = -1;
  private String output = null;
  private int exitValue = 0;
  private boolean failed = false;
  private boolean killed = false;
  private Exception exception = null;
  private File workingDirectory = null;
  private String commandRun = null;

  public SystemCommand() {

  }

  public LinkedHashMap<String, String> getParams() {
    return params;
  }

  public void setParams(LinkedHashMap<String, String> params) {
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public int getTimeoutInSeconds() {
    return timeoutInSeconds;
  }

  public void setTimeoutInSeconds(int timeoutInSeconds) {
    this.timeoutInSeconds = timeoutInSeconds;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public int getExitValue() {
    return exitValue;
  }

  public void setExitValue(int exitValue) {
    this.exitValue = exitValue;
  }

  public boolean isFailed() {
    return failed;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  public boolean isKilled() {
    return killed;
  }

  public void setKilled(boolean killed) {
    this.killed = killed;
  }

  public Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public File getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public String getCommandRun() {
    return commandRun;
  }

  public void setCommandRun(String commandRun) {
    this.commandRun = commandRun;
  }

  public Object clone()
    throws CloneNotSupportedException {
    return super.clone();
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
}
