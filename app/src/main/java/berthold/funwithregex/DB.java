package berthold.funwithregex;

/**
 * DB Class
 * 
 * This class handled all SQL- requests CreateDB
 * 
 * @author Berthold Fritz 2016
 * @param args -
 *
 */

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
	
	/**
	 * Read database
	 * Opens an existig DB if it exists or creates a new one if not
	 * 
	 * @param	DB_DRIVER		Name of database driver
	 * @param	DB_CONNECTION	DB- object
	 * @param	DB_USER			User id
	 * @param	DB_PASSWORD		Password
	 * @return	The DB- object 
	 * 
	 */
	
	public static Connection read (	String DB_DRIVER,
									String DB_CONNECTION,
									String DB_USER,
									String DB_PASSWORD) throws Exception 
	{
					
		Class.forName(DB_DRIVER); 
		Connection conn = DriverManager.
					
		// Open DB. If not present, create one
		// Parameters: Driver name (jdbc:h2 + Pathname + username + pwd)
		
		getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD); 
		conn.setAutoCommit(true);
		return (conn);
	}
	
	/**
	 * Handle SQL- request
	 * Returns the result(s) of an SQL- request as an one dimensional String array.
	 * Each row contains a matching result, fields are comma separated.
	 * 
	 * @param	sqlString	Sql- statement
	 * @param	conn		DB- object
	 * @return	"error" if the request had no result or the request could not be
	 * 			processed (e.g. syntax error in sql- statement)
	 *
	 * 			"empty" if no matching pattern could be found
	 * 
	 */
	
	public static StringBuffer sqlRequest(String sqlString,Connection conn)
	{
		StringBuffer result=new StringBuffer();

		try {
			PreparedStatement selectPreparedStatement =null;
			selectPreparedStatement = conn.prepareStatement(sqlString);
			ResultSet rs = selectPreparedStatement.executeQuery();
			
			ResultSetMetaData md = rs.getMetaData();
			int maxcol=md.getColumnCount();
			
			// Get all columns and return them in an string buffer
			if(rs.isBeforeFirst()) {		// Check if result is empty
				while (rs.next()) {
					for (int coll = 1; coll <= maxcol; coll++) {
						if (coll != maxcol)
							result.append(rs.getString(coll) + ","); // Column are comma separated
						else result.append(rs.getString(coll));
					}
					result.append("#");    // This is the end of the row
				}
			} else result.append("empty");	// There was no first result, therefore there was no rs.beforeFirst
		}
		catch (SQLException e)
		{
			result.append("error");
		}
		return result;
	}

	/**
	 * Insert a String into the database
	 *
	 * @param 	sql			SQL- Statement
	 * @param 	conn		DB- connection
	 *
	 */

	public static void insert(String sql,Connection conn)
	{
		try {
			Statement stmt = null;

			stmt = conn.createStatement();
			stmt.executeUpdate(sql);

		} catch (SQLException se) {
			Log.d ("######", "Error while creating table");
			Log.d("######",se.toString());
			return;
		}
		return;
	}
	
	/**
	 *  Close database
	 */
	
	public static void close(Connection conn) throws Exception
	{
		conn.close();
	}

	/**
	 * Get # of rows of the given table
	 *
	 * @param table	Table
	 * @param conn 	DB connection
	 */

	public static int getNumberOfRows (String table,Connection conn)
	{
		int numberOfRows;


		StringBuffer r= DB.sqlRequest("select Count(*) from "+table, conn);

		try {
			numberOfRows = Integer.parseInt(r.toString().replace("#", "").trim());
		} catch (Exception e){
			numberOfRows=0;
		}
		return numberOfRows;
	}

	/**
	 * Check if an entry already exists (case sensitive)
	 *
	 * @param 	table  Name of table
	 * @param 	column Name of column to be checked
	 * @param 	value  Value of column
	 * @return  true if the value is already a member of the DB
	 *
	 * todo: Add case insensitive
	 */

	public static Boolean doesExist(String table,String column,String value,Connection conn)
	{
		StringBuffer rs=DB.sqlRequest("select "+column+" from "+table+" where "+column+"='"+value+"'",conn);

		// Value of column was found
		if (rs.toString().replace("#","").trim().equals(value)) return true;

		// Value of collumn could not be found
		return false;
	}


	/**
	 * Get key 1 Value
	 *
	 * @param 	table  Name of table
	 * @param 	value  Value of column
	 */

	public static int getKey1(String table,String column,String value,Connection conn) {
		StringBuffer rs = DB.sqlRequest("select key1 from " + table + " where " + column + "='" + value + "'", conn);
		String r = rs.toString().replace("#", "").trim();
		System.out.println("------+++++++++++++++getkey1:" + value);
		try {
			int key1 = Integer.parseInt(r);
			return key1;
		} catch (Exception e) {
			return 0;
		}
	}

}
