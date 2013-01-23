package client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import bean.StockMonitor;

public class ClientTest {

	static Context ctx;

	public static void main(String[] args) throws Exception {

		try {
			Context ctx = getContext();
			bean.StockMonitor tradeAlert = (StockMonitor) ctx
					.lookup("StockMonitorBean/remote");
			String request  = "Analysis" ;  // or "Consultancy"
			String result = tradeAlert.monitorStock(request);
			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// to test JSON parsing...
		String json = "[{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"},{\"symbol\":\"<value from resultlist>\",\"change\":\"<value from resultlist>\",\"volume\":\"<value from resultlist>\",\"date\":\"<value from resultlist>\"}]";
		parseJSON(json);

	}

	private static Context getContext() throws Exception {
		if (ctx != null)
			return ctx;

		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		props.put(Context.URL_PKG_PREFIXES,
				"org.jboss.naming:org.jnp.interfaces");
		ctx = new InitialContext(props);

		return ctx;
	}

	// Parse JSON text files and convert these to a Java model
	private static void parseJSON(String json) throws Exception {
		if (json != null) {

			JSONParser parser = new JSONParser();
			Object obj = parser.parse(json);
			JSONArray array = (JSONArray) obj;
			// Iterator iter = array.iterator();
			array.size();
			int i = 0;
			while (i < array.size()) { // iter.hasNext()){
				JSONObject jobj = (JSONObject) array.get(i);
				System.out.println("Symbol : " + jobj.get("symbol"));
				System.out.println("Date : " + jobj.get("date"));
				System.out.println("Change : " + jobj.get("change"));
				System.out.println("Vol : " + jobj.get("volume"));
				System.out.println("------------------------");
				i++;
			}

		}
	}

}
