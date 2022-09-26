package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.projects.dao.CategoryDAO;

/**
 * Servlet implementation class InsertCategory
 */
@WebServlet("/InsertCategory")
public class InsertCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertCategory() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = null;
		int father=0;
		boolean badRequest = false;
		try {
			name = request.getParameter("name");
			if(!request.getParameter("categoryId").isEmpty())
				father = Integer.parseInt(request.getParameter("categoryId"));
			if (name.isEmpty()) {
				badRequest = true;
			}
		}catch(NullPointerException e) {
			badRequest = true;
		}
		if (badRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameters");
		   return;
		}
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		try {
			if (father != 0) {
				if (categoryDAO.getCategoryFromID(father).getsubCategory().size() == 9) {
					badRequest = true;
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
								"This category has already 9 subcategories, so you can't insert another category here");
				}
			}
			else
				if (categoryDAO.findPrimaryCategories().size() < 9) {
					try {
						connection.setAutoCommit(false);
						categoryDAO.insertNewCategory(name,father);
					}catch(SQLException e) {
						badRequest = true;
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
									"Error in creating the category in the database");
						return;
					}catch(Exception io) {
						badRequest = true;
						response.sendError(HttpServletResponse.SC_BAD_REQUEST,
								"Category is already inserted");
						return;
					}
				}else {
					badRequest = true;
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					     "There are 9 primary categories, you can't add another one.");
				}
		} catch (SQLException e) {
			badRequest = true;
		    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		     "Error in creating the category in the database");
		}
		
		if (!badRequest) {
			try {
				connection.commit();
				String ctxpath = getServletContext().getContextPath();
				String path = ctxpath + "/Home";
				response.sendRedirect(path);
			} catch (SQLException e) {
				badRequest = true;
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					     "Error in creating the category in the database");
			}
		}
		else {
			try {
				connection.rollback();
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					     "Error in creating the category in the database");
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
