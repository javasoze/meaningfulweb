package org.meaningfulweb.util.http;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.meaningfulweb.util.URIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpComponentsServiceImpl
  implements HttpClientService {

  private static class GzipDecompressingEntity
    extends HttpEntityWrapper {

    public GzipDecompressingEntity(final HttpEntity entity) {
      super(entity);
    }

    @Override
    public InputStream getContent()
      throws IOException, IllegalStateException {
      // the wrapped entity's getContent() decides about repeatability
      InputStream wrappedin = wrappedEntity.getContent();
      return new GZIPInputStream(wrappedin);
    }

    @Override
    public long getContentLength() {
      // length of ungzipped content is not known
      return -1;
    }

  }

  private final static Logger LOG = LoggerFactory
    .getLogger(HttpComponentsServiceImpl.class);

  private AtomicBoolean active = new AtomicBoolean(false);

  private int connectionsTotal = 1000;
  private int connectionsPerHost = 50;
  private int connectionTimeoutInSeconds = 20;
  private int socketTimeoutInSeconds = 10;
  private int idleConnectionTimeoutInSeconds = 10;
  private boolean tcpNoDelay = true;
  private int soLingerInSeconds = 0;

  private String userAgent;

  ThreadSafeClientConnManager connMgr;
  private DefaultHttpClient httpClient;
  private int cullInterval = 2500;
  private ConnectionCuller cullerThread = null;

  private class ConnectionCuller
    extends Thread {

    @Override
    public void run() {

      LOG.info("Connection culling thread starting up");

      while (active.get()) {
        try {
          Thread.sleep(cullInterval);
          if (connMgr != null) {
            LOG.debug("Connections in pool:" + connMgr.getConnectionsInPool());
            httpClient.getConnectionManager().closeIdleConnections(
              idleConnectionTimeoutInSeconds, TimeUnit.SECONDS);
          }
        }
        catch (InterruptedException e) {
          // do nothing and continue
        }
      }

      LOG.info("Connection culling thread shutting down");
    }
  }

  public HttpComponentsServiceImpl() {

  }

  public void initialize() {

    HttpParams params = new BasicHttpParams();

   // turn on redirecting
    HttpClientParams.setRedirecting(params,true);

    params.setParameter(HttpProtocolParams.USER_AGENT, userAgent);
    params.setParameter(HttpProtocolParams.PROTOCOL_VERSION,
      HttpVersion.HTTP_1_1);
    params.setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");
    params.setParameter("http.protocol.cookie-policy",
      CookiePolicy.BROWSER_COMPATIBILITY);
    params.setBooleanParameter("http.protocol.single-cookie-header", true);
    params.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
      connectionTimeoutInSeconds * 1000);
    params.setIntParameter(HttpConnectionParams.SO_LINGER, soLingerInSeconds);
    params.setBooleanParameter(HttpConnectionParams.TCP_NODELAY, tcpNoDelay);
    params.setIntParameter(HttpConnectionParams.SO_TIMEOUT,
      socketTimeoutInSeconds * 1000);

    params.setParameter(ConnManagerParams.MAX_CONNECTIONS_PER_ROUTE,
      new ConnPerRouteBean(connectionsPerHost));
    params.setIntParameter(ConnManagerParams.MAX_TOTAL_CONNECTIONS,
      connectionsTotal);

    SchemeRegistry registry = new SchemeRegistry();
    SocketFactory http = PlainSocketFactory.getSocketFactory();
    registry.register(new Scheme("http", http, 80));
    SocketFactory https = SSLSocketFactory.getSocketFactory();
    registry.register(new Scheme("https", https, 443));

    connMgr = new ThreadSafeClientConnManager(params, registry);

    httpClient = new DefaultHttpClient(connMgr, params);

    httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
      public void process(final HttpRequest request, final HttpContext context)
        throws HttpException, IOException {
        if (!request.containsHeader("Accept-Encoding")) {
          request.addHeader("Accept-Encoding", "gzip");
        }
      }
    });

    httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
      public void process(final HttpResponse response, final HttpContext context)
        throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        if (entity==null) return;
        Header ceheader = entity.getContentEncoding();
        if (ceheader != null) {
          HeaderElement[] codecs = ceheader.getElements();
          for (int i = 0; i < codecs.length; i++) {
            if (codecs[i].getName().equalsIgnoreCase("gzip")) {
              response.setEntity(new GzipDecompressingEntity(response
                .getEntity()));
              return;
            }
          }
        }
      }
    });

    active.set(true);

    // start up the idle connection culling thread
    cullerThread = new ConnectionCuller();
    cullerThread.setDaemon(true);
    cullerThread.start();
  }

  public void shutdown() {
    active.set(false);
    httpClient.getConnectionManager().shutdown();
  }
  
  public byte[] get(InputStream is) throws IOException{
	ByteArrayOutputStream baos = null;

    try {
      baos = new ByteArrayOutputStream();
      IOUtils.copy(is, baos);
      return baos.toByteArray();
    }
    finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(baos);
    }
  }
  

  public byte[] get(String url)
    throws HttpException {
	 HttpGet httpget = null;

	 // if the uri is invalid try and clean it up a little before fetching
	 boolean isValidURI = URIUtils.isValidURI(url);
	 if (!isValidURI) {
	    String fixed = URIUtils.fixInvalidUri(url);
	    LOG.info("Fixed invalid URI: " + url + " to " + fixed);
	    url = fixed;
	 }

	 httpget = new HttpGet(url);
	  
	 HttpEntity entity = doGet(httpget);
	 if (entity != null) {
		try{
	      InputStream is = entity.getContent();
	      return get(is);
		}
	    catch (Exception e) {
	      LOG.error("Error getting url: " + url, e);
	      httpget.abort();
	      throw new HttpException(e);
	    }
	 }
	 return null;
  }
  
  @Override
  public HttpResponse doHead(HttpHead httpget) throws HttpException {

  // return immediately if we are shutting down and no longer active
  if (!active.get()) {
    return null;
  }

  try {
    HttpResponse response = httpClient.execute(httpget);
    StatusLine status = response.getStatusLine();
    int statusCode = status.getStatusCode();
    if (statusCode >= 400) {
      throw new HttpException(statusCode, httpget.getURI()+":"+status.getReasonPhrase());
    }
    return response;
  }
  catch (HttpException he) {
    throw he;
  }
  catch (Exception e) {
    LOG.error("Error getting url: " + httpget.getURI(), e);
    httpget.abort();
    throw new HttpException(e);
  }

}
  
  @Override 

  public HttpResponse process(HttpGet httpget,HttpContext ctx) throws IOException{
	  return  httpClient.execute(httpget,ctx);
  }
  
  @Override
  public HttpEntity doGet(HttpGet httpget)
    throws HttpException {

    // return immediately if we are shutting down and no longer active
    if (!active.get()) {
      return null;
    }

    try {
      HttpResponse response = httpClient.execute(httpget);
      HttpEntity entity = response.getEntity();
      StatusLine status = response.getStatusLine();
      int statusCode = status.getStatusCode();
      if (statusCode >= 400) {
        try {
          IOUtils.closeQuietly(entity.getContent());
        }
        catch (Exception e) {
        }
        throw new HttpException(statusCode, status.getReasonPhrase());
      }
      return entity;
    }
    catch (HttpException he) {
      throw he;
    }
    catch (Exception e) {
      LOG.error("Error getting url: " + httpget.getURI(), e);
      httpget.abort();
      throw new HttpException(e);
    }

  }

  public boolean ping(String url) {

    // return immediately if we are shutting down and no longer active
    if (!active.get()) {
      return false;
    }

    try {

      HttpGet httpget = new HttpGet(url);
      HttpResponse response = httpClient.execute(httpget);
      StatusLine status = response.getStatusLine();
      int statusCode = status.getStatusCode();
      if (statusCode == 200) {
        httpget.abort();
        return true;
      }
    }
    catch (Exception e) {
      return false;
    }

    return false;
  }

  public byte[] postAsBody(String url, String body, String mimeType,
    String encoding)
    throws HttpException {

    // return immediately if we are shutting down and no longer active
    if (!active.get()) {
      return null;
    }

    HttpPost httppost = null;
    try {

      httppost = new HttpPost(url);
      httppost.setEntity(new StringEntity(body, "UTF-8"));

      HttpResponse response = httpClient.execute(httppost);
      HttpEntity entity = response.getEntity();
      StatusLine status = response.getStatusLine();
      int statusCode = status.getStatusCode();
      if (statusCode >= 400) {
        throw new HttpException(statusCode, status.getReasonPhrase());
      }

      if (entity != null) {

        InputStream is = null;
        ByteArrayOutputStream baos = null;

        try {
          is = entity.getContent();
          baos = new ByteArrayOutputStream();
          IOUtils.copy(is, baos);
          return baos.toByteArray();
        }
        finally {
          IOUtils.closeQuietly(is);
          IOUtils.closeQuietly(baos);
        }
      }

      return null;
    }
    catch (HttpException he) {
      httppost.abort();
      throw he;
    }
    catch (Exception e) {
      httppost.abort();
      throw new HttpException(e);
    }
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public void setConnectionsTotal(int connectionsTotal) {
    this.connectionsTotal = connectionsTotal;
  }

  public void setConnectionsPerHost(int connectionsPerHost) {
    this.connectionsPerHost = connectionsPerHost;
  }

  public void setConnectionTimeoutInSeconds(int connectionTimeoutInSeconds) {
    this.connectionTimeoutInSeconds = connectionTimeoutInSeconds;
  }

  public void setSocketTimeoutInSeconds(int socketTimeoutInSeconds) {
    this.socketTimeoutInSeconds = socketTimeoutInSeconds;
  }

  public void setTcpNoDelay(boolean tcpNoDelay) {
    this.tcpNoDelay = tcpNoDelay;
  }

  public void setSoLingerInSeconds(int soLingerInSeconds) {
    this.soLingerInSeconds = soLingerInSeconds;
  }

  public void setIdleConnectionTimeoutInSeconds(
    int idleConnectionTimeoutInSeconds) {
    this.idleConnectionTimeoutInSeconds = idleConnectionTimeoutInSeconds;
  }

  public void setCullInterval(int cullInterval) {
    this.cullInterval = cullInterval;
  }

}
