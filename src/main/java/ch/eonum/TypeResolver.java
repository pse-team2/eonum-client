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
	private static HashMap<String, String> types = new HashMap<String, String>();
	private static final TypeResolver instance = new TypeResolver();

	public static TypeResolver getInstance()
	{
		return instance;
	}

	/**
	 * Retrieves a list of all categories the server is aware of
	 * and puts them into a HashMap along with the appropriate translation.
	 */
	private TypeResolver()
	{
		HTTPRequest request = new HTTPRequest();
		String resultString = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			resultString = httpTask.get();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			// Restore the interrupted status
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		Log.i(this.getClass().getName(), "Size of results in TypeResolver: " + resultString.length());

		// Parse results
		JSONParser parser = new JSONParser();

		ArrayList<String> typesList = new ArrayList<String>();
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
			TypeResolver.types.put(categoryEntry, visibleDescription);
			typesList.add(visibleDescription);
		}

		if (!error.isEmpty())
		{
			Log.w("Resource not found", error.toString());
			AlertDialog.Builder builder = new AlertDialog.Builder(HealthActivity.mainActivity);
			builder.setCancelable(false);
			builder.setTitle(HealthActivity.mainActivity.getString(R.string.missing_translations));
			builder.setMessage(String.format(HealthActivity.mainActivity.getString(R.string.missing_translations_list),
				error.toString()));
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

	public String[] getAllCategories()
	{
		String[] typesArray = new String[TypeResolver.types.size()];
		return TypeResolver.types.values().toArray(typesArray);
	}

	public String resolve(String type)
	{
		return TypeResolver.types.get(type);
	}

	
	public static String getKeyByValue(String value)
	{
		for (Entry<String, String> entry : TypeResolver.types.entrySet())
		{
			if (value.equals(entry.getValue()))
			{
				return entry.getKey();
			}
		}
		return null;
	}
}
