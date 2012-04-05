package ch.eonum;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class HealthActivity extends MapActivity
{

	private double latitude;
	private double longitude;
	private LocationManager locMgr;
	private String locProvider;
	
	TextView locationTxt;

	MapView mapView;
	List<Overlay> mapOverlays;
	Drawable drawableLocation, drawableSearchresult;
	MapItemizedOverlay itemizedLocationOverlay, itemizedSearchresultOverlay;
	
	// detect zoom and trigger event
	private Handler handler = new Handler();
	private int zoomLevel = 0, newZoomLevel;
	public static final int zoomCheckingDelay = 500; // in ms
	private Runnable zoomChecker = new Runnable()
	{
	    public void run()
	    {
	    	//ZoomControls mZoom = (ZoomControls) mapView.getZoomControls();;
	    	mapView.getZoomLevel();
	    	
	    	if (zoomLevel == 0)
	    		zoomLevel = mapView.getZoomLevel();
	    	
	    	newZoomLevel = mapView.getZoomLevel();
	    	
	    	if (newZoomLevel != zoomLevel) {
	    		Toast.makeText(getApplicationContext(), "You just zoomed!", Toast.LENGTH_SHORT).show();
	    		zoomLevel = newZoomLevel;
	    	}

	    	
	    	/* TODO
	    	 * 
	    	 * Add code for firing a search event
	    	 * 
	    	 */
	    	
	        handler.removeCallbacks(zoomChecker); // remove the old callback
	        handler.postDelayed(zoomChecker, zoomCheckingDelay); // register a new one
	    }
	};
	
	/* Implement Location Listener */
	private LocationListener locLst = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location)
		{
			HealthActivity.this.latitude = location.getLatitude();
			HealthActivity.this.longitude = location.getLongitude();
			String Text = getString(R.string.location) + ": "
				+ HealthActivity.this.latitude + " : "
				+ HealthActivity.this.longitude;
			Log.i("Location change", HealthActivity.this.latitude + " : " + HealthActivity.this.longitude);
			Toast.makeText(HealthActivity.this.getApplicationContext(), Text, Toast.LENGTH_SHORT).show();

			// Remove other points
			HealthActivity.this.mapOverlays.clear(); // Not visible
			HealthActivity.this.itemizedLocationOverlay.clear(); // Visible

			GeoPoint initGeoPoint = new GeoPoint(
				(int) (location.getLatitude() * 1000000), // (int)
															// (HealthActivity.this.locMgr.getLastKnownLocation(locProvider).getLatitude()
															// * 1000000),
				(int) (location.getLongitude() * 1000000) // (int)
															// (HealthActivity.this.locMgr.getLastKnownLocation(locProvider).getLongitude()
															// * 1000000)
			);
			OverlayItem overlayitem = new OverlayItem(initGeoPoint, "Our Location", "We are here");
			HealthActivity.this.itemizedLocationOverlay.addOverlay(overlayitem);
			HealthActivity.this.mapOverlays.add(HealthActivity.this.itemizedLocationOverlay);

			MapController mc = HealthActivity.this.mapView.getController();
			mc.setZoom(16);
			mc.animateTo(initGeoPoint);

			// Search for results around that point and display them
			AsyncTask<Double, Void, ch.eonum.MedicalLocation[]> queryAnswer = new QueryData(HealthActivity.this).execute(
				location.getLatitude(), location.getLongitude());
			ch.eonum.MedicalLocation[] results = {};
			try
			{
				results = queryAnswer.get();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
			Log.i("Results from server", "Length: " + results.length);
			for (ch.eonum.MedicalLocation point : results)
			{
				Log.i(String.format("GeoPoint is at %f : %f", point.getLocation()[0], point.getLocation()[1]),
					String.format("Draw GeoPoint \"%s (%s)\"", point.getName(), point.getType()));
				GeoPoint matchingResult = new GeoPoint(
					(int) (point.getLocation()[0] * 1000000),
					(int) (point.getLocation()[1] * 1000000)
					);
				OverlayItem matchingOverlayitem = new OverlayItem(matchingResult, point.getName(), point.getType());
				HealthActivity.this.itemizedSearchresultOverlay.addOverlay(matchingOverlayitem);
				HealthActivity.this.mapOverlays.add(HealthActivity.this.itemizedSearchresultOverlay);
			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Toast.makeText(getApplicationContext(), getString(R.string.gpsdisabled), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			Toast.makeText(getApplicationContext(), getString(R.string.gpsenabled), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// This is called when the location provider status alters
			switch (status)
			{
				case LocationProvider.OUT_OF_SERVICE:
				{
					Toast.makeText(getApplicationContext(),
						String.format("Provider %s: Status Changed: Out of Service", provider), Toast.LENGTH_SHORT)
						.show();
					break;
				}
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
				{
					Toast.makeText(getApplicationContext(),
						String.format("Provider %s: Status Changed: Temporarily Unavailable", provider),
						Toast.LENGTH_SHORT).show();
					break;
				}
				case LocationProvider.AVAILABLE:
				{
					Toast.makeText(getApplicationContext(),
						String.format("Provider %s: Status Changed: Available", provider), Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					Toast.makeText(getApplicationContext(), String.format("Provider %s: Unknown status", provider),
						Toast.LENGTH_LONG).show();
			}
		}
	};
	/* End of implemented LocationListener */
	
	private static final String[] CITIES = new CityResolver().getAllCities();

	/**
	 * Main Activity:
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(HealthActivity.class.getName(), "Main Activity started.");
		setContentView(R.layout.main);

		/** AutoCompleteTextView searchforWhere */
		AutoCompleteTextView searchforWhere = (AutoCompleteTextView) findViewById(R.id.searchforWhere);
		ArrayAdapter<String> adapter =
			new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, CITIES);
		searchforWhere.setAdapter(adapter);

		searchforWhere.setOnKeyListener(new View.OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER)
				{
					Toast.makeText(getApplicationContext(),
						"Where is " + ((AutoCompleteTextView) findViewById(R.id.searchforWhere)).getText() + "?",
						Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});

		/** AutoCompleteTextView searchforWhat */
		AutoCompleteTextView searchforWhat = (AutoCompleteTextView) findViewById(R.id.searchforWhat);
		searchforWhat.setOnKeyListener(new View.OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER)
				{
					Toast.makeText(getApplicationContext(),
						"What is " + ((AutoCompleteTextView) findViewById(R.id.searchforWhat)).getText() + "?",
						Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});

		/** MapView */
		this.mapView = (MapView) findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setSatellite(true);

		this.mapOverlays = this.mapView.getOverlays();
		this.drawableLocation = this.getResources().getDrawable(R.drawable.arrow_red);
		this.itemizedLocationOverlay = new MapItemizedOverlay(this.drawableLocation, this);
		this.drawableSearchresult = this.getResources().getDrawable(R.drawable.arrow_green);
		this.itemizedSearchresultOverlay = new MapItemizedOverlay(this.drawableSearchresult, this);

		// Use the LocationManager class to obtain GPS locations
		this.locationTxt = (TextView) findViewById(R.id.locationlabel);
		this.locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Register the listener with the Location Manager to receive location updates
		if (isLocationSensingAvailable())
		{
			this.locMgr.requestLocationUpdates(this.locProvider, 1000, 10, this.locLst);
		}
		else
		{
			Toast.makeText(this, "Failure: No location provider available!", Toast.LENGTH_LONG).show();
		}

		/** Button "location" */
		Button location = (Button) findViewById(R.id.location);
		location.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (!HealthActivity.this.locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setMessage(getString(R.string.askusertoenablenetwork)).setCancelable(true);
					builder.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int id)
							{
								Intent gpsOptionsIntent = new Intent(
									android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(gpsOptionsIntent);
							}
						});
					builder.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int id)
							{
								dialog.cancel();
							}
						});
					AlertDialog alert = builder.create();
					alert.show();
				}
				else
				{
					startActivity(new Intent(view.getContext(), ShowLocation.class));
				}
			}
		});

		/** Button "getdata" */
		Button getdata = (Button) findViewById(R.id.getdata);
		getdata.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startActivity(new Intent(view.getContext(), DisplayData.class));
			}
		});

	}

	@Override
	protected void onStart()
	{
		if (isLocationSensingAvailable())
		{
			this.locMgr.requestLocationUpdates(this.locProvider, 1000, 10, this.locLst);
		}
		else
		{
			Toast.makeText(this, "Failure: No location provider available!", Toast.LENGTH_LONG).show();
		}
		super.onStart();
	}

	@Override
	protected void onPause()
	{
		// zoom handler
		handler.removeCallbacks(zoomChecker);
		
		this.locMgr.removeUpdates(this.locLst);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		// zoom handler
		handler.postDelayed(zoomChecker, zoomCheckingDelay);
		
		if (this.locMgr == null)
		{
			this.locMgr = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
		}
		if (isLocationSensingAvailable())
		{
			this.locMgr.requestLocationUpdates(this.locProvider, 1000, 10, this.locLst);
		}
		else
		{
			Toast.makeText(this, "Failure: No location provider available!", Toast.LENGTH_LONG).show();
		}
		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	/** This criteria will settle for less accuracy, high power, and cost */
	public static Criteria createCoarseCriteria()
	{
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		c.setAltitudeRequired(false);
		c.setBearingRequired(false);
		c.setSpeedRequired(false);
		c.setCostAllowed(true);
		c.setPowerRequirement(Criteria.POWER_HIGH);
		return c;
	}

	/** This criteria needs high accuracy, high power, and cost */
	public static Criteria createFineCriteria()
	{
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		c.setAltitudeRequired(false);
		c.setBearingRequired(false);
		c.setSpeedRequired(false);
		c.setCostAllowed(true);
		c.setPowerRequirement(Criteria.POWER_HIGH);
		return c;
	}

	/** Network connection check */
	private boolean isLocationSensingAvailable()
	{
		String mBestProvider = null;

		String mBestFineProvider = this.locMgr.getBestProvider(createFineCriteria(), true);
		String mBestCoarseProvider = this.locMgr.getBestProvider(createCoarseCriteria(), true);
		// Prefer coarse provider
		mBestProvider = (mBestCoarseProvider == null ? mBestFineProvider : mBestCoarseProvider);
		if (mBestProvider != null)
		{
			this.locProvider = mBestProvider;
			return true;
		}
		return false;
	}
}
