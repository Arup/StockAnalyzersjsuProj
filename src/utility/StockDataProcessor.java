package utility;

//imports
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import bean.StockActivityEntity;
import bean.StockActivityEntityManager;
/**
 * Server class responsible for processing Client requests  
 *
 */
public class StockDataProcessor {

	/* Single Instance From our Application 	 */
	private StockDataProcessor()
	{
		// no code req'd
	}

	public static StockDataProcessor getInstance()
	{
		if (ref == null)
			// it's ok, we can call this constructor
			ref = new StockDataProcessor();		
		return ref;
	}

	public Object clone()
	throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException(); 
		// Only single instance allowed
	}

	private static StockDataProcessor ref;

	private String clientID = null;
	private String serviceName = null;

	public  void startProcessing(String request) {

		StockDataProcessor ref = StockDataProcessor.getInstance(); 
		//ref.detectStockAlerts();

		String reply = null; 
		//String splitArray[] = request.split(" ");
		String query = null;
		ResultSet rs = null; 

		utility.DatabaseUtility conn;
		try {
			conn = new utility.DatabaseUtility("cmpe275");
			query = "SELECT `clientID` , `serviceName`  FROM `clientrequest` WHERE `status` = 'pending' ORDER BY "
				+ "`clientID` LIMIT 0,1;";

			rs = conn.doQuery(query);
			if (rs.next()) {
				clientID = rs.getString("clientID"); 
				serviceName = rs.getString("serviceName");
				reply =  clientID + " : your request now is being processed"  ;   
				// chnage the status to in progress
				query = "UPDATE `ClientRequest` SET "
					+ "`status`="
					+  "'in progress' WHERE `clientID` = '"
					+  clientID + "';" ; 

				System.out.println("Executing query : " + query + "...");
				conn.doUpdate(query); 			

			}
			//conn.closeRS(rs); 			



			//Start all the processing
			// read from properties, if historic script has been run or not (may be till yesterday)
			Properties prop = new Properties(); 
			FileInputStream fis;
			DateFormat dateformat = new SimpleDateFormat("MM/dd/yyyy");
			Date curr_date = new Date();
			long difInDays = 0; 
			try {
				fis = new FileInputStream("stock_alert.properties");
				prop.load(fis);
				String hirstoric_prop = prop.getProperty("historic"); 
				Date historic_last_run_date = dateformat.parse(hirstoric_prop); 
				difInDays = (long) (curr_date.getTime() - historic_last_run_date.getTime())/(1000*60*60*24);
				System.out.println("Days since Historic Data load has been run is :" + difInDays); 

			}catch (Exception e) {
				System.out.println("Error in reading Historic Last Run Property"); 
				e.printStackTrace();
			} 

			if (difInDays >= 2)
			{
				ref.runPerlScriptHistoric(); 
				ref.processHistoricStockData();
				prop.setProperty("historic", dateformat.format(curr_date));
			}


			String folderPath = "realtime"; 
			ref.runPerlScriptRealTime(); 
			ref.processRealTimeStockData(folderPath);
			
			// calculate average volume of stocks treaded ... 
			ref.setAverageVolume(); 
			

			// read from client request for the type of service requested --> analysis / consultancy ?? 
			// prepare result accordingly -- Done inside method detectStockAlerts() 
			ref.detectStockAlerts(); 
			ref.generateGraphForStockAlerts(); 

			// set the status to completed ... 
			//conn = new utility.DatabaseUtility("cmpe275");
			query = "UPDATE `ClientRequest` SET "
				+ "`status`="
				+  "'completed' WHERE `clientID` = '"
				+  clientID + "';" ; 

			System.out.println("Executing query : " + query + "...");
			conn.doUpdate(query); 				
			conn.closeRS(rs); 				
			//Done :) 


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



	}

	public static void main(String[] args) {
		// testing ..
		StockDataProcessor ref = StockDataProcessor.getInstance(); 
		ref.startProcessing("");   
	}	

	public void runPerlScriptHistoric() {
		// runs the perl script to generate historic data
		try {

			String cmdString = "cmd.exe /c start perl stock_data.pl -h";

			Process p = Runtime.getRuntime().exec(cmdString);

			Robot robot = null;
			robot = new Robot();
			for (int i = 0; i < 8; i++) {
				robot.keyPress(KeyEvent.VK_ENTER);
				robot.keyPress(KeyEvent.VK_END);
			}
			p.waitFor();
		}

		catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}


	}

	public void runPerlScriptRealTime() {
		// runs the perl script to generate real time data
		// this method is scheduled with timer
		try {
			
			String perlpath = "C:/Perl64/bin/perl";
			
			
			String cmdString = "cmd.exe /c start " + perlpath +" stock_data.pl";
			System.out.println("Command :" + cmdString);

			Process p = Runtime.getRuntime().exec(cmdString);

			Robot robot = null;
			robot = new Robot();
			for (int i = 0; i < 8; i++) {
				robot.keyPress(KeyEvent.VK_ENTER);
				robot.keyPress(KeyEvent.VK_END);
			}
			p.waitFor();
		}

		catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	public void processHistoricStockData() {
		// reads historic data and stores in the data base

		File directory = new File("historic");
		File files[] = directory.listFiles();

		for (File f : files) { // do necessary processing for each File f
			System.out.println("Now reading File : " + f);
			FileReader fr = null;
			try {
				fr = new FileReader(f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader br = new BufferedReader(fr);
			String strRead;

			int lineNo = 0;
			try {
				while ((strRead = br.readLine()) != null) {
					// skips first 12 lines
					if (lineNo < 12) {
						// nothing
					} else {
						String splitarray[] = strRead.split(",");
						String time = "23.59.59";
						String exch = "NASDAQ";
						/*						 float AvgVolume = 0;
						 for (int a=0; a < strRead.length(); a+=7)
						 {
							 AvgVolume = Float.parseFloat(splitarray[a+5]);
							 AvgVolume+=AvgVolume;
						 }
						 float avgVolume = AvgVolume / (strRead.length()-12);*/

						for (int i = 0; i < splitarray.length; i += 7) {
							String Symbol = f.getName().substring(0,(f.getName().length() - 4));
							String Date = splitarray[i + 0].equalsIgnoreCase("N/A") ? "01/01/2009" : splitarray[i + 0] ;
							String Time = time ; // splitarray[i + 2].equalsIgnoreCase("N/A") ? "00:00:00" : splitarray[i + ] ;
							String Min_Value = splitarray[i + 3].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 3] ; 
							String Max_Value = splitarray[i + 2].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 2] ; 
							String Volume = splitarray[i + 5].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 5] ; 
							Double Avg_Volume = (Double) 0.00 ; // splitarray[i + 6].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 6] ; 
							//String Per_Change = splitarray[i + 7].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 7] ; 
							Float Per_Change = ((Float.parseFloat(Max_Value) - Float.parseFloat(Min_Value)) / Float.parseFloat(Min_Value) * 100);
							StockActivityEntityManager.setStockActivityData(Symbol, exch, Date, Time, Min_Value,
									Max_Value, Volume, Avg_Volume.toString(), Per_Change.toString());

						}

					}
					lineNo++;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void processRealTimeStockData(String folderPath) {
		// reads real time data and stores in the data base
		String folder = "C:/CmpE275/Project1/phoenix-trade-alert/quote2.dat"; 
		StockActivityEntityManager.loadDataFromFileToStockActivityEntity(folder);

	}

	public void detectStockAlerts() {
		// reads all stock data from DB and detects alerts
		// Creating JDBC connection
		//Connection con = null;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			utility.DatabaseUtility conn = new utility.DatabaseUtility("cmpe275");

			//			if (!con.isClosed())
			//				System.out.println("successfully connected to "
			//						+ "MySQL server using TCP/IP...");

			//Statement stmt = con.createStatement();
			String query ; 
			ResultSet rs = null; 
			String result = null; 
			// For Analysis team : All Data for a company for a time period
			query = "SELECT *  FROM `clientrequest` WHERE `clientID` = '"
				+ clientID + "';";

			rs = conn.doQuery(query);  
			if (serviceName.equalsIgnoreCase("analysis")) {
				if (rs.next()){
					String symbol     = rs.getString("symbol");
					String start_date = rs.getString("from_date");
					String end_date   = rs.getString("to_date");

					query = "select * from stockactivity where " 
						+ "`symbol` = '" + symbol +"'" 
						+ " AND `date` BETWEEN '" + start_date + " ' AND '" + end_date + "';"; 

					rs = conn.doQuery(query);  

					// write result to a File
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					String fname = clientID + "_AnalysisReport.dat"; 
					File file = new File(fname);
					boolean exist = file.createNewFile();

					FileWriter fstream = new FileWriter(file);
					BufferedWriter out = new BufferedWriter(fstream);
					out.write("Symbol");
					out.write("\t");
					out.write("Exchange");
					out.write("\t");
					out.write("Date");
					out.write("\t");
					out.write("Time");
					out.write("\t");
					out.write("Min_Price");
					out.write("\t");
					out.write("Max_Price");
					out.write("\t");
					out.write("Volume");
					out.write("\t");
					out.write("Average Volume");
					out.write("\t");
					out.write("Percent Change");
					out.newLine();

					while(rs.next())
					{//write to file here
						out.write(rs.getString("symbol"));
						out.write("\t");
						out.write(rs.getString("exchange"));
						out.write("\t");
						out.write(rs.getString("date"));
						out.write("\t");
						out.write(rs.getString("time"));
						out.write("\t");
						out.write(rs.getString("min_price"));
						out.write("\t");
						out.write(rs.getString("max_price"));
						out.write("\t");
						out.write(rs.getString("volume"));
						out.write("\t");
						out.write(rs.getString("avg_vol"));
						out.write("\t");
						out.write(rs.getString("percentchange"));
						out.write("\t");
						out.newLine();
					}
					out.close();
					// Store the result file in the client request DB  
					query = "UPDATE `ClientRequest` SET "
						+ "`result`='"
						+  fname +"' WHERE `clientID` = '"
						+  clientID + "';" ; 

					System.out.println("Executing query : " + query + "...");
					conn.doUpdate(query);
					conn.closeRS(rs); 
				}

			}
			else {
				
				Properties prop = new Properties(); 
				FileInputStream fis;

				// read config file for volume and price change setup 
				fis = new FileInputStream("stock_alert.properties");
				prop.load(fis);
				Float volume_change = Float.parseFloat(prop.getProperty("volume_change"));
				Float percent_change = Float.parseFloat(prop.getProperty("percent_change"));
				
				query = "select * from stockactivity where  percent_change > " 
					 + percent_change + " AND ((volume - avg_vol )/ avg_vol * 100 ) >= "
					 + volume_change + ";" ;  
				
				rs = conn.doQuery(query);  
				JSONArray stockAlerts = new JSONArray();

				while(rs.next())
				{
					// Convert results to JSON format
					JSONObject alertEntry = new JSONObject();

					alertEntry.put("symbol", rs.getString("symbol")); 
					alertEntry.put("date", rs.getString("date"));
					alertEntry.put("change", rs.getString("time"));
					alertEntry.put("volume", rs.getString("percentchange"));
					alertEntry.put("volume", rs.getString("volume"));
					stockAlerts.add(alertEntry);
					//i ++ ; 
				}
				// Convert JSON to String to store into DB and then pass to Client
				result = stockAlerts.toString(); 

				query = "UPDATE `ClientRequest` SET "
					+ "`result`='"
					+  result +"' WHERE `clientID` = '"
					+  clientID + "';" ; 

				System.out.println("Executing query : " + query + "...");
				conn.doUpdate(query); 				
				conn.closeRS(rs); 						
			}


		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}


	}

	public void generateGraphForStockAlerts() {
		// reads all stock alert data from DB and draw graphs
		GraphGenerator.drawGraph(); 
	}
	
	
	
/* 
 * Query to set Average volume by calculating average from volumes of each symbol
 * ------------------------------------------------------------------------------
 UPDATE cmpe275.stockactivity x
  JOIN (SELECT t1.symbol
             , t1.volume
             , AVG(t2.volume) av
          FROM cmpe275.stockactivity t1
          JOIN cmpe275.stockactivity t2
            ON t2.symbol = t1.symbol GROUP BY t1.symbol
        ) y
     ON y.symbol = x.symbol
    SET x.avg_vol = av;
 * 
 * 	
 */
	private void setAverageVolume() {
		// Since average volume could not be collected by the perl scripts we do the following 
		// Calculate average from the volumes for each company data and storing that in avg col
		// Using the above query to get average from another column in the same table

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			utility.DatabaseUtility conn = new utility.DatabaseUtility("cmpe275");

			String query ; 
			ResultSet rs = null; 
			String result = null; 
			query = "UPDATE cmpe275.stockactivity x " 
				+ "JOIN (SELECT t1.symbol, t1.volume, AVG(t2.volume) av " 
				+ "FROM cmpe275.stockactivity t1 "
				+ "JOIN cmpe275.stockactivity t2 "
				+ "ON t2.symbol = t1.symbol GROUP BY t1.symbol ) y "
				+ "ON y.symbol = x.symbol "
				+ "SET x.avg_vol = av; " ; 

			System.out.println("Executing query : " + query + "...");
			conn.doUpdate(query);
			conn.closeRS(rs); 
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}


	}
	
	
}
