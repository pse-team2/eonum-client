package ch.eonum;

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

	public QueryData(Activity application)
	{
		this.activity = application;
		this.dialog = new ProgressDialog(application.getApplicationContext());
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

	private ch.eonum.MedicalLocation[] queryServer(double latitude, double longitude)
	{
		Log.i(this.getClass().getName(), "Location to query: " + latitude + " : " + longitude);
		// Send query to server
		HTTPRequest request = new HTTPRequest(latitude, longitude);
		String resultString = request.getResults();
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserialize(resultString);
	}
}
