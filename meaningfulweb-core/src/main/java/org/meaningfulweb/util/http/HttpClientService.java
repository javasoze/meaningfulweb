package org.meaningfulweb.util.http;
public interface HttpClientService {

  public byte[] get(String url)
    throws HttpException;

  public boolean ping(String url);

  public byte[] postAsBody(String url, String body, String mimeType,
    String encoding)
    throws HttpException;

}
