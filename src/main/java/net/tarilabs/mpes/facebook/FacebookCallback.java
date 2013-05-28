package net.tarilabs.mpes.facebook;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/FacebookCallback")
public class FacebookCallback extends HttpServlet {
	
	@EJB
	private FacebookConnMgr fbConnMgr;
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(FacebookCallback.class);
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO change to constant and log if property not found.
		String client_id = System.getProperty("net.tarilabs.mpes.facebook.client_id");
		String client_secret = System.getProperty("net.tarilabs.mpes.facebook.client_secret");
		String code = request.getParameter("code");
		logger.info("code: " + code );

		String res = 
				Request.Get("https://graph.facebook.com/oauth/access_token?"
						+"client_id="+client_id+"&"
						+"redirect_uri=http://localhost:8080/mpes-core/FacebookCallback&"
						+"client_secret="+client_secret+"&"
						+"code="+code).execute().returnContent().toString();

		String left = res.substring(res.indexOf("=")+1, res.length());
		String access_token = left.substring(0, left.indexOf("&"));
		logger.info(access_token);
		fbConnMgr.setAccess_token(access_token);
		response.sendRedirect(request.getContextPath()+"/index.xhtml");

	}

}
