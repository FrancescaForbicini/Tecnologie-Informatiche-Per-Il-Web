package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.dao.CategoryDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

/**
 * Servlet implementation class InsertCategory
 * 
 */
@WebServlet("/InsertCategory")
@MultipartConfig

public class InsertCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertCategory() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
    

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = null;
		int fatherID=0;
		boolean badRequest = false;
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("Name"));
			if(!request.getParameter("FatherID").isEmpty())
				fatherID = Integer.parseInt(request.getParameter("FatherID"));
			if (name.isEmpty()) {
				badRequest = true;
			}
		}catch(NullPointerException e) {
			badRequest = true;
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		String message = null;
		try {
			if (fatherID == 0 && categoryDAO.findPrimaryCategories().size() == 9)
				badRequest = true;
			else {
				connection.setAutoCommit(false);
				categoryDAO.insertNewCategory(name,fatherID);
			}
		} catch (SQLException e) {
			badRequest = true;
			message = "Error in the database";
		} catch (Exception e) {
			badRequest = true;
			message = "Category is already inserted or the ID is not valid";
		}
		if (!badRequest) {
			try {
				connection.commit();
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			} catch (SQLException e) {
				badRequest = true;
				message = "Error";
			}
		}
		if (badRequest) {
			try {
				connection.rollback();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println(message);
				return;
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Error in rollback");
				return;
			}

		}
	}
	
	@Override
	 public void destroy() {
		if (connection != null) {
			try {
				connection.close();
			}catch (SQLException e){
			}
		}
	 }
}
