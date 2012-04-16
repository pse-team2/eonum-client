package ch.eonum;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Resolves a medical type to a String.
 */
public class TypeResolver extends AsyncTask<Void, Void, String[]>
{

	// Names (german, plural)
	HashMap<String, String> types = new HashMap<String, String>();
	Activity activity;
	ArrayList<String> error = new ArrayList<String>();

	/**
	 * Gets a list of available categories from the server and parses them into an array.
	 * The main thread is denied to establish network connections resulting in an
	 * android.os.NetworkOnMainThreadException when doing so.
	 * For this reason the task of fetching the category list from the server is detached in an AsyncTask.
	 */
	public TypeResolver()
	{
	}

	@Override
	protected void onPreExecute()
	{
		this.activity = HealthActivity.mainActivity;
	}

	@Override
	protected String[] doInBackground(Void... params)
	{
		HTTPRequest request = new HTTPRequest();
		String resultString = request.getResults();
		Log.i(this.getClass().getName(), "Size of results in TypeResolver: " + resultString.length());

		// Parse results
		JSONParser parser = new JSONParser();

		ArrayList<String> typesList = new ArrayList<String>();
		String[] typesArray = new String[types.size()];
		for (String categoryEntry : parser.deserializeCategories(resultString))
		{
			int id = this.activity.getResources().getIdentifier(categoryEntry, "string",
				this.getClass().getPackage().getName());
			String visibleDescription;
			try
			{
				visibleDescription = this.activity.getString(id);
			}
			catch (NotFoundException e)
			{
				visibleDescription = categoryEntry;
				this.error.add(visibleDescription);
			}
			types.put(categoryEntry, visibleDescription);
			typesList.add(visibleDescription);
			// Log.i("visibleDescription", visibleDescription);
		}
		return typesList.toArray(typesArray);
	}

	@Override
	protected void onPostExecute(String[] categories)
	{
		if (!error.isEmpty())
		{
			Log.e("Resource not found", this.error.toString());
			AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
			builder.setCancelable(false);
			builder.setTitle("Error: Missing translation resources");
			builder.setMessage(String.format("String resources for\n%s\nwere not found!", this.error.toString()));
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
	}

	public String resolve(String type)
	{
		return types.get(type);
	}
}
