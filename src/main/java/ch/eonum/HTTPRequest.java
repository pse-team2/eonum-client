package ch.eonum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class HTTPRequest {
	
	URL url;
	String resultString;
	
	public HTTPRequest(double latitude, double longitude) {
		
		resultString = "";
		
	    try {
			url = new URL("http://77.95.120.72:8080/finder?lat="+latitude+"&long="+longitude);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public String getResults() {
		try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	resultString += str;
		    }
		    in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}/*
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		Log.i(HTTPRequest.class.getName(), "Size of HTTP answer: "+resultString.length());
		return resultString;
	}
}