package org.meaningfulweb.opengraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
 Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

public class OGObject implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final static String[] REQUIRED_META = new String[]{OpenGraphVocabulary.TITLE, OpenGraphVocabulary.TYPE, OpenGraphVocabulary.IMAGE, OpenGraphVocabulary.URL };
	public static final String MEDIA_URL = "url";

	private final Map<String,String> _meta = new HashMap<String,String>();
	
	private Map<String,String> _audio = new HashMap<String,String>();
	private Map<String,String> _video = new HashMap<String,String>();
	
	public Map<String,String> getAudio() {
		return _audio;
	}
	
	public Map<String,String> getVideo() {
		return _video;
	}
	
	public Map<String,String> getMeta(){
		return _meta;
	}
	
	public boolean isEmpty(){
		return _audio.isEmpty() && _video.isEmpty() && _meta.isEmpty();
	}
	
	public boolean isValid(){
		for (String name : REQUIRED_META){
			if (!_meta.containsKey(name)) return false;
		}
		
		if (!_video.isEmpty()){
			if (!_video.containsKey(MEDIA_URL)) return false;
		}
		
		if (!_audio.isEmpty()){
			if (!_audio.containsKey(MEDIA_URL)) return false;
		}
		
		return true;
	}
	
	public String getTitle(){
		return _meta.get(OpenGraphVocabulary.TITLE);
	}
	
	public String getImage(){
		return _meta.get(OpenGraphVocabulary.IMAGE);
	}
	
	public String getType(){
		return _meta.get(OpenGraphVocabulary.TYPE);
	}
	
	public String getUrl(){
		return _meta.get(OpenGraphVocabulary.URL);
	}
	
	public String getDescription(){
		return _meta.get(OpenGraphVocabulary.DESCRIPTION);
	}
	
	public String getSiteName(){
		return _meta.get(OpenGraphVocabulary.SITE_NAME);
	}
	
	public String getStreetAddress(){
		return _meta.get(OpenGraphVocabulary.STREET_ADDRESS);
	}
	
	public String getLocality(){
		return _meta.get(OpenGraphVocabulary.LOCALITY);
	}
	
	public String getRegion(){
		return _meta.get(OpenGraphVocabulary.REGION);
	}
	
	public String getPostalCode(){
		return _meta.get(OpenGraphVocabulary.POSTAL_CODE);
	}
	
	public String getCountryName(){
		return _meta.get(OpenGraphVocabulary.COUNTRY_NAME);
	}
	
	public float getLatitude(){
		float lat;
		try{
		   lat = Float.parseFloat(_meta.get(OpenGraphVocabulary.LATITUDE));	
		}
		catch(Exception e){
			lat = Float.NaN;
		}
		return lat;
	}
	
	public float getLongitude(){
		float lon;
		try{
			lon = Float.parseFloat(_meta.get(OpenGraphVocabulary.LONGITUDE));	
		}
		catch(Exception e){
			lon = Float.NaN;
		}
		return lon;
	}
	
	public String getEmail(){
		return _meta.get(OpenGraphVocabulary.EMAIL);
	}
	
	public String getPhoneNumber(){
		return _meta.get(OpenGraphVocabulary.PHONE_NUMBER);
	}
	
	public String getFaxNumber(){
		return _meta.get(OpenGraphVocabulary.FAX_NUMBER);
	}
	
	public String getAudioProp(String prop){
		return _audio.get(prop);
	}
	
	public String getVideoProp(String prop){
		return _video.get(prop);
	}
	
	public int getAudioWidth(){
		int width;
		try{
			width = Integer.parseInt(getAudioProp("width"));
		}
		catch(Exception e){
			width = -1;
		}
		return width;
	}
	
	public int getAudioHeight(){
		int height;
		try{
			height = Integer.parseInt(getAudioProp("height"));
		}
		catch(Exception e){
			height = -1;
		}
		return height;
	}
	
	public String getAudioType(){
		return getAudioProp("type");
	}
	
	public int getVideoWidth(){
		int width;
		try{
			width = Integer.parseInt(getVideoProp("width"));
		}
		catch(Exception e){
			width = -1;
		}
		return width;
	}
	
	public int getVideoHeight(){
		int height;
		try{
			height = Integer.parseInt(getVideoProp("height"));
		}
		catch(Exception e){
			height = -1;
		}
		return height;
	}
	
	public String getVideoType(){
		return getVideoProp("type");
	}
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("meta: ").append(_meta);
		buf.append("\naudio: ").append(_audio);
		buf.append("\nvideo: ").append(_video);
		return buf.toString();
	}
}
