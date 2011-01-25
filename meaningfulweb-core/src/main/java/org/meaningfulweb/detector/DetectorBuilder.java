package org.meaningfulweb.detector;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.TypeDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

public class DetectorBuilder {
	
	private DetectorBuilder(){
		
	}
	
	public static DetectorBuilder getInstance(Configuration config){
		return new DetectorBuilder();
	}

	public Detector buildDetector(){
		return new Detector(){
			private Detector typeDetector = new TypeDetector();
			private Detector defaultDetector = new DefaultDetector();
			
			@Override
			public MediaType detect(InputStream input, Metadata metadata)
					throws IOException {
				MediaType type = typeDetector.detect(input, metadata);
				if (MediaType.OCTET_STREAM == type){
					type = defaultDetector.detect(input, metadata);
				}
				return type;
			}
			
		};
	}
}
