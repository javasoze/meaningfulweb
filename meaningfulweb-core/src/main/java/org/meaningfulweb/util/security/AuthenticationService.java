package org.meaningfulweb.util.security;
public interface AuthenticationService {

  public boolean isAuthorized(String login, String apiKey);
  
}
