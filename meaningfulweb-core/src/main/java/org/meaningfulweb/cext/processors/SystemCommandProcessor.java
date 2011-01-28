package org.meaningfulweb.cext.processors;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.JsonUtils;
import org.meaningfulweb.util.ProcessUtils;
import org.meaningfulweb.util.SystemCommand;
import org.meaningfulweb.util.TempDirUtils;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.JsonNode;
import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCommandProcessor
  extends HtmlContentProcessor {

  private final static Logger LOG = LoggerFactory
  .getLogger(SystemCommandProcessor.class);

  private String commandDir = null;
  private int timeoutInSeconds = 60;
  private List<String> commands = new ArrayList<String>();

  public String getCommandDir() {
    return commandDir;
  }

  public void setCommandDir(String commandDir) {
    this.commandDir = commandDir;
  }

  public int getTimeoutInSeconds() {
    return timeoutInSeconds;
  }

  public void setTimeoutInSeconds(int timeoutInSeconds) {
    this.timeoutInSeconds = timeoutInSeconds;
  }

  public List<String> getCommands() {
    return commands;
  }

  public void setCommands(List<String> commands) {
    this.commands = commands;
  }

  private File getWorkspace() {

    File tempRoot = new File(TempDirUtils.getTempDirectory(), "__workspaces__");
    if (!tempRoot.exists()) {
      tempRoot.mkdirs();
    }
    int rand = RandomUtils.nextInt(1000000);
    File workspace = new File(tempRoot, System.currentTimeMillis() + "_" + rand);
    workspace.mkdir();
    return workspace;
  }

  @Override
  public boolean processContent(Document document) {

    if (commands != null && commands.size() > 0) {

      Document tempDoc = new Document();
      tempDoc.addContent(document.cloneContent());

      for (String command : commands) {

        try {

          File commandFile = new File(commandDir, command);
          if (!commandFile.exists()) {
            continue;
          }

          File workspace = getWorkspace();

          File inputFile = new File(workspace, "input.html");
          FileUtils.writeStringToFile(inputFile, XMLUtils.toXml(tempDoc));

          File outputFile = new File(workspace, "output.json");

          File metaFile = new File(workspace, "metadata.json");
          String metaJson = JsonUtils.serializeToJson(getMetadata());
          FileUtils.writeStringToFile(inputFile, metaJson);

          LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
          params.put("command", commandDir + command);
          params.put("inputPath", inputFile.getPath());
          params.put("outputPath", outputFile.getPath());
          params.put("metadataPath", metaFile.getPath());

          SystemCommand systemCommand = new SystemCommand();
          String commandStr = "${command} \"${inputPath}\" \"${outputPath}\"" +
          		" \"${metadataPath}\"";
          systemCommand.setCommand(commandStr);
          systemCommand.setWorkingDirectory(workspace);
          systemCommand.setTimeoutInSeconds(timeoutInSeconds);
          systemCommand.setParams(params);

          // run the system command
          ProcessUtils.runSystemCommand(systemCommand);
          boolean failed = systemCommand.isFailed();

          if (!failed && outputFile.exists()) {
            String json = FileUtils.readFileToString(outputFile);
            if (StringUtils.isNotBlank(json)) {
              JsonNode root = JsonUtils.parseJson(json);
              addExtractedValue("commands", root);
            }
          }

          // remove the workspace
          FileUtils.deleteQuietly(workspace);
        }
        catch (Exception e) {
          LOG.error("Error processing command: " + command, e);
          continue;
        }
      }
    }

    return true;
  }
}
