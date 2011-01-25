package org.meaningfulweb.api;

import java.io.InputStream;
import java.util.Map;

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.meaningfulweb.detector.DetectorBuilder;

import proj.og4j.entities.OGObject;

public class MetaContentExtractor {

	
	private final Detector _detector;
	
	public MetaContentExtractor(){
	  _detector = DetectorBuilder.getInstance(null).buildDetector();
	}
	
	public OGObject extract(String url,InputStream in,Metadata meta) throws Exception{
	  OGObject obj = new OGObject();
	  Map<String,String> ogMeta = obj.getMeta();
	  MediaType type = _detector.detect(in, meta);
	  if ("image".equals(type.getType())){
		ogMeta.put("image", url);
		ogMeta.put("type", "image");
		ogMeta.put("title", url);
		ogMeta.put("url", url);
	  }
	  else if ("video".equals(type.getType())){
		ogMeta.put("image", "");
		ogMeta.put("type", "video");
		ogMeta.put("title", url);
		ogMeta.put("url", url);
	  }
	  else if ("text".equals(type.getType())){
		String subtype = type.getSubtype();
		if ("plain".equals(subtype)){
		  
		}
		else if ("html".equals(subtype)){
			
		}
	  }
	  else if ("application".equals(type.getType())){
		
	  }
	  else{
		  
	  }
	  
	  return obj;
	}
}
