package org.meaningfulweb.util.http;

public class HttpClientFactory {
  private final static HttpComponentsServiceImpl _httpClientSvc = new HttpComponentsServiceImpl();
  
  static{
	  _httpClientSvc.initialize();
	  
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
