package org.meaningfulweb.util.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.protocol.HttpContext;

public interface HttpClientService {

  public byte[] get(String url)
    throws HttpException;
  
  public byte[] get(InputStream in) throws IOException;

  public boolean ping(String url);
  
  public HttpEntity doGet(HttpGet httpget) throws HttpException;
  
  public HttpResponse process(HttpGet httpget,HttpContext ctx) throws IOException;
  
  public HttpResponse doHead(HttpHead httphead) throws HttpException;
  

  public byte[] postAsBody(String url, String body, String mimeType,
    String encoding)
    throws HttpException;

}
