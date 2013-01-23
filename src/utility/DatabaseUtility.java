package utility;

/*        
 * Project cmpe275
 * JDBCConnectionImpl.java - Makes JDBC connections and serves JDBC requests.
 * Written by - SR
 */


import java.util.*;
import java.sql.*;

//import com.mysql.jdbc.Connection;

public class DatabaseUtility {

	// database instance name, such as myOracleDB
	private String dbName;

	Connection conn; // the precious connection

	public DatabaseUtility (String dbName) // private constructor
	throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.dbName = dbName;
		// code for creating Oracle connection
		if (dbName == "cmpe275") {
			String userName = "root";
			String password = "password";
			String url = "jdbc:mysql://localhost:3306/cmpe275";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = (Connection)DriverManager.getConnection (url, userName, password);
			System.out.println ("Database connection established");
			return ;
		}
		// code for creating connections to other databases
		// ...

		// if database name is not found    
		throw new ClassNotFoundException();
		// return;
	}

	String getDatabaseName() {
		return dbName;
	}

	public ResultSet doQuery (String sqlString) 
	throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sqlString);   // for SELECT
		//stmt.close();
		return rs;
	}

	public void doUpdate (String sqlString ) 
	throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sqlString);  // for INSERT, DELETE or UPDATE
		//stmt.close();
	}
	
	public void closeRS (ResultSet rs) 
	throws SQLException {
		rs.close();
	}
	
	static class JDBCPool {

		// dictionary of database names with corresponding vector of connections
		private Hashtable poolDictionary = new Hashtable();

		// methods and attributes for Singleton pattern
		private JDBCPool() {} // private constructor
		private static JDBCPool _instance; // get class instance
		// Singleton getter utilizing Double Checked Locking pattern
		public static JDBCPool getInstance() {
			if (_instance == null) {
				synchronized(JDBCPool.class) {
					if (_instance == null)
						_instance = new JDBCPool();
				}
			}
			return _instance;
		}

		// get connection from pool
		public synchronized DatabaseUtility acquireImpl (String dbName) 
		throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {  
			// get pool matching database name
			Vector pool = (Vector)poolDictionary.get(dbName);
			if (pool != null) {
				int size = pool.size();
				if (size > 0) {
					DatabaseUtility impl = null;
					// retrieve existing unused connection 
					impl = (DatabaseUtility)pool.elementAt(size-1);
					// remove connection from pool
					pool.removeElementAt(size-1);
					// return connection
					return impl;
				}        
			} 
			// pool is empty so create new connection
			return new DatabaseUtility(dbName);
		}

		// return connection to pool
		public synchronized void releaseImpl (DatabaseUtility impl) {
			String dbName = impl.getDatabaseName();
			Vector pool = (Vector)poolDictionary.get(dbName);
			if (pool == null) {
				pool = new Vector();
				poolDictionary.put(dbName, pool);
			}
			pool.addElement(impl);
		}
	}
}



