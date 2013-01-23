package bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class StockActivityEntityManager {

	public static void main(String[] args) {
		// testing ..
		String file = "quote2.dat" ; //C:/CmpE275/Project1/phoenix-trade-alert/quote2.dat 
		loadDataFromFileToStockActivityEntity(file);

	}

	public static void loadDataFromFileToStockActivityEntity(String filePath) {
		try {
			
			File f = new File(filePath); 
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String strLine = br.readLine();
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				String splitarray[] = strLine.split(",");
				String exch = "NASDAQ";
				for (int i = 0; i < splitarray.length; i += 8) {
					String Symbol = splitarray[i + 0];
					String Date = splitarray[i + 1].equalsIgnoreCase("N/A") ? "01/01/2009" : splitarray[i + 1] ;
					String Time = splitarray[i + 2].equalsIgnoreCase("N/A") ? "00:00:00" : splitarray[i + 2] ;
					String Min_Value = splitarray[i + 3].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 3] ; 
					String Max_Value = splitarray[i + 4].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 4] ; 
					String Volume = splitarray[i + 5].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 5] ; 
					String Avg_Volume = splitarray[i + 6].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 6] ; 
					String Per_Change = splitarray[i + 7].equalsIgnoreCase("N/A") ? "0" : splitarray[i + 7] ; 

					setStockActivityData(Symbol, exch, Date, Time, Min_Value,
							Max_Value, Volume, Avg_Volume, Per_Change);

				}

			}
			// Close the input stream
			fr.close();

		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	}

	@SuppressWarnings("deprecation")
	public static void setStockActivityData(String symbol, String exchange,
			String date, String time, String min_price, String max_price,
			String volume, String avg_volume, String percentchange) {
		EntityManagerFactory entityManagerFactory = Persistence
				.createEntityManagerFactory("StockActivityPU");

		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction userTransaction = em.getTransaction();
		userTransaction.begin();

		DateFormat dateformat = new SimpleDateFormat("mm/dd/yyyy"); // "yyyy-mm-dd");

		StockActivityEntity stockobj = new StockActivityEntity();
		try {
			stockobj.setSymbol(symbol);
			stockobj.setExchange(exchange);
			stockobj.setDate(dateformat.parse(date));
			stockobj.setTime(Time.valueOf(time));
			stockobj.setMin_price(Float.parseFloat(min_price));
			stockobj.setMax_price(Float.parseFloat(max_price));
			stockobj.setVolume(Long.parseLong(volume));
			stockobj.setAvg_volume(Long.parseLong(avg_volume));
			stockobj.setPercentchange(Float.parseFloat(percentchange));
			em.persist(stockobj);
			userTransaction.commit();
			em.close();
			entityManagerFactory.close();

		}

		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}