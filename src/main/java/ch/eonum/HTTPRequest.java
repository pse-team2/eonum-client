package ch.eonum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

/** Fetches the raw data from the server */
public class HTTPRequest extends AsyncTask<Void, Void, String>
{

	URL url;
	String resultString;
	ProgressDialog dialog;

	public HTTPRequest(double lat1, double long1, double lat2, double long2)
	{

		this.dialog = new ProgressDialog(HealthActivity.mainActivity);
		resultString = "";

		try
		{
			url = new URL("http://77.95.120.72:8080/finder?lat1=" + lat1 + "&long1=" + long1 + "&lat2=" + lat2 + "&long2=" + long2);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	public HTTPRequest(double lat1, double long1, double lat2, double long2, String category)
	{

		this.dialog = new ProgressDialog(HealthActivity.mainActivity);
		resultString = "";

		try
		{
			url = new URL("http://77.95.120.72:8080/finder?lat1=" + lat1 + "&long1=" + long1 + "&lat2=" + lat2 + "&long2=" + long2 + "&category=" + category);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	public HTTPRequest()
	{

		this.dialog = new ProgressDialog(HealthActivity.mainActivity);
		resultString = "";

		try
		{
			url = new URL("http://77.95.120.72:8080/categories");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onPreExecute()
	{
		this.dialog = ProgressDialog.show(HealthActivity.mainActivity,
			HealthActivity.mainActivity.getString(R.string.pleasewait),
			HealthActivity.mainActivity.getString(R.string.sendingrequest), true, false);
	}

	@Override
	protected String doInBackground(Void... params)
	{
		Log.i(this.getClass().getName(), "Start reading answer from server with url:");
		Log.i(this.getClass().getName(), "   " + url);
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String str;
			while ((str = in.readLine()) != null)
			{
				resultString += str;
			}
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Log.i(this.getClass().getName(), "Size of HTTP answer: " + resultString.length());
		return resultString;
	}

	@Override
	protected void onPostExecute(String resString)
	{
		Log.i(this.getClass().getName(), "Size of results from server in onPostExecute: " + resString.length());
		this.dialog.dismiss();
	}
}
