package net.tarilabs.mpes.facebook;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/FacebookSignIn")
public class FacebookSignIn extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO change to constant and log if property not found.
		String client_id = System.getProperty("net.tarilabs.mpes.facebook.client_id");
		response.sendRedirect("https://graph.facebook.com/oauth/authorize?client_id="
				+client_id
				+"&redirect_uri=http://localhost:8080/mpes-core/FacebookCallback&scope=publish_stream");
	}

}
