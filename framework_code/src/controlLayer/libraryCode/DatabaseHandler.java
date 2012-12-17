package controlLayer.libraryCode;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.Statement;

public class DatabaseHandler {

    
	Connection conn = null;
	
	public void startDB() {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
      
			conn = DriverManager.getConnection("jdbc:mysql://dbserver.in.cs.ucy.ac.cy:3306/socialdevices",
					"akamil01", "de%7t2");

			if(!conn.isClosed())
				System.out.println("Successfully connected to " +"MySQL server using TCP/IP...");

		} catch(Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}
  
	public void executeQuery(String query){
		
		Statement s;
		try {
			s = (Statement) conn.createStatement();
			
			s.executeQuery(query);

			s.close ();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void executeUpdate(String query){
		Statement s;
		try {
			s = (Statement) conn.createStatement();
			
			s.executeUpdate(query);

			s.close ();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// check for eventing
		checkForEventing(query);
	}
	
	// check for eventing on the Facebook Application
	public void checkForEventing(String query){
		// check for new Measurements
		if(query.contains("Measurement")){
			System.err.println("Checking for events in DB...");
			// new Measurement in Database: Check for eventing
			try {
				String facebookURL = "http://apps.facebook.com/myhomedevices/code/check_eventing.php";
				URL url = new URL(facebookURL);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				
			} catch (Exception e) {
				System.err.println("Failed to run php script");
			}
		}
	}
	
	public void stopDB(){
		try {
			if(conn != null)
				conn.close();
		} catch(SQLException e) {}
	}
}