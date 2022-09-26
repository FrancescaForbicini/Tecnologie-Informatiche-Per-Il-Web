package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;


import javax.servlet.ServletException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.gson.Gson;


import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.beans.Category;
import it.polimi.tiw.projects.dao.CategoryDAO;

/**
 * Servlet implementation class MoveCategory
 */
@WebServlet("/SaveCategory")
@MultipartConfig
public class SaveCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveCategory() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Gson gson = new Gson();
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		CategoryChanged categoryChanged = gson.fromJson(new InputStreamReader(request.getInputStream()), CategoryChanged.class);
		boolean badRequest = false;
		if (categoryChanged == null || badRequest)
			badRequest = true;
		else {
			int [] categoriesPicked = categoryChanged.getCategoryPick();
			int [] categoriesDestination = categoryChanged.getCategoryDestination();
			Category categoryToMove = new Category();
			Category categoryDestination = new Category();
			for (int i = 0; i < categoryChanged.getCategoryPick().length ; i++) {
				try {
					categoryToMove = categoryDAO.getCategoryFromID(categoriesPicked[i]);
					categoryDestination = categoryDAO.getCategoryFromID (categoriesDestination[i]);
					if (categoryToMove.getID() == 0 || categoryDestination.getID() == 0 )
						badRequest = true;
					else
						if (categoryDestination.getsubCategory().size() == 9)
							badRequest = true;
				} catch (SQLException e) {
					badRequest = true;
				}
				
				if (categoryToMove.getsubCategory().contains(categoryDestination))
					badRequest = true;
				else {
						try {
							connection.setAutoCommit(false);
							categoryDAO.moveCategory(categoriesPicked[i], categoriesDestination[i]);
						} catch (SQLException e) {
							try {
								connection.rollback();
							} catch (SQLException e1) {
								badRequest = true;
							}
						}
					}
			}
		}
		if(!badRequest) {
			try {
				connection.commit();	
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Error in database");

				return;
			}
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("It is not possible to move categories");
			return;
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

