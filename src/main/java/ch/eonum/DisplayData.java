package ch.eonum;

import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
//import android.widget.Toast;

public class DisplayData extends ListActivity {

	ProgressDialog dialog;
	ArrayAdapter<String> adapter;
	String[] res = { "" };

	public class GetQuery extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(DisplayData.this, "Bitte warten...",
					"Abfrage wird gesendet", true, false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO for now, just some sample query
			String[] d = queryServer(46.95, 7.15);
			res = d;
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			updateAdapter();
			dialog.dismiss();
		}
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		/*
		final Bundle extras = getIntent().getExtras();
		query = extras.getString("query");
		characters = Integer.valueOf(extras.getString("characters"));
		 */
		
		// ProgressDialog dialog = ProgressDialog.show(MyList.this, "",
		// "Loading. Please wait...", true);
		
		new GetQuery().execute();

		String[] results = res;

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, results);
		setListAdapter(adapter);
	}

	protected void updateAdapter() {
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, res);
		setListAdapter(adapter);
	}

	/**
	 * On item click, show some text. TODO some beter alt text, maybe more
	 * information
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//String item = (String) getListAdapter().getItem(position);
		//Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	}

	private String[] queryServer(double latitude, double longitude) {
		// send query to server
		HTTPRequest request = new HTTPRequest(latitude, longitude);
		String resultString = request.getResults();
		
		Log.i(DisplayData.class.getName(), "Size of results from server: " + resultString.length());
		
		// parse results
		JSONParser parser = new JSONParser();
		String[] entries = parser.deserialize(resultString);
		
		// TODO some temporary data, delete when JSONParser works
		//String[] entries = {};

		String[] results;

		if (entries != null) {
			// sort entries
			assert ((entries.length % 3) == 0);
			String[] tmp = new String[Math.min(entries.length / 3, 50)];
			
			// TODO rearrangement for display, for improvement
			for (int i = 0, j = 0; i < entries.length; i += 3) {
				if (j >= 50)
					break;
				tmp[j++] = entries[i + 2] + " - " + entries[i + 1] + " - "
						+ entries[i];
			}
			results = tmp;

		} else {
			results = new String[] {"Leider keine Resultate zurückerhalten.",  "Abfrage: "+latitude+"/"+longitude};
		}
		return results;
	}
}