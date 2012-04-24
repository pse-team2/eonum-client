package ch.eonum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

/** Fetches the raw data from the server */
public class HTTPRequest
{

	URL url;
	String resultString;

//	public HTTPRequest(double lat1, double long1)
//	{
//
//		resultString = "";
//		double d = 0.05;
//		
//		try
//		{
//			url = new URL("http://77.95.120.72:8080/finder?lat1=" + (lat1-d) + "&long1=" + (long1-d) + "&lat2=" + (lat1+d) + "&long2=" + (long1+d));
//		}
//		catch (MalformedURLException e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	public HTTPRequest(double lat1, double long1, double lat2, double long2)
	{

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

	public HTTPRequest()
	{

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

	public String getResults()
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
}
