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
 * Servlet implementation class MoveCategoryHere
 */
@WebServlet("/MoveCategoryHere")
public class MoveCategoryHere extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MoveCategoryHere() {
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
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameters");
			return;
		}
		CategoryDAO categoryDAO = new CategoryDAO(connection);
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
				if (categoryDAO.getCategoryFromID(ID).getsubCategory().size() == 9) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can't insert another category here, because it is already had 9 subcategories");
					badRequest = true;
				}
				else {
					connection.setAutoCommit(false);
					categoryDAO.moveCategory(ID);
				}
			} catch (SQLException e) {
				badRequest = true;
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in the database");
			}
			}
			if (!badRequest) {
				try {
					connection.commit();
					connection.setAutoCommit(true);
					categoryDAO.updateDatabase();
					String ctxpath = getServletContext().getContextPath();
					String path = ctxpath + "/Home";
					response.sendRedirect(path);
				} catch (SQLException e) {
					badRequest = true;
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in the database");
				}
				
			}
			if (badRequest) {
				try {
				connection.rollback();
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in the database");
					return;
				}
			}		
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}
