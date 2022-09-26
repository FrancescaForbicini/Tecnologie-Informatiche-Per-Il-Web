package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.polimi.tiw.projects.beans.User;

public class UserDAO{
	private Connection connection;
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Checks if the user is authorized to access to the database
	 * @param username the username to verify
	 * @param password the own password to enter in the catalog
	 * @return the user correct to enter in the database
	 * @throws SQLException
	 */
	public User checkUser(String username, String password) throws SQLException{
		String query = "SELECT username FROM user WHERE username = ? AND password = ? ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1,username);
			pstatement.setString(2,password);
			try (ResultSet result = pstatement.executeQuery();){
					result.next();
					User user = new User();
					user.setUsername(result.getString("username"));
					return user;
			}
		}
		
	}
	
}