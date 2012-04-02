package ch.eonum;

import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// import android.widget.Toast;

public class DisplayData extends ListActivity
{

	ProgressDialog dialog;
	ArrayAdapter<String> adapter;
	String[] res = {""};

	public class GetQuery extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute()
		{
			dialog = ProgressDialog.show(DisplayData.this, getString(R.string.pleasewait),
				getString(R.string.sendingrequest), true, false);
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO for now, just some sample query
			String[] d = queryServer(46.95, 7.15);
			res = d;
			return null;
		}

		@Override
		protected void onPostExecute(Void v)
		{
			updateAdapter();
			dialog.dismiss();
		}
	}

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		/*final Bundle extras = getIntent().getExtras();
		 * query = extras.getString("query");
		 * characters = Integer.valueOf(extras.getString("characters")); */

		// ProgressDialog dialog = ProgressDialog.show(MyList.this, "",
		// "Loading. Please wait...", true);

		new GetQuery().execute();

		String[] results = res;

		adapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_list_item_1, results);
		setListAdapter(adapter);
	}

	protected void updateAdapter()
	{
		adapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_list_item_1, res);
		setListAdapter(adapter);
	}

	/**
	 * On item click, show some text. TODO some beter alt text, maybe more
	 * information
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		// String item = (String) getListAdapter().getItem(position);
		// Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	}

	private String[] queryServer(double latitude, double longitude)
	{
		// send query to server
		HTTPRequest request = new HTTPRequest(latitude, longitude);
		String resultString = request.getResults();

		Log.i(DisplayData.class.getName(), "Size of results from server: " + resultString.length());

		// parse results
		JSONParser parser = new JSONParser();
		Location[] entries = parser.deserialize(resultString);

		String[] results = new String[entries.length];

		for (int i = 0; i < entries.length; i++)
		{
			results[i] = entries[i].getName() + " (" + entries[i].getType() + ")";
		}
		// TODO handle case with no results
		/*else {
		 * results = new String[] {getString(R.string.noresults), getString(R.string.query) + ": " + latitude
		 * + "/" + longitude};
		 * } */
		return results;
	}
}
