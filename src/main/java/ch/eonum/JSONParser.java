package ch.eonum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Fills the data obtained from the server into arrays
 * as defined in {@link MedicalLocation}
 */

public class JSONParser
{

	public MedicalLocation[] deserializeLocations(String str)
	{
		MedicalLocation[] results = new MedicalLocation[] {};

		try
		{
			JSONObject jsonObj = new JSONObject(str);
			JSONArray jsonArray = jsonObj.getJSONArray("results");

			// split up into addresses
			int MAX_RESULTS = jsonArray.length();

			results = new MedicalLocation[MAX_RESULTS];
			double latitude, longitude;

			Log.i(this.getClass().getName(), "Start parsing " + jsonArray.length() + " results");

			for (int i = 0; i < MAX_RESULTS; i++)
			{
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.getString("name");
				String address = jsonObject.getString("address");
				String email = jsonObject.getString("email");
				
				String type = jsonObject.getString("types");

				// regex on category
				type = type.replaceAll("\\W", "");

				JSONObject location = jsonObject.getJSONObject("location");
				latitude = location.getDouble("lat");
				longitude = location.getDouble("lng");

				results[i] = new MedicalLocation(name, address, email, type, latitude, longitude);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		Log.i(this.getClass().getName(), "Finished, return " + results.length + " results");
		return results;
	}

	public String[] deserializeCategories(String str)
	{
		Log.i(this.getClass().getName(), "Start deserializing categories");
		String[] results = new String[] {};
		JSONObject jsonObj;
		try
		{
			jsonObj = new JSONObject(str);
			JSONArray jsonArray = jsonObj.getJSONArray("categories");
			results = new String[jsonArray.length()];

			Log.i(this.getClass().getName(), "Start parsing " + jsonArray.length() + " results");

			for (int i = 0; i < jsonArray.length(); i++)
			{
				String category = jsonArray.getString(i);
				results[i] = category;
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		Log.i(this.getClass().getName(), "Finished, return " + results.length + " results");
		return results;
	}
}
