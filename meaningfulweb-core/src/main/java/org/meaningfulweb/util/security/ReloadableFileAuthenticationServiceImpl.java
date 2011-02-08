package org.meaningfulweb.util.security;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadableFileAuthenticationServiceImpl
  implements AuthenticationService {

  private final static Logger LOG = LoggerFactory
    .getLogger(ReloadableFileAuthenticationServiceImpl.class);

  private File authFile;
  private long pollFrequencyInSeconds = 60;
  private Map<String, String> authMap = new ConcurrentHashMap<String, String>();
  private Thread reloaderThread;

  private AtomicBoolean active = new AtomicBoolean(false);

  private class ReloaderThread
    extends Thread {

    private AtomicLong lastModified = new AtomicLong(0L);

    private void reloadAuthMap()
      throws IOException {

      Map<String, String> newAuths = new HashMap<String, String>();
      Scanner scanner = new Scanner(authFile);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] loginApiKeys = StringUtils.split(line, "=");
        if (loginApiKeys != null && loginApiKeys.length > 0) {
          newAuths.put(StringUtils.trim(loginApiKeys[0]),
            StringUtils.trim(loginApiKeys[1]));
        }
      }

      // remove any keys no longer in the map
      for (String key : authMap.keySet()) {
        if (!newAuths.containsKey(key)) {
          authMap.remove(key);
        }
      }

      // add and update keys in the map
      for (Entry<String, String> entry : newAuths.entrySet()) {
        authMap.put(entry.getKey(), entry.getValue());
      }
    }

    @Override
    public void run() {

      while (active.get()) {
        try {
          if (authFile == null || !authFile.exists()) {
            LOG.warn("No authentication file found");
          }
          else {
            long currentMod = authFile.lastModified();
            if (currentMod > lastModified.get()) {
              LOG.info("Loading authentication keys file: "
                + authFile.getPath());
              lastModified.set(currentMod);
              reloadAuthMap();
            }
          }
          Thread.sleep(pollFrequencyInSeconds * 1000);
        }
        catch (Exception e) {
          LOG.error("Error loading auth file: " + authFile.getAbsolutePath(), e);
        }
      }
    }

  }

  public ReloadableFileAuthenticationServiceImpl() {

  }

  public void initialize() {
    active.set(true);
    reloaderThread = new ReloaderThread();
    reloaderThread.setDaemon(true);
    reloaderThread.start();
  }

  public void shutdown() {
    active.set(false);
  }

  public void setAuthFile(File authFile) {
    this.authFile = authFile;
  }

  public void setPollFrequencyInSeconds(long pollFrequencyInSeconds) {
    this.pollFrequencyInSeconds = pollFrequencyInSeconds;
  }

  @Override
  public boolean isAuthorized(String login, String apiKey) {
    if (StringUtils.isEmpty(login) || StringUtils.isEmpty(apiKey)) {
      return false;
    }
    String value = authMap.get(login);
    return value != null && value.equals(apiKey);
  }

}
