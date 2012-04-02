package ch.eonum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Fills the data obtained from the server into arrays as defined in {@link Location} */
public class JSONParser
{

	public Location[] deserialize(String str)
	{
		Location[] results = null;

		try
		{
			JSONObject jsonObj = new JSONObject(str);
			JSONArray jsonArray = jsonObj.getJSONArray("results");

			// split up into addresses
			results = new Location[jsonArray.length()];
			double latitude, longitude;

			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.getString("name");
				String type = jsonObject.getString("types");

				// regex on category
				type = type.replaceAll("\\W", "");

				JSONObject location = jsonObject.getJSONObject("location");
				latitude = location.getDouble("lat");
				longitude = location.getDouble("lng");

				results[i] = new Location(name, type, latitude, longitude);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return results;
	}
}
