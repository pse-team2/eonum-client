package ch.eonum;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

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
		Timer t = new Timer();
		Log.i(this.getClass().getName(), "Start reading answer from server at " + t.timeElapsed() + "ms");
		Log.i(this.getClass().getName(), "   url: " + url);

		final int APPROX_MAX_PAGE_SIZE = 500;
		try
		{
			InputStream inputStream = url.openStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(APPROX_MAX_PAGE_SIZE);

			byte[] buf = new byte[APPROX_MAX_PAGE_SIZE];
			int read;
			do
			{
				read = bufferedInputStream.read(buf, 0, buf.length);
				if (read > 0)
					byteArrayBuffer.append(buf, 0, read);
			}
			while (read >= 0);
			this.resultString = new String(byteArrayBuffer.toByteArray());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Log.i(this.getClass().getName(), "Size of HTTP answer: " + resultString.length() + " at " + t.timeElapsed()
			+ "ms");
		return resultString;
	}

	@Override
	protected void onPostExecute(String resString)
	{
		Log.i(this.getClass().getName(), "Size of results from server in onPostExecute: " + resString.length());
		this.dialog.dismiss();
	}
}
