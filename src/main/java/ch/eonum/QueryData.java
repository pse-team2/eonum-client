package ch.eonum;

import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

/** Used to query results */
public class QueryData extends AsyncTask<Double, Void, MedicalLocation[]>
{

	Activity activity;
	ProgressDialog dialog;
	ArrayAdapter<String> adapter;
	MedicalLocation[] res;

	public QueryData()
	{
		this.activity = HealthActivity.mainActivity;
		this.dialog = new ProgressDialog(this.activity.getApplicationContext());
	}

	/** Gets called just before the thread begins */
	@Override
	protected void onPreExecute()
	{
		this.dialog = ProgressDialog.show(this.activity, this.activity.getString(R.string.pleasewait),
			this.activity.getString(R.string.sendingrequest), true, false);
	}

	/**
	 * Main part comes here
	 * The datatype of the first parameter in the class definition matches the one passed to this method
	 * The datatype of the last parameter in the class definition matches the return type of this method
	 */
	@Override
	protected MedicalLocation[] doInBackground(Double... params)
	{
		Log.i(this.getClass().getName(), "Got arguments: " + params.length);
		Log.i(this.getClass().getName(), Arrays.asList(params).toString());

		if (params.length == 4)
		{
			// params[0]: lowerLeftLatitude
			// params[1]: lowerLeftLongitude
			// params[2]: upperRightLatitude
			// params[3]: upperRightLongitude
			return queryServer(params[0], params[1], params[2], params[3]);
		}
		double myLatitude = params[0];
		double myLongitude = params[1];
		// publishProgress();
		return queryServer(myLatitude, myLongitude);
	}

	/**
	 * Called from the publish progress
	 * The datatype of the second parameter gets passed to this method
	 */
	@Override
	protected void onProgressUpdate(Void... values)
	{
		// Increment Progress Dialog with the update from the doInBackgroundMethod
	}

	/**
	 * Called as soon as doInBackground method completes
	 * The third parameter gets passed to this method
	 */
	@Override
	protected void onPostExecute(MedicalLocation[] result)
	{
		Log.i(this.getClass().getName(), "Size of results from server in onPostExecute: " + result.length);
		this.dialog.dismiss();
	}

	private ch.eonum.MedicalLocation[] queryServer(double lat1, double long1)
	{
		double d = 0.05;
		Log.i(this.getClass().getName(), "Single location to query: " + lat1 + " : " + long1);
		// Send query to server
		HTTPRequest request = new HTTPRequest(lat1-d, long1-d, lat1+d, long1+d);
		String resultString = request.getResults();
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserializeLocations(resultString);
	}
	
	private ch.eonum.MedicalLocation[] queryServer(double lat1, double long1, double lat2, double long2)
	{
		Log.i(this.getClass().getName(), "Map rectangle to query: " + lat1 + "/" + long1+ "," + lat2 + "/" + long2);
		// Send query to server
		HTTPRequest request = new HTTPRequest(lat1, long1, lat2, long2);
		String resultString = request.getResults();
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserializeLocations(resultString);
	}
}
