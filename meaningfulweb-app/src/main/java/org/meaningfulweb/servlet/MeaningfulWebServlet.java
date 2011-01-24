package org.meaningfulweb.servlet;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MeaningfulWebServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	                        throws ServletException, IOException {
	  try {
        resp.setContentType("text/plain; charset=utf-8");
		resp.setCharacterEncoding("UTF-8");
		OutputStream ostream = resp.getOutputStream();
		ostream.write("hello".getBytes("UTF-8"));
		ostream.flush();
	  } catch (Exception e) {
		throw new ServletException(e.getMessage(),e);
	  }
	}
}
