package org.meaningfulweb.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.meaningfulweb.api.MetaContentExtractor;
import org.meaningfulweb.api.MeaningfulWebObject;

public class MeaningfulWebServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Charset utf8 = Charset.forName("UTF-8");
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	                        throws ServletException, IOException {
	  try {
		String url = req.getParameter("url");
		try{
			URL urlObj = new URL(url);
			url = urlObj.toExternalForm();
		}
		catch(MalformedURLException me){
			url="http://"+url;
		}
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
		JSONObject resObj = new JSONObject();
		MetaContentExtractor extractor = new MetaContentExtractor();
		MeaningfulWebObject obj = extractor.extractFromUrl(url);
			
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
}
