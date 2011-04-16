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

public class ImageMeta extends ImageInfo{
	private static final long serialVersionUID = 1L;
	  
	  int _position;
	  String _alt;
	  boolean _hasOnclick;
	  
	  public ImageMeta(int position, String alt, String title, String width, String height, Long size,String src, String onClick)
	  {
	    super(src, title, toInteger(width), toInteger(height),size);
	    _position = position;
	    _alt = alt;
	    _hasOnclick = onClick!=null && onClick.length()>0;
	  }
	  
	  public int getPosition()
	  {
	    return _position;
	  }
	  
	  public boolean getHasOnclick()
	  {
	    return _hasOnclick;
	  }
	  
	  public String getAlt()
	  {
	    return _alt;
	  }
	  
	  public static Integer toInteger(String val)
	  {
		  try{
			  return Integer.parseInt(val);
		  }
		  catch(Exception e){
			  return null;
		  }
	  }
	  
	  @Override
	  public String toString()
	  {
	    StringBuilder builder = new StringBuilder();
	    builder.append("ImageReference(alt=");
	    builder.append(_alt);
	    builder.append(" title=");
	    builder.append(getTitle());
	    builder.append(" width=");
	    builder.append(getWidth());
	    builder.append(" height=");
	    builder.append(getHeight());
	    builder.append(" src=");
	    builder.append(getUri());
	    builder.append(" hasOnClick=");
	    builder.append(_hasOnclick);
	    builder.append(")");
	    return builder.toString();
	  }
}
