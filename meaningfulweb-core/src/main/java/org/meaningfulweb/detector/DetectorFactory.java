package org.meaningfulweb.detector;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.detect.Detector;
import org.apache.tika.detect.TextDetector;
import org.apache.tika.detect.TypeDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

public class DetectorFactory {
	private static DetectorFactory instance = new DetectorFactory();
	private DetectorFactory(){

	}

	public static DetectorFactory getInstance(){
		return instance;
	}

	public Detector buildDetector(){
		return new Detector(){
			private Detector typeDetector = new TypeDetector();
			private Detector textDector = new TextDetector();

			@Override
			public MediaType detect(InputStream input, Metadata metadata)
					throws IOException {
				MediaType type = typeDetector.detect(input, metadata);
				if (MediaType.OCTET_STREAM == type){
					type = textDector.detect(input, metadata);
				}
				return type;
			}

		};
	}
	
	public static void main(String[] args) throws Exception{
		DetectorFactory detector = DetectorFactory.getInstance();
		
	}
}
