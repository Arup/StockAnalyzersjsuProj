package utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//JSONRenderer : Render a Java representation into text
public class JSONRenderer {
	
	@SuppressWarnings("unchecked")
	public static String renderAlertToJSON(){
		
		JSONArray stockAlerts = new JSONArray();

		// read from alert database into resultsets
		
		
		//loop result set and convert to JSON
		int i = 0; 
		while (i < 10) {
			JSONObject alertEntry = new JSONObject();
	
			alertEntry.put("symbol", "<value from resultlist>"); 
			alertEntry.put("date", "<value from resultlist>");
			alertEntry.put("change", "<value from resultlist>");
			alertEntry.put("volume", "<value from resultlist>");
			stockAlerts.add(alertEntry);
			i ++ ; 
		}
		
		// convert to string to send to client
		String output = stockAlerts.toString(); 
		
		return output; 
	}
	
	
	public static void main(String args[]){
		System.out.println(renderAlertToJSON()); 
		
	}
	
}
