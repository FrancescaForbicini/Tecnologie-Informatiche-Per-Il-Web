package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.projects.beans.Category;
import it.polimi.tiw.projects.dao.CategoryDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

/**
 * Servlet implementation class Home
 */
@WebServlet("/Home")
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;
    public Home() {
        super();
    }

	public void init() throws ServletException{
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix("html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("username") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		ArrayList<Category> primaryCategory = new ArrayList<>();
		ArrayList<Category> allCategory = new ArrayList<>();
		try{
			primaryCategory = categoryDAO.findPrimaryCategories();
			categoryDAO.findAllChildren(primaryCategory);
			allCategory= categoryDAO.findAll();
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"INTERNAL ERROR");
			e.printStackTrace();
		}
		String path = "/WEB-INF/Home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request,response,servletContext,request.getLocale());
		ctx.setVariable("primaryCategory",primaryCategory);
		ctx.setVariable("allCategory", allCategory);
		templateEngine.process(path,ctx,response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	


}
