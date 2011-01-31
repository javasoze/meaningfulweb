package org.meaningfulweb.opengraph;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the opengraph vocabulary (http://ogp.me/).
 * 
 * The opengraph vocabulary is "based on" RDFa (http://www.w3.org/TR/rdfa-syntax/).
 * 
 * @author "Joe Betz<jbetz@linkedin.com>"
 * @version $Revision$
 */
public class OpenGraphVocabulary
{
  public static final String DEFAULT_PREFIX = "og:";
  public static final String NAMESPACE = "http://ogp.me/ns";
  
  public static final String
     //basic tags
    TITLE = "title",
    TYPE = "type",
    IMAGE = "image",
    URL = "url",
     
    // optional ("recommended") basic tags
    DESCRIPTION = "description", 
    SITE_NAME = "site_name",
    
    // location
    LATITUDE = "latitude",
    LONGITUDE = "longitude",
    
    // location: human readable
    STREET_ADDRESS = "street-address",
    LOCALITY = "locality",
    REGION = "region",
    POSTAL_CODE = "postal-code",
    COUNTRY_NAME = "country-name",
    
    // contact info
    EMAIL = "email",
    PHONE_NUMBER = "phone_number",
    FAX_NUMBER = "fax_number",
    
    // attach video
    VIDEO = "video",
    VIDEO_PREFIX = "video:",
    VIDEO_HEIGHT = "video:height",
    VIDEO_WIDTH = "video:width",
    VIDEO_TYPE = "video:type",
    
    // attach audio
    AUDIO = "audio",
    AUDIO_PREFIX = "audio:",
    AUDIO_TITLE = "audio:title",
    AUDIO_ARTIST = "audio:artist",
    AUDIO_ALBUM = "audio:album", 
    AUDIO_TYPE = "audio:type"
    ;
  
  public static final String[] BASIC_TAGS = new String[] { TITLE, TYPE, IMAGE, URL};
  public static final String[] OPTIONAL_BASIC_TAGS = new String[] {DESCRIPTION, SITE_NAME};
  public static final String[] LOCATION_GEO_TAGS = new String[] {LATITUDE, LONGITUDE};
  public static final String[] LOCATION_HUMAN_READABLE_TAGS = new String[] {STREET_ADDRESS, LOCALITY, REGION, POSTAL_CODE, COUNTRY_NAME};
  public static final String[] CONTACT_INFO_TAGS = new String[] {EMAIL, PHONE_NUMBER, FAX_NUMBER};
  public static final String[] VIDEO_TAGS = new String[] {VIDEO, VIDEO_HEIGHT, VIDEO_WIDTH, VIDEO_TYPE};
  public static final String[] AUDIO_TAGS = new String[] {AUDIO, AUDIO_TITLE, AUDIO_ARTIST, AUDIO_ALBUM, AUDIO_TYPE};
  
  public static final Set<String> OPENGRAPH_TAGS = new HashSet<String>();
  static {
    for(String[] tagGroup : new String[][] {BASIC_TAGS, OPTIONAL_BASIC_TAGS, LOCATION_GEO_TAGS, LOCATION_HUMAN_READABLE_TAGS, VIDEO_TAGS, AUDIO_TAGS})
    {
      for(String tag : tagGroup)
      {
        OPENGRAPH_TAGS.add(tag);
      }
    }
  }
  
  public static boolean isValidOpengraphTag(String metaProperty)
  {
    if(metaProperty == null) return false;
    return OPENGRAPH_TAGS.contains(metaProperty.toLowerCase().trim());
  }
}

