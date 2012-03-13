package ch.eonum;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;


public class JSONParser {

	public String[] deserialize(String str) {
		String[] result = null;
		try {
			JSONObject jsonobj = new JSONObject(str);
			//JSONArray jsonArray = new JSONArray(str);
			result = new String[jsonobj.length()];
			Log.i(JSONParser.class.getName(), "Number of JSON entries: " + jsonobj.length());
			
			// TODO atm: parsing for 10 objects
			for (int i = 0; i < 1; i++) {
				//JSONObject jsonObject = jsonArray.getJSONObject(i);
				result[i] = jsonobj.getString("name");
				
				// Log.i(JSONParser.class.getName(), jsonObject.getString("text"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}

/*

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ParseJSON extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		String readTwitterFeed = readTwitterFeed();
		try {
			JSONArray jsonArray = new JSONArray(readTwitterFeed);
			Log.i(ParseJSON.class.getName(),
					"Number of entries " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Log.i(ParseJSON.class.getName(), jsonObject.getString("text"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readTwitterFeed() {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"http://twitter.com/statuses/user_timeline/vogella.json");
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(ParseJSON.class.toString(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder		try {
			JSONArray jsonArray = new JSONArray(readTwitterFeed);
			Log.i(ParseJSON.class.getName(),
					"Number of entries " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Log.i(ParseJSON.class.getName(), jsonObject.getString("text"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}.toString();
	}
}

*/