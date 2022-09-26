package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckLogin")

public class CheckLogin extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection dbconnection = null;
	private TemplateEngine templateEngine;
	
	public CheckLogin() {
		super();
	}
	public void init() throws ServletException{
		dbconnection = ConnectionHandler.getConnection(getServletContext());
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");

	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = null;
		String password = null;
		String path;
		
		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
				throw new Exception ("Missing or empty credential value");
			}
		}catch(Exception e) {
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing Credential Value");
			return;
		}
		UserDAO userDAO = new UserDAO(dbconnection);
		User user = null;
		try {
			user = userDAO.checkUser(username,password);
		}catch (SQLException e ) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"You are not authorized");
			return;
		}
		if (user == null) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
			ctx.setVariable("errorMsg","Username o Password sbagliati");
			path = "/index.html";
			templateEngine.process(path,ctx,response.getWriter());
		}
		else {
			request.getSession().setAttribute("username",user);
			path = getServletContext().getContextPath() + "/Home";
			response.sendRedirect(path);
		}
		
	}
	
	public void destroy	(){
		try {
			ConnectionHandler.closeConnection(dbconnection);
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
