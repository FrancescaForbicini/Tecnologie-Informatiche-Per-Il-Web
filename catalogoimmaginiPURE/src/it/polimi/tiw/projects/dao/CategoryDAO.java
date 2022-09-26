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
	 * Finds the primary categories : those that don't have a father.
	 * @return ArrayList with primary categories
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
	 * Finds the sub categories of a category
	 * @param father : the category of which we wants subCategories
	 * @return ArrayList of the sub categories (children) of the father
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
	 * Finds all subCategories of a specific father (identified by ID)
	 * @param ID:  the ID of the father 
	 * @return ArrayList with all the children of the father (all the category's subCategories) 
	 * @throws SQLException
	 */
	public ArrayList <Category> findCategoryBranch(int ID,ArrayList <Category> child) throws SQLException {
		String query = "SELECT * FROM category WHERE father = ? ORDER BY ID ASC ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1,ID);
			try (ResultSet result = pstatement.executeQuery();){
				while (result.next()) {
					ArrayList<Category> subCategory= new ArrayList<>();
					Category category = new Category();
					category.setID(result.getInt("ID"));
					category.setcategoryName(result.getString("name"));
					category.setIsSelected(result.getInt("selected"));
					category.setIsDestination(result.getInt("destination"));
					category.setprimaryCategory(false);
					subCategory = findSubCategories(category);
					category.setsubCategory(subCategory);
					child.add(category);
					child.addAll(subCategory);					
					for (int i = 0; i < subCategory.size(); i++)
						if (findSubCategories(subCategory.get(i))!= null) {
							subCategory.get(i).setsubCategory(findCategoryBranch(subCategory.get(i).getID(),child));
						}
				}			
			}
		}
		return child;
	}

	/**
	 * Finds the sub-categories of a primary category
	 * @param fathers the category 
	 * @throws SQLException
	 */
	public void findAllChildren(ArrayList <Category> fathers) throws SQLException {
		for (Category f : fathers) {
			f.setsubCategory(findSubCategories(f));
		}
	}
	
	
	/**
	 * Finds all categories
	 * @return ArrayList with the categories in the database
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
	 * Finds the first available ID for a primary category 
	 * @return the ID of the new primary category
	 * @throws SQLException
	 */
	public int newFatherID () throws SQLException{
		ArrayList<Category> primaryCategory= new ArrayList<>();
		try{
			primaryCategory= findPrimaryCategories();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		if(primaryCategory.isEmpty()) {
			return 1;
		}
		else {
			Category lastFather= primaryCategory.get(primaryCategory.size()-1);
			return lastFather.getID()+1;
		}
	}
	
	/**
	 * Inserts a new category in the database
	 * @param name the name of the new category
	 * @param father the father (primary category) of the new category, it could be null if the new category is a primary category
	 * @throws SQLException
	 */
	public void insertNewCategory(String name, int father) throws SQLException, Exception{
		String query = "INSERT into category(ID, name, father, selected,destination) VALUES (?, ? , ?, 0, 0)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			for (Category category : findAll()) {
				if (category.getcategoryName().equalsIgnoreCase(name)) {
					throw new Exception();
				}
			}
			pstatement.setString(2, name);		
			if(father!=0) {
				pstatement.setInt(1, newChildID(father));
				pstatement.setInt(3	, father);
				}
				else {
					pstatement.setNull(3, Types.INTEGER);
					pstatement.setInt(1, newFatherID());	
				}
				pstatement.executeUpdate();
		}
	}
	
	/**
	 * Identify the categories that has been selected to be moved
	 * @param ID the ID of the category that has been selected
	 * @throws SQLException
	 */
	public void setSelected(int ID) throws SQLException {
		String query= "UPDATE category SET selected = 1 WHERE ID = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1,ID);
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * Sets the category that can be the destination of the categories that will be moved
	 * @param ID the ID of the destination
	 * @throws SQLException
	 */
	public void setDestination (int ID) throws SQLException{
		String query= "UPDATE category SET destination = 1 WHERE ID != ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1,ID);
			pstatement.executeUpdate();
		}
	}
	

	/**
	 * Moves a child under a new father (identified by ID)
	 * @param ID the destination of the category that has to be moved
	 * @throws SQLException
	 */
	public void moveCategory(int ID) throws SQLException{
		Category categorySelected = getSelectedCategory();
		int newChildID = newChildID(ID);
		String query = "UPDATE category SET ID = ?,father = ? WHERE selected = 1 and destination = 0 ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, newChildID);
			pstatement.setInt(2, ID);
			pstatement.executeUpdate();
		}
		ArrayList<Category> childCategory= findSubCategories(categorySelected);
		if(!childCategory.isEmpty()) {
			moveSubCategory(childCategory, newChildID);
		}
		updateDatabase();
	}
	
	/**
	 * Moves all child of a father to the new position of their father
	 * @param childCategory the categories to move
	 * @param newIDFather the new ID of the father
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
	 * Gets a category from its ID
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
					category.setID(result.getInt("ID"));
					category.setcategoryName(result.getString("name"));
					category.setIsSelected(result.getInt("selected"));
					category.setIsDestination(result.getInt("destination"));
					category.setsubCategory(findSubCategories(category));
				}
			}
		}
		return category;
	}
		
	/**
	 * Gets the category that has been selected (the one that has to be moved)
	 * @return the categories that has been selected
	 * @throws SQLException
	 */
	public Category getSelectedCategory() throws SQLException {
		String query = "SELECT ID, father FROM category WHERE selected = 1 AND destination = 0";
		Category categorySelected = new Category();
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			try(ResultSet result = pstatement.executeQuery();){
				result.next();
				categorySelected.setID(result.getInt("ID"));
				return categorySelected;
			}
		}
	}
	
	

	/**
	 * Updates the database: every categories' "selected" and "destination" attributes return to 0
	 * @throws SQLException
	 */
	public void updateDatabase() throws SQLException{
		String query= "UPDATE category SET selected=0, destination=0";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.executeUpdate();
		}		
	}
	
	
}