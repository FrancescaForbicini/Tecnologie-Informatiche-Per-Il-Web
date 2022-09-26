package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import it.polimi.tiw.projects.beans.Category;

public class CategoryDAO {
	Connection connection;
	
	public CategoryDAO (Connection connection) {
		this.connection = connection; 
	}
	
	
	/**
	 * Finds the primary category : those that don't have a father
	 * @return the primary category
	 * @throws SQLException
	 */
	public ArrayList<Category> findPrimaryCategories() throws SQLException {
		ArrayList<Category> primaryCategory = new ArrayList<>();
		String query = "SELECT ID,name, selected ,destination FROM category WHERE father IS NULL ORDER BY ID ASC";
	
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			try (ResultSet result = pstatement.executeQuery();){
				while (result.next()) {
					Category category = new Category();
					category.setID(result.getInt("ID"));
					category.setcategoryName(result.getString("name"));
					category.setprimaryCategory(true);
					category.setIsSelected(result.getInt("selected"));
					category.setIsDestination(result.getInt("destination"));
					primaryCategory.add(category);
				}
			}
		}
		return primaryCategory;
	}
	
	/**
	 * Finds the sub categories  of a category
	 * @param father : the category that has sub categories (children)
	 * @return the sub categories (children) of the father
	 * @throws SQLException
	 */
	public ArrayList <Category> findSubCategories(Category father) throws SQLException {
		ArrayList <Category> child = new ArrayList<>();
		String query = "SELECT * FROM category WHERE father = ? ORDER BY ID ASC ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1,father.getID());
			try (ResultSet result = pstatement.executeQuery();){
				while (result.next()) {
					Category category = new Category();
					category.setID(result.getInt("ID"));
					category.setcategoryName(result.getString("name"));
					category.setIsSelected(result.getInt("selected"));
					category.setIsDestination(result.getInt("destination"));
					category.setprimaryCategory(false);
					category.setsubCategory(findSubCategories(category));
					child.add(category);
				}			
			}
		}
		return child;
	}
	


	/**
	 * Finds the sub-categories of a primary category
	 * @param fathers the primary category 
	 * @throws SQLException
	 */
	public void findAllChildren(ArrayList <Category> fathers) throws SQLException {
		for (Category f : fathers) {
			f.setsubCategory(findSubCategories(f));
		}
	}
	
	
	/**
	 * Finds all categories
	 * @return the categories in the database
	 * @throws SQLException
	 */
	public ArrayList <Category> findAll() throws SQLException{
		ArrayList <Category> allCategory = new ArrayList<>();
		String query = "SELECT * FROM category";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			try (ResultSet result = pstatement.executeQuery();){
				while (result.next()) {
					Category category = new Category();
					category.setID(result.getInt("ID"));
					category.setcategoryName(result.getString("name"));
					category.setIsSelected(result.getInt("selected"));
					category.setIsDestination(result.getInt("destination"));
					category.setsubCategory(findSubCategories(category));
					allCategory.add(category);
				}			
			}
		}
		return allCategory;
	}
	
	/**
	 * Sets the ID of a sub-category corresponded to the ID of the father
	 * @param IDfather ID of the primary category
	 * @return the new ID of the sub-category of the primary category
	 * @throws SQLException
	 */
	public int newChildID(int IDfather) throws SQLException {
		Category father= new Category();
		Category lastChild = new Category(); 
		ArrayList<Category> subCategories = new ArrayList();
		father.setID(IDfather);
		try{
			subCategories = findSubCategories(father);
		} catch(SQLException e ) {
			e.printStackTrace();
		}
		if(subCategories.isEmpty()) {
			return (IDfather*10+1);
		} else {	
			lastChild= subCategories.get(subCategories.size()-1);
			return lastChild.getID()+1;
		}
		
	}
	
	/**
	 * Sets the ID of the primary category to a new ID
	 * 
	 * @return the new ID of the primary category
	 * @throws SQLException
	 */
	public int newFatherID() throws Exception {
		ArrayList<Category> primaryCategory = new ArrayList<>();
		try {
			primaryCategory = findPrimaryCategories();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (primaryCategory.isEmpty()) {
			return 1;
		} else {
			int ID = 1;
			for (Category category : primaryCategory)
				if (category.getID() == ID)
					ID++;
				else
					return ID;
			if (ID >= 10 )
				throw new Exception();
			return ID;
		}
	}
	
	/**
	 * Inserts a new category in the database
	 * 
	 * @param name   the name of the new category
	 * @param father the father (primary category) of the new category, it could be
	 *               null
	 * @throws SQLException
	 */
	public void insertNewCategory(String name, int father) throws SQLException, Exception {
		boolean checkFather = false;
		if (father < 0)
			throw new Exception();
		for (Category category : findAll()) {
			if (category.getcategoryName().equalsIgnoreCase(name)) 		
				throw new Exception();
			if (father == category.getID())
				checkFather = true;
		}
		if (checkFather)
			if (this.getCategoryFromID(father).getsubCategory().size() == 9)
				throw new Exception();
		int ID = 0;
		if (father != 0 && checkFather) {
			ID = newChildID(father);
		} else {
			ID = 0;
			if (!checkFather && father != 0 && (father < 10 && father > 0))
				ID = father;
			else {
				try {
					ID = newFatherID();
				} catch (Exception e) {}
			}
		}
		String query = "INSERT into category(ID, name, father, selected,destination) VALUES (?, ? , ?, 0, 0)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, ID);
			pstatement.setString(2, name);
			if (father == 0 || !checkFather)
				pstatement.setNull(3, Types.INTEGER);
			else
				pstatement.setInt(3, father);
			pstatement.executeUpdate();
		}
	}
	
	
	
	/**
	 * Moves a category to another
	 * @param ID of the category that has to be moved
	 * @param ID of the category where the other category has to be moved
	 * @throws SQLException
	 */
	public void moveCategory(int IDToMove,int IDDestination) throws SQLException{
		Category categoryToMove = this.getCategoryFromID(IDToMove);
		int newChildID = newChildID(IDDestination);
		String query = "UPDATE category SET ID = ?,father = ? WHERE ID = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, newChildID);
			pstatement.setInt(2, IDDestination);
			pstatement.setInt(3, IDToMove);
			pstatement.executeUpdate();
		}
		ArrayList<Category> childCategory= findSubCategories(categoryToMove);
		if(!childCategory.isEmpty()) {
			moveSubCategory(childCategory, newChildID);
		}
	}
	
	/**
	 * Moves all child of a father to the new position of their father
	 * @param childCategory the categories to move
	 * @param newIDFather the category of destination
	 * @throws SQLException
	 */
	public void moveSubCategory(ArrayList<Category> childCategory, int newIDFather) throws SQLException {
		int newIDChild;
		for(Category child: childCategory) {
			newIDChild = newChildID(newIDFather);
			String query="UPDATE category SET ID = ?,father = ? WHERE ID=? ";
			try (PreparedStatement pstatement = connection.prepareStatement(query);){
				pstatement.setInt(1, newIDChild);
				pstatement.setInt(2, newIDFather);
				pstatement.setInt(3, child.getID());
				ArrayList <Category> childCategories= findSubCategories(child);
				if(!childCategories.isEmpty()) {
					moveSubCategory(childCategories, newIDChild);
				}
				pstatement.executeUpdate();
			}	
		}
	}
	
	/**
	 * Gets a category
	 * @param ID the ID of the category
	 * @return the category that corresponds to the ID 
	 * @throws SQLException
	 */
	public Category getCategoryFromID(int ID) throws SQLException {
		Category category= new Category();
		String query = "SELECT * FROM category WHERE ID = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1,  ID);
			try (ResultSet result = pstatement.executeQuery();){
				while (result.next()) {
				category.setID(ID);
				category.setcategoryName(result.getString("name"));
				category.setIsSelected(result.getInt("selected"));
				category.setIsDestination(result.getInt("destination"));
				category.setsubCategory(findSubCategories(category));
				}
			}
		}
		return category;
	}
}