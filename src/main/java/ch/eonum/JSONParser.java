package ch.eonum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fills the data obtained from the server into arrays as defined in {@link MedicalLocation}.
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

			// Split up into addresses
			int NUMBER_OF_RESULTS = jsonArray.length();

			results = new MedicalLocation[NUMBER_OF_RESULTS];
			double latitude, longitude;

			Logger.info(this.getClass().getName(), "Start parsing " + jsonArray.length() + " results");

			for (int i = 0; i < NUMBER_OF_RESULTS; i++)
			{
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.getString("name");
				String address = jsonObject.getString("address");
				String email = jsonObject.getString("email");

				JSONArray jsonTypes = jsonObject.getJSONArray("types");
				String[] types = new String[jsonTypes.length()];
				for (int j = 0; j < jsonTypes.length(); j++)
				{
					String category = jsonTypes.getString(j);
					// Remove all non-word characters
					types[j] = category.replaceAll("\\W", "");
				}

				JSONObject location = jsonObject.getJSONObject("location");
				latitude = location.getDouble("lat");
				longitude = location.getDouble("lng");

				results[i] = new MedicalLocation(name, address, email, types, latitude, longitude);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		Logger.info(this.getClass().getName(), "Finished, return " + results.length + " results");
		return results;
	}

	public String[] deserializeCategories(String str)
	{
		Logger.info(this.getClass().getName(), "Start deserializing categories");
		String[] results = new String[] {};
		JSONObject jsonObj;
		try
		{
			jsonObj = new JSONObject(str);
			JSONArray jsonArray = jsonObj.getJSONArray("categories");
			results = new String[jsonArray.length()];

			Logger.info(this.getClass().getName(), "Start parsing " + jsonArray.length() + " results");

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
		Logger.info(this.getClass().getName(), "Finished, return " + results.length + " results");
		return results;
	}
}
