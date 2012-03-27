package ch.eonum;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
//import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HealthActivity extends MapActivity {

	private double latitude;
	private double longitude;
	private LocationManager locMgr = null;
	/* Implement Location Listener */
	private LocationListener locLst = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			HealthActivity.this.latitude = location.getLatitude();
			HealthActivity.this.longitude = location.getLongitude();
			String Text = getString(R.string.location) + ": " + HealthActivity.this.latitude + " : " + HealthActivity.this.longitude;
			Log.i("Location change", HealthActivity.this.latitude + " : " + HealthActivity.this.longitude);
			Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();

			// Remove other points
			HealthActivity.this.mapOverlays.clear(); // Not visible
			HealthActivity.this.itemizedOverlay.clear(); // Visible

			GeoPoint initGeoPoint = new GeoPoint(
					(int) (HealthActivity.this.locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude() * 1000000),
					(int) (HealthActivity.this.locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude() * 1000000)
					);
				OverlayItem overlayitem = new OverlayItem(initGeoPoint, "", "");
				HealthActivity.this.itemizedOverlay.addOverlay(overlayitem);
				HealthActivity.this.mapOverlays.add(HealthActivity.this.itemizedOverlay);

				MapController mc = HealthActivity.this.mapView.getController();
				mc.setZoom(16);
				mc.animateTo(initGeoPoint);
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), getString(R.string.gpsdisabled), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), getString(R.string.gpsenabled), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
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

	/**
	 * Main Activity:
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(HealthActivity.class.getName(), "Main Activity started.");
		setContentView(R.layout.main);

		/** searchforWhere */
		TextView searchforWhere = (TextView) findViewById(R.id.searchforWhere);
		searchforWhere.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					Toast.makeText(getApplicationContext(), "Where is "+((TextView) findViewById(R.id.searchforWhere)).getText()+"?", Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});

		/** searchforWhat */
		TextView searchforWhat = (TextView) findViewById(R.id.searchforWhat);
		searchforWhat.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					Toast.makeText(getApplicationContext(), "What is "+((TextView) findViewById(R.id.searchforWhat)).getText()+"?", Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});

		/** MapView */
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

		/** Button "location" */
		Button location = (Button) findViewById(R.id.location);
		location.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LocationManager tmpLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				if (!tmpLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setMessage(getString(R.string.askusertoenablegps))
							.setCancelable(true)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int id) {
											Intent gpsOptionsIntent = new Intent(
													android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
											startActivity(gpsOptionsIntent);
										}
									});
					builder.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				} else {
					startActivity(new Intent(view.getContext(), ShowLocation.class));
				}
			}
		});

		/** Button "getdata" */
		Button getdata = (Button) findViewById(R.id.getdata);
		getdata.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(view.getContext(), DisplayData.class));
			}
		});

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		this.locMgr.removeUpdates(this.locLst);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.locLst);
	}
}
