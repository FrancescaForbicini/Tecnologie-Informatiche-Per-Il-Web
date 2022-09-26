package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.projects.beans.Category;
import it.polimi.tiw.projects.dao.CategoryDAO;

/**
 * Servlet implementation class MoveCategory
 */
@WebServlet("/MoveCategory")
public class MoveCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MoveCategory() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
    	  try {
    	   ServletContext context = getServletContext();
    	   String driver = context.getInitParameter("dbDriver");
    	   String url = context.getInitParameter("dbUrl");
    	   String user = context.getInitParameter("dbUser");
    	   String password = context.getInitParameter("dbPassword");
    	   Class.forName(driver);
    	   connection = DriverManager.getConnection(url, user, password);
    	  } catch (ClassNotFoundException e) {
    	   e.printStackTrace();
    	   throw new UnavailableException("Can't load database driver");
    	  } catch (SQLException e) {
    	   e.printStackTrace();
    	   throw new UnavailableException("Couldn't get db connection");
    	  }
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int ID = 0;
		boolean badRequest = false;
		try {
			ID = Integer.parseInt(request.getParameter("ID"));
		}catch(NullPointerException e) {
			badRequest = true;
		}
		if (badRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameters");
		   return;
		}
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		ArrayList <Category> child = new ArrayList();
		ArrayList <Category> allCategory = new ArrayList();
		try {
			allCategory = categoryDAO.findAll();
		} catch (SQLException e1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error in download all categories");
			return;
		}
		boolean checkID = false;
		for (int i = 0; i < allCategory.size(); i++) {
			if (allCategory.get(i).getID() == ID) {
				checkID = true;
				break;
			}
		}
		if (!checkID) {
			badRequest = true;
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "This ID doesn't exist anymore");
			return;
		}
		else {
			try {
				connection.setAutoCommit(false);
				categoryDAO.setSelected(ID);
				child = categoryDAO.findCategoryBranch(ID,child);
				for (Category category : child) {
					categoryDAO.setSelected(category.getID());
				}
				if (!categoryDAO.getCategoryFromID(ID).getprimaryCategory()) {
					categoryDAO.setSelected(ID/10);
				}
				categoryDAO.setDestination(ID);
			} catch (SQLException e) {
				badRequest = true;
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error in the database");
			}
		}
		if (!badRequest) {
			try {
				connection.commit();
			} catch (SQLException e) {
				badRequest = true;
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error in the commit");
			}
			String ctxpath = getServletContext().getContextPath();
			String path = ctxpath + "/Home";
			response.sendRedirect(path);
		}
		if (badRequest)
			try {
				connection.rollback();
				System.out.print("ciao");
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error");
			}
		}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}
