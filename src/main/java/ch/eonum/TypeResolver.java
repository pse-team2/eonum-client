package ch.eonum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Resolves a medical type to a String.
 */
public class TypeResolver
{

	private static final TypeResolver instance = new TypeResolver();

	// Names (german, plural, male)
	private static HashMap<String, String> types = new HashMap<String, String>();

	private TypeResolver()
	{
	}

	public static TypeResolver getInstance()
	{
		return instance;
	}

	public String[] getAllCategories()
	{
		HTTPRequest request = new HTTPRequest();
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
		Log.i(this.getClass().getName(), "Size of results in TypeResolver: " + resultString.length());

		// Parse results
		JSONParser parser = new JSONParser();

		ArrayList<String> typesList = new ArrayList<String>();
		String[] typesArray = new String[types.size()];
		ArrayList<String> error = new ArrayList<String>();

		for (String categoryEntry : parser.deserializeCategories(resultString))
		{
			int id = HealthActivity.mainActivity.getResources().getIdentifier(categoryEntry, "string",
				this.getClass().getPackage().getName());
			String visibleDescription;
			try
			{
				visibleDescription = HealthActivity.mainActivity.getString(id);
			}
			catch (NotFoundException e)
			{
				visibleDescription = categoryEntry;
				error.add(visibleDescription);
			}
			types.put(categoryEntry, visibleDescription);
			typesList.add(visibleDescription);
			// Log.i("visibleDescription", visibleDescription);
		}

		// Display missing translation resources
		if (!error.isEmpty())
		{
			Log.e("Resource not found", error.toString());
			AlertDialog.Builder builder = new AlertDialog.Builder(HealthActivity.mainActivity);
			builder.setCancelable(false);
			builder.setTitle("Error: Missing translation resources");
			builder.setMessage(String.format("String resources for\n%s\nwere not found!", error.toString()));
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setNeutralButton(android.R.string.ok, new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
			AlertDialog alert = builder.create();
			alert.show();
		}

		return typesList.toArray(typesArray);
	}

	public String resolve(String type)
	{
		return types.get(type);
	}
	
	
	public static String getKeyByValue(String value) {
	    for (Entry<String, String> entry : types.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
}
