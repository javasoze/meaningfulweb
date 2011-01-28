package org.meaningfulweb.util.http;
import java.io.IOException;

public class HttpException
  extends IOException {

  private int statusCode = -1;

  public HttpException() {

  }

  public HttpException(int statusCode, Throwable cause, String message) {
    super(message, cause);
    this.statusCode = statusCode;
  }
  
  public HttpException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public HttpException(int statusCode, Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
  }
  
  public HttpException(int statusCode) {
    this.statusCode = statusCode;
  }

  public HttpException(Throwable cause, String message) {
    super(message, cause);
  }

  public HttpException(String message) {
    super(message);
  }

  public HttpException(Throwable cause) {
    super(cause);
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

}
