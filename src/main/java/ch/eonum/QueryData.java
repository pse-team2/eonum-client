package ch.eonum;

import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import android.util.Log;

/** Used to query results */
public class QueryData
{

	public QueryData()
	{
	}

	public MedicalLocation[] getData(double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude)
	{
		MedicalLocation[] result = this.queryServer(lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
		Log.i(this.getClass().getName(), "Size of results from server: " + result.length);
		return result;
	}

	public MedicalLocation[] getData(double lat1, double long1)
	{
		MedicalLocation[] result = this.queryServer(lat1, long1);
		Log.i(this.getClass().getName(), "Size of results from server: " + result.length);
		return result;
	}

	public MedicalLocation[] getData(double lat1, double long1, String category)
	{
		MedicalLocation[] result = this.queryServer(lat1, long1, category);
		Log.i(this.getClass().getName(), "Size of results from server: " + result.length);
		return result;
	}

	private ch.eonum.MedicalLocation[] queryServer(double lat1, double long1, String category)
	{
		double d = 0.05;
		Log.i(this.getClass().getName(), "location to query: " + lat1 + " : " + long1 + ", limited to category " + category);
		// Send query to server
		HTTPRequest request = new HTTPRequest(lat1-d, long1-d, lat1+d, long1+d, category);
		String resultString = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			resultString = httpTask.get();
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
			// Restore the interrupted status
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException e1)
		{
			e1.printStackTrace();
		}
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserializeLocations(resultString);
	}
	
	private ch.eonum.MedicalLocation[] queryServer(double lat1, double long1)
	{
		double d = 0.05;
		Log.i(this.getClass().getName(), "Single location to query: " + lat1 + " : " + long1);
		// Send query to server
		HTTPRequest request = new HTTPRequest(lat1-d, long1-d, lat1+d, long1+d);
		String resultString = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			resultString = httpTask.get();
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
			// Restore the interrupted status
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException e1)
		{
			e1.printStackTrace();
		}
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
		String resultString = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			resultString = httpTask.get();
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
			// Restore the interrupted status
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException e1)
		{
			e1.printStackTrace();
		}
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserializeLocations(resultString);
	}
}
