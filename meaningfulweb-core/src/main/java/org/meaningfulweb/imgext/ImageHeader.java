/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meaningfulweb.imgext;

import java.util.Date;

/**
 * Simple representation of useful http header fields for an image from the web.
 */
public class ImageHeader
{
  private final long _contentLength;
  private final Date _modifiedDate;
  private final String _mimeType;
  public ImageHeader(long contentLength, Date modifiedDate, String mimeType)
  {
    _contentLength = contentLength;
    _modifiedDate = modifiedDate;
    _mimeType = mimeType;
  }
  
  /**
   * HTTP "Content-Length" header value
   * @return
   */
  public long getContentLength()
  {
    return _contentLength;
  }
  
  /**
   * HTTP "Last-Modified" header value
   * @return
   */
  public Date getModifiedDate()
  {
    return _modifiedDate;
  }
  
  /**
   * HTTP "Content-Type" header value
   * @return
   */
  public String getMimeType()
  {
    return _mimeType;
  }
  
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("ImageHeader(");
    builder.append("contentLength=");
    builder.append(_contentLength);
    builder.append(", modifiedDate=");
    builder.append(_modifiedDate);
    builder.append(", mimeType=");
    builder.append(_mimeType);
    builder.append(")");
    return builder.toString();
  }
}

