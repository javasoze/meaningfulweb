package org.meaningfulweb.cext;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.meaningfulweb.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class HtmlContentProcessorFactory {

  private final static Logger LOG = LoggerFactory
    .getLogger(HtmlContentProcessorFactory.class);

  Map<String, Class> classes = new HashMap<String, Class>();
  Map<String, HtmlContentProcessor> components = new HashMap<String, HtmlContentProcessor>();
  Map<String, List<String>> configs = new HashMap<String, List<String>>();
  Map<String, List<String>> pipelines = new HashMap<String, List<String>>();

  private void handleOverlay(String name, HtmlContentProcessor processor,
    Object overlay) {

    if (overlay != null) {
      try {
        if (overlay instanceof HtmlContentProcessor) {
          HtmlContentProcessor overlayProc = (HtmlContentProcessor)overlay;
          List<String> props = configs.get(name);
          if (props != null && props.size() > 0) {
            for (String prop : props) {
              Object value = PropertyUtils.getProperty(overlayProc, prop);
              try {
                PropertyUtils.setProperty(processor, prop, value);
              }
              catch (Exception e) {
                // log and continue setting properties, ignore invalid
                LOG.error("ignoring invalid property: " + name + ":" + prop);
              }
            }
          }
        }
        else {
          Map<String, Object> overlayMap = (Map<String, Object>)overlay;
          if (overlayMap != null && overlayMap.size() > 0) {
            for (String prop : overlayMap.keySet()) {
              Object value = overlayMap.get(prop);
              try {
                PropertyUtils.setProperty(processor, prop, value);
              }
              catch (Exception e) {
                // log and continue setting properties, ignore invalid
                LOG.error("ignoring invalid property: " + name + ":" + prop);
              }
            }
          }
        }
      }
      catch (Exception e) {
        // just log the error, overlays are best completion instead of fail fast
        LOG.error("Error processing overlay", e);
      }
    }
  }
  
  public HtmlContentProcessorFactory(Resource configRes) throws Exception{
	  this(configRes.getFile());
  }
  

  public HtmlContentProcessorFactory(File configFile)
    throws Exception {
	  this(FileUtils.readFileToString(configFile));
  }
  

  public HtmlContentProcessorFactory(String jsonString)
    throws Exception {

	 this(JsonUtils.parseJson(jsonString));
  }

  public HtmlContentProcessorFactory(JsonNode root)
    throws Exception {
    JsonNode compNodes = root.get("components");
    if (compNodes != null) {
      for (JsonNode compNode : compNodes) {

        String name = JsonUtils.getStringValue(compNode, "name");
        String className = JsonUtils.getStringValue(compNode, "class");
        Class compClass = null;
        try {
          compClass = Class.forName(className);
          classes.put(name, compClass);
        }
        catch (Exception e) {
          e.printStackTrace();
          continue;
        }
        JsonNode compConfig = compNode.get("config");
        if (compConfig != null) {
          List<String> configPropNames = JsonUtils.getFieldNames(compConfig);
          configs.put(name, configPropNames);
          components.put(
            name,
            (HtmlContentProcessor)JsonUtils.deserializeFromJson(
              compConfig.toString(), compClass));
        }
        else {
          components.put(name, (HtmlContentProcessor)compClass.newInstance());
        }
      }
    }

    JsonNode pipelineNodes = root.get("pipelines");
    if (pipelineNodes != null) {
      for (JsonNode plNode : pipelineNodes) {
        String plName = JsonUtils.getStringValue(plNode, "name");
        List<String> pipeline = new ArrayList<String>();
        JsonNode plCompNodes = plNode.get("components");
        if (compNodes != null) {
          for (JsonNode plCompNode : plCompNodes) {
            String name = JsonUtils.getStringValue(plCompNode, "name");
            pipeline.add(name);
            JsonNode compConfig = plCompNode.get("config");
            if (compConfig != null) {
              String json = compConfig.toString();
              Class compClass = classes.get(name);
              String fullname = plName + "." + name;
              List<String> configPropNames = JsonUtils
                .getFieldNames(compConfig);
              configs.put(fullname, configPropNames);
              HtmlContentProcessor plProc = (HtmlContentProcessor)JsonUtils
                .deserializeFromJson(json, compClass);
              components.put(fullname, plProc);
            }
          }
        }
        pipelines.put(plName, pipeline);
      }
    }
  }

  public HtmlContentProcessor getComponent(String fullname,
    Map<String, Object> runtime) {

    String[] names = StringUtils.split(fullname, ".");
    boolean hasPipeline = (names.length == 2);
    String component = hasPipeline ? names[1] : names[0];

    // new instance of the component class
    Class compClass = classes.get(component);
    HtmlContentProcessor processor;
    try {
      processor = (HtmlContentProcessor)compClass.newInstance();
    }
    catch (Exception e) {
      LOG.error("Error creating processor: " + compClass, e);
      return null;
    }

    // base component overlay
    HtmlContentProcessor baseProc = components.get(component);
    if (baseProc != null) {
      handleOverlay(component, processor, baseProc);
    }

    // pipeline component overlay
    if (hasPipeline) {
      HtmlContentProcessor plProc = components.get(fullname);
      handleOverlay(fullname, processor, plProc);
    }

    // runtime overlay
    if (runtime != null) {

      // allow for runtime component overlay
      Object rtCompObj = runtime.get(component);
      handleOverlay(component, processor, rtCompObj);

      // if there is a pipeline allow for runtime pipeline component overlay
      if (hasPipeline) {
        Object rtPlObj = runtime.get(fullname);
        handleOverlay(fullname, processor, rtPlObj);
      }
    }

    // name is set as to not be overwritten
    processor.setName(component);
    return processor;
  }

  public HtmlContentPipeline getPipeline(String name,
    Map<String, Object> runtime) {
    List<String> plCompNames = pipelines.get(name);
    if (plCompNames != null && plCompNames.size() > 0) {
      List<HtmlContentProcessor> plComps = new ArrayList<HtmlContentProcessor>();
      for (String plCompName : plCompNames) {
        String fullname = name + "." + plCompName;
        HtmlContentProcessor curComp = getComponent(fullname, runtime);
        plComps.add(curComp);
      }
      HtmlContentPipeline pipeline = new HtmlContentPipeline();
      pipeline.setName(name);
      pipeline.setProcessors(plComps);
      return pipeline;
    }
    return null;
  }

  public List<HtmlContentProcessor> getComponents(List<String> names,
    Map<String, Object> runtime) {

    List<HtmlContentProcessor> processors = new ArrayList<HtmlContentProcessor>();
    for (String name : names) {
      HtmlContentProcessor processor = getComponent(name, runtime);
      if (processor != null) {
        processors.add(processor);
      }
    }
    return processors;
  }

  public Set<String> getComponentNames() {
    return components.keySet();
  }

  public Set<String> getPipelineNames() {
    return pipelines.keySet();
  }
}
