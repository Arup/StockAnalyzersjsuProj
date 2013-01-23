package junit;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bean.StockMonitorBean;

public class JUnitTestStockAlert {

	Connection con = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/cmpe275", "root", "password");

			if (!con.isClosed())
				System.out.println("successfully connected to "
						+ "MySQL server using TCP/IP...");

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void databaseconnection()

	{

		Statement stmt;
		try {
			stmt = con.createStatement();
			String ChangeQuery = "select * from stockactivity where percentchange > 5 ORDER BY DESC LIMIT 0,5 ";
			ResultSet PercentchngRS = stmt.executeQuery(ChangeQuery);

			// retriving the data from result set
			int i = 0;
			while (PercentchngRS.next() && i < 1) {
				System.out.println(PercentchngRS.getString("symbol"));
				assertEquals("They are equal", "Ab",
						PercentchngRS.getString("symbol"));

				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Query to retrive fields from database where %change > 5

	}

	@Test
	public void processrequestanalysis() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		StockMonitorBean smb = new StockMonitorBean();
		String s = "Please wait and check back later ..";
		String sm;
		// Please edit according to user input
		sm = smb.processClientRequest("analysis");
		assertEquals("Both have same results", s, sm);

	}

	@Test
	public void processrequestconsultancy() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		StockMonitorBean smb = new StockMonitorBean();
		String s = "Please wait and check back later ..";
		String sm;
		// Please edit according to user input
		sm = smb.processClientRequest("consultancy");
		assertEquals("Both have same results", s, sm);

	}

	@Test
	public void processbadrequest() {

		StockMonitorBean smb = new StockMonitorBean();

		String badrequest = "Bad Request! \n Request Format : <client-req-ID> <function:analysis/consult> <stock-symbol> <from-time> <to-time> ";
		String smi = null;
		try {
			smi = smb.processClientRequest("");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("BAd Format", badrequest, smi);

	}

}
