package it.polimi.tiw.projects.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;


public class ConnectionHandler{
	public static Connection getConnection(ServletContext context) throws  UnavailableException {
		Connection connection = null;
		try {
			//Parametri necessari per connettersi al db presi dal xml
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			// posso collegarmi per i requisiti 
			connection = DriverManager.getConnection(url,user,password);
		}catch(ClassNotFoundException e) {
			//lanciata da Class.forName se non trova nome del db
			throw new UnavailableException("Can't load database driver");
		}catch (SQLException e) {
			//lanciata da DriverManager se non riesce a connettersi al db
			throw new UnavailableException("Couldn't get a db connection");
		}
		return connection;
	}
	
	public static void closeConnection (Connection connection) throws SQLException{
		if (connection != null) {
			connection.close();
		}
	}
}