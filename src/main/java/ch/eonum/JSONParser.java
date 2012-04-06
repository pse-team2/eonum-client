package ch.eonum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/** Fills the data obtained from the server into arrays as defined in {@link MedicalLocation} */
public class JSONParser
{

	public MedicalLocation[] deserialize(String str)
	{
		MedicalLocation[] results = null;

		try
		{
			JSONObject jsonObj = new JSONObject(str);
			JSONArray jsonArray = jsonObj.getJSONArray("results");

			// split up into addresses
			results = new MedicalLocation[jsonArray.length()];
			double latitude, longitude;

			Log.i(this.getClass().getName(), "Start parsing "+jsonArray.length()+" results");
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

				results[i] = new MedicalLocation(name, type, latitude, longitude);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		Log.i(this.getClass().getName(), "Finished, return results");
		return results;
	}
}
