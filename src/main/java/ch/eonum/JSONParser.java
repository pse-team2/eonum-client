package ch.eonum;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONParser {

	public String[] deserialize(String str) {
		String[] result = null;
		
		try {
			// string to jsonarray
			//JSONArray jsonArray = new JSONArray(str);
			JSONObject jsonObj = new JSONObject(str);
			JSONArray jsonArray = jsonObj.getJSONArray("results");
			
			// split up into addresses
			result = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				result[i] = jsonObject.getString("name");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		return result;
	}
}