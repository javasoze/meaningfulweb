package org.meaningfulweb.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tika.metadata.Metadata;
import org.json.JSONException;
import org.json.JSONObject;
import org.meaningfulweb.api.MetaContentExtractor;

import org.meaningfulweb.opengraph.OGObject;

public class MeaningfulWebServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Charset utf8 = Charset.forName("UTF-8");
	private HttpClient httpClient = new HttpClient();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	                        throws ServletException, IOException {
	  try {
		String url = req.getParameter("url");
		JSONObject resObj = extractContent(url);
        resp.setContentType("text/plain; charset=utf-8");
		resp.setCharacterEncoding("UTF-8");
		
		OutputStream ostream = resp.getOutputStream();
		ostream.write(resObj.toString().getBytes(utf8));
		ostream.flush();
	  } catch (Exception e) {
		throw new ServletException(e.getMessage(),e);
	  }
	}
	
	private static JSONObject convert(Map<String,String> map,JSONObject obj) throws JSONException{
		if (obj == null){
			obj = new JSONObject();
		}
		
		Set<Entry<String,String>> entries = map.entrySet();
		for (Entry<String,String> entry : entries){
			String name = entry.getKey();
			String val = entry.getValue();
			if (name!=null && val!=null){
				obj.put(name, val);
			}
		}
		
		return obj;
	}
	
	private JSONObject extractContent(String url) throws Exception{
		GetMethod get = null;
		JSONObject resObj = new JSONObject();
		try{
			MetaContentExtractor extractor = new MetaContentExtractor();
			get = new GetMethod(url);
			httpClient.executeMethod(get);
			
			Metadata metadata = new Metadata();
			metadata.add(Metadata.RESOURCE_NAME_KEY, url);
			metadata.add(Metadata.CONTENT_TYPE, get.getResponseHeader(Metadata.CONTENT_TYPE).getValue());
			OGObject obj = extractor.extract(url, get.getResponseBodyAsStream(), metadata,get.getResponseCharSet());
			
			resObj = convert(obj.getMeta(),resObj);
			
			Map<String,String> audioMap = obj.getAudio();
			if (audioMap!=null && audioMap.size()>0){
				resObj.put("audio",convert(audioMap,null));
			}

			Map<String,String> videoMap = obj.getVideo();
			if (videoMap!=null && videoMap.size()>0){
				resObj.put("video",convert(videoMap,null));
			}

			return resObj;
		}
		finally{
			if (get!=null){
				get.releaseConnection();
			}
		}
	}
}
