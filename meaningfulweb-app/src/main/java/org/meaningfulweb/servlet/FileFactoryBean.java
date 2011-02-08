package org.meaningfulweb.servlet;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

public class FileFactoryBean
  implements FactoryBean {

  private Resource backing;

  public void setBacking(Resource backing) {
    this.backing = backing;
  }

  @Override
  public Class getObjectType() {
    return File.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  public Object getObject() {
    try {
      File backingFile = backing.getFile();
      return backingFile;
    }
    catch (IOException e) {
      return null;
    }
  }
}
