package ch.eonum;

import java.util.List;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class ShowLocation extends Activity implements LocationListener {

	private double latitude;
	private double longitude;
	private LocationManager locMgr = null;
	TextView locationTxt, list;
	HTTPRequest request;

	String resultString;
	List<String> resultList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);

		locationTxt = (TextView) findViewById(R.id.locationlabel);
		locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
		this.locationTxt.setText("Location: " + this.latitude + " : " + this.longitude);
		Log.i("Location change", "" + latitude + " : " + longitude);
		
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}