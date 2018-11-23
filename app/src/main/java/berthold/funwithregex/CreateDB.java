package berthold.funwithregex;

/**
 * Create.
 * 
 * This creates a new, empty, database if no database of the same
 * name already exists.
 * 
 * @author Berthold Fritz 2016
 *
 */

import android.util.Log;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDB {
	
	// Const.

	public static final int ERROR=0;				// Index in 'result' for 'error' text
	public static final int ERROR_DESCRIPTION=1;	// Index in 'result' for description of error

	public static void make (String name) throws Exception
	{
		
		// DB- Conection
		
		String DB_DRIVER		=	"org.h2.Driver";
		String DB_CONNECTION	=	"jdbc:h2:";
		String DB_USER			=	"";
		String DB_PASSWORD		=	"";
	
		// Create database
		
		Connection conn;
		
		try
		{
			conn=DB.read(DB_DRIVER, DB_CONNECTION+name, DB_USER, DB_PASSWORD);
		}
		catch (SQLException se)
		{
			System.out.println("Error while creating database:"+DB_CONNECTION);
			System.out.println(se);
			return;
		}
		
		try
		{
			Statement stmt=null;
			
			stmt=conn.createStatement();

			// Games table

			stmt.executeUpdate 	("create table"
								+ "	regex"
								+ " (key1 identity,"
								+ "regexstring char(255),"
								+ "description char(255),"
								+ "date datetime,"
								+ "rating int,"
								+ "samplepath char(255))");

			// Create sample entry's

			stmt.executeUpdate("insert into regex "
					+ "(regexstring,description,rating) "
					+ "values ('^([\\w\\.\\-]+)@([\\w\\-]+)((\\.(\\w){2,3})+)$','E- Mail.....',"+4+")");
			
			stmt.executeUpdate("insert into regex "
					+ "(regexstring,description) "
					+ "values ('[0-9]','Alle Zahlen von 1 bis 9.....')");

			stmt.executeUpdate("insert into regex "
					+ "(regexstring,description) "
					+ "values ('[a-z]','Alle Buchstaben....')");

			stmt.executeUpdate("insert into regex "
					+ "(regexstring,description) "
					+ "values ('[^a-z]','Alle Buchstaben, ausser a....')");

		}
		catch (SQLException se)
		{
				System.out.println("Error while creating table");
				System.out.println(se.toString());
				return;
		}
		
		
		// Close database
		
		DB.close(conn);
		Log.i ("---------","Closed connection");
	}
}
