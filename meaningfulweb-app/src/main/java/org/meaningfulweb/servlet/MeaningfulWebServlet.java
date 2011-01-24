package org.meaningfulweb.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class MeaningfulWebServlet extends HttpServlet {
	private static Charset utf8 = Charset.forName("UTF-8");
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	                        throws ServletException, IOException {
	  try {
		
		JSONObject resObj = extractContent();
        resp.setContentType("text/plain; charset=utf-8");
		resp.setCharacterEncoding("UTF-8");
		
		OutputStream ostream = resp.getOutputStream();
		ostream.write(resObj.toString().getBytes(utf8));
		ostream.flush();
	  } catch (Exception e) {
		throw new ServletException(e.getMessage(),e);
	  }
	}
	
	private JSONObject extractContent() throws JSONException{
		JSONObject resObj = new JSONObject();
		resObj.put("title", "faketitle");
		return resObj;
	}
}
