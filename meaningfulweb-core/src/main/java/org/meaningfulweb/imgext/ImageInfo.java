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
import java.io.Serializable;

public class ImageInfo implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  private final String _uri;
  private final String _title;
  private final Integer _width;
  private final Integer _height;
  
  public ImageInfo(String url)
  {
    this(url, null, null, null);
  }
  
  public ImageInfo(String uri, String title, Integer width, Integer height)
  {
    _uri = uri;
    _title = title;
    _width = width;
    _height = height;
  }
  
  public String getUri()
  {
    return _uri;
  }
  
  public String getTitle()
  {
    return _title;
  }
  
  public Integer getWidth()
  {
    return _width;
  }
  
  public Integer getHeight()
  {
    return _height;
  }
  
  public String toString(){
	  StringBuilder buf = new StringBuilder();
	  buf.append("uri: ").append(_uri).append("\n");
	  buf.append("title: ").append(_title).append("\n");
	  buf.append("width: ").append(_width).append("\n");
	  buf.append("height: ").append(_height);
	  return buf.toString();
  }
}
