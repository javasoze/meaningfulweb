package org.meaningfulweb.util.http;

public class HttpClientFactory {
  private final static HttpComponentsServiceImpl _httpClientSvc = new HttpComponentsServiceImpl();
  
  static{
	  _httpClientSvc.initialize();
	  
	  String userAgent = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.10) Gecko/20100915 Ubuntu/10.04 (lucid) Firefox/3.6.10 GTB7.1";
	  int totalConns = 3000;
	  int connPerHost = 3000;
	  int connTimeoutSecs = 5;
	  int sockTimeoutSecs = 10;
	  boolean tcpNoDelay = true;
	  int sockLingerSec = 0;
	  int idelConnTimeoutSecs = 20;
	  int cullInterval= 5000;
	  _httpClientSvc.setUserAgent(userAgent);
	  _httpClientSvc.setConnectionsTotal(totalConns);
	  _httpClientSvc.setConnectionsPerHost(connPerHost);
	  _httpClientSvc.setConnectionTimeoutInSeconds(connTimeoutSecs);
	  _httpClientSvc.setSocketTimeoutInSeconds(sockTimeoutSecs);
	  _httpClientSvc.setTcpNoDelay(tcpNoDelay);
	  _httpClientSvc.setSoLingerInSeconds(sockLingerSec);
	  _httpClientSvc.setIdleConnectionTimeoutInSeconds(idelConnTimeoutSecs);
	  _httpClientSvc.setCullInterval(cullInterval);
	  
	  Runtime.getRuntime().addShutdownHook(new Thread(){
		  public void run(){
			_httpClientSvc.shutdown();
		  }
	  });
  }
  
  private HttpClientFactory(){
	  
  }
  
  public static HttpClientService getHttpClientService(){
	  return _httpClientSvc;
  }
}
