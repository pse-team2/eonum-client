package ch.eonum;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class ShowLocation extends MapActivity
{

	private double latitude;
	private double longitude;
	private LocationManager locMgr = null;
	/* Implement Location Listener */
	private LocationListener locLst = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location)
		{
			ShowLocation.this.latitude = location.getLatitude();
			ShowLocation.this.longitude = location.getLongitude();
			String Text = "Location: " + ShowLocation.this.latitude + " : " + ShowLocation.this.longitude;
			ShowLocation.this.locationTxt.setText(Text);
			Log.i("Location change", "" + ShowLocation.this.latitude + " : " + ShowLocation.this.longitude);
			Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	};
	/* End of implemented LocationListener */
	TextView locationTxt, list;
	HTTPRequest request;

	String resultString;
	List<String> resultList;

	MapView mapView;
	List<Overlay> mapOverlays;
	Drawable drawable;
	MapItemizedOverlay itemizedOverlay;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);

		this.mapView = (MapView) findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setSatellite(true);

		this.mapOverlays = this.mapView.getOverlays();
		this.drawable = this.getResources().getDrawable(R.drawable.icon);
		this.itemizedOverlay = new MapItemizedOverlay(this.drawable, this);

		// Use the LocationManager class to obtain GPS locations
		this.locationTxt = (TextView) findViewById(R.id.locationlabel);
		this.locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Register the listener with the Location Manager to receive location updates
		this.locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.locLst);
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	public void onPostExecute()
	{
		GeoPoint initGeoPoint = new GeoPoint(
			(int) (this.locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude() * 1000000),
			(int) (this.locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude() * 1000000)
			);
		OverlayItem overlayitem = new OverlayItem(initGeoPoint, "", "");

		this.itemizedOverlay.addOverlay(overlayitem);
		this.mapOverlays.add(this.itemizedOverlay);

		this.locMgr.removeUpdates(this.locLst);
	}

}