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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ImageSizeExtractor {
  public static final class ImageSize{
	  public final int width;
	  public final int height;
	  
	  public ImageSize(int width,int height){
		this.width = width;
		this.height = height;
	  }
  }
  
  public Map<ImageMeta,ImageSize> extractSize(List<ImageMeta> imgMeta);

  public ImageSize extractSize(ImageMeta imgMeta) throws Exception;
  
  public static final class DefaultIImageSizeExtractor implements ImageSizeExtractor{

	public Map<ImageMeta,ImageSize> extractSize(List<ImageMeta> imgMetas){
		HashMap<ImageMeta,ImageSize> map = new HashMap<ImageMeta,ImageSize>();
		for (ImageMeta meta : imgMetas){
			map.put(meta, new ImageSize(meta.getWidth(),meta.getHeight()));
		}
		return map;
	}

	public ImageSize extractSize(ImageMeta imgMeta){
		return new ImageSize(imgMeta.getWidth(),imgMeta.getHeight());
	}
  }
}
