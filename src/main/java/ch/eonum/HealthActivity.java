package ch.eonum;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class HealthActivity extends MapActivity
{

	public static Activity mainActivity;
	private double latitude;
	private double longitude;
	private LocationManager locMgr;
	private String locProvider;
	private Location location = null;
	TextView locationTxt;

	MapView mapView;
	List<Overlay> mapOverlays;
	Drawable drawableLocation, drawableSearchresult;
	MapItemizedOverlay itemizedLocationOverlay, itemizedSearchresultOverlay;

	// Detect zoom and trigger event
	private Handler zoomHandler = new Handler();
	private int zoomLevel = 0, newZoomLevel;
	public static final int zoomCheckingDelay = 500; // Milliseconds
	private Runnable zoomChecker = new Runnable()
	{
		@Override
		public void run()
		{
			// ZoomControls mZoom = (ZoomControls) mapView.getZoomControls();;
			mapView.getZoomLevel();
			if (zoomLevel == 0)
			{
				zoomLevel = mapView.getZoomLevel();
			}

			newZoomLevel = mapView.getZoomLevel();

			if (newZoomLevel != zoomLevel)
			{
				Toast.makeText(getApplicationContext(),
					"You just zoomed " + (newZoomLevel > zoomLevel ? "in" : "out") + "!", Toast.LENGTH_SHORT).show();
				zoomLevel = newZoomLevel;
			}

			/* TODO
			 * call function for firing a search event */

			zoomHandler.removeCallbacks(zoomChecker); // remove the old callback
			zoomHandler.postDelayed(zoomChecker, zoomCheckingDelay); // register a new one
		}
	};

	/* Implement Location Listener */
	private LocationListener locLst = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location newLocation)
		{
			if (isBetterLocation(newLocation, location))
			{
				location = newLocation;
			}

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

			// Draw current location and move to this point
			GeoPoint initGeoPoint = new GeoPoint(
				(int) (location.getLatitude() * 1000000),
				(int) (location.getLongitude() * 1000000)
				);
			OverlayItem overlayitem = new OverlayItem(initGeoPoint, "Our Location", "We are here");
			HealthActivity.this.itemizedLocationOverlay.addOverlay(overlayitem);
			HealthActivity.this.mapOverlays.add(HealthActivity.this.itemizedLocationOverlay);

			MapController mc = HealthActivity.this.mapView.getController();
			mc.setZoom(16);
			mc.animateTo(initGeoPoint);

			// Search for results around that point and display them
			AsyncTask<Double, Void, ch.eonum.MedicalLocation[]> queryAnswer =
				new QueryData().execute(location.getLatitude(), location.getLongitude());
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

			// Draw results to map
			Log.i(this.getClass().getName(), "Draw "+results.length+" results to map");
			Log.i("GeoPoint", "Start drawing");
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
			Log.i("GeoPoint", "Finished drawing");
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
						getString(R.string.provider_status_out_of_service, provider), Toast.LENGTH_SHORT).show();
					break;
				}
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
				{
					Toast.makeText(getApplicationContext(),
						getString(R.string.provider_status_temp_univailable, provider),
						Toast.LENGTH_SHORT).show();
					break;
				}
				case LocationProvider.AVAILABLE:
				{
					Toast.makeText(getApplicationContext(),
						getString(R.string.provider_status_available, provider), Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					Toast.makeText(getApplicationContext(), getString(R.string.provider_status_unknown, provider),
						Toast.LENGTH_LONG).show();
			}
		}
	};
	/* End of implemented LocationListener */

	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final String[] CITIES = new CityResolver().getAllCities();
	private static String[] TYPES;

	/**
	 * Main Activity:
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(this.getClass().getName(), "Main Activity started.");
		setContentView(R.layout.main);
		mainActivity = this;

		/** AutoCompleteTextView searchforWhere */
		AutoCompleteTextView searchforWhere = (AutoCompleteTextView) findViewById(R.id.searchforWhere);
		ArrayAdapter<String> adapterWhere =
			new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, CITIES);
		searchforWhere.setAdapter(adapterWhere);

		// Item from autocompletion selected
		searchforWhere.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String cityString = String.valueOf(((AutoCompleteTextView) findViewById(R.id.searchforWhere)).getText());
				City city = new CityResolver().getCoordinates(cityString);
				GeoPoint cityPoint = new GeoPoint(
					(int) (city.getLocation()[0] * 1000000),
					(int) (city.getLocation()[1] * 1000000));
				MapController mc = HealthActivity.this.mapView.getController();
				mc.setZoom(16);
				mc.animateTo(cityPoint);
			}
		});

		// Enter key pressed
		searchforWhere.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
				{
					String citySearchString = String.valueOf(((AutoCompleteTextView) findViewById(R.id.searchforWhere))
						.getText());
					Geocoder geocoder = new Geocoder(HealthActivity.this, HealthActivity.this.getResources()
						.getConfiguration().locale);
					List<Address> resultsList = null;
					String error = null;
					try
					{
						resultsList = geocoder.getFromLocationName(citySearchString, 5);
						Log.i("resultsList", resultsList.toString());
					}
					catch (IOException e)
					{
						error = e.getMessage();
						e.printStackTrace();
					}
					if (resultsList == null || resultsList.isEmpty())
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setCancelable(true);
						builder.setTitle(getString(R.string.noresults));
						builder.setMessage(error);
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
						return false;
					}
					Double[] coordinates = new Double[2];
					coordinates[0] = resultsList.get(0).getLatitude();
					coordinates[1] = resultsList.get(0).getLongitude();
					if (resultsList.size() > 2)
					{
						String ambiguousString = "";
						for (int i = 0; i < resultsList.size(); i++)
						{
							ambiguousString += (i + 1) + ": " + resultsList.get(i).getAddressLine(0) + ", "
								+ resultsList.get(i).getAddressLine(1) + ", "
								+ resultsList.get(i).getAddressLine(2) + "\n";
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setTitle(resultsList.size() + " ambiguous results");
						builder.setMessage(ambiguousString);
						builder.setIcon(android.R.drawable.ic_dialog_info);
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
						return false;
					}

					GeoPoint cityPoint = new GeoPoint(
						(int) (coordinates[0] * 1000000),
						(int) (coordinates[1] * 1000000));
					MapController mc = HealthActivity.this.mapView.getController();
					mc.setZoom(16);
					mc.animateTo(cityPoint);
					return true;
				}
				return false;
			}
		});

		/** AutoCompleteTextView searchforWhat */
		AsyncTask<Void, Void, String[]> typesResolved = new TypeResolver().execute();
		try
		{
			TYPES = typesResolved.get();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AutoCompleteTextView searchforWhat = (AutoCompleteTextView) findViewById(R.id.searchforWhat);
		ArrayAdapter<String> adapterWhat =
			new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, TYPES);
		searchforWhat.setAdapter(adapterWhat);
		searchforWhat.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
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
		//this.locationTxt = (TextView) findViewById(R.id.locationlabel);
		this.locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Register the listener in onResume()

//		/** Button "location" */
//		Button location = (Button) findViewById(R.id.location);
//		location.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				if (!HealthActivity.this.locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER))
//				{
//					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//					builder.setMessage(getString(R.string.askusertoenablenetwork)).setCancelable(true);
//					builder.setPositiveButton(android.R.string.yes,
//						new DialogInterface.OnClickListener()
//						{
//							@Override
//							public void onClick(DialogInterface dialog, int id)
//							{
//								Intent gpsOptionsIntent = new Intent(
//									android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//								startActivity(gpsOptionsIntent);
//							}
//						});
//					builder.setNegativeButton(android.R.string.no,
//						new DialogInterface.OnClickListener()
//						{
//							@Override
//							public void onClick(DialogInterface dialog, int id)
//							{
//								dialog.cancel();
//							}
//						});
//					AlertDialog alert = builder.create();
//					alert.show();
//				}
//				else
//				{
//					startActivity(new Intent(view.getContext(), ShowLocation.class));
//				}
//			}
//		});

//		/** Button "getdata" */
//		Button getdata = (Button) findViewById(R.id.getdata);
//		getdata.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				startActivity(new Intent(view.getContext(), DisplayData.class));
//			}
//		});

		
		/** ImageButton "getposition" */
		ImageButton getposition = (ImageButton) findViewById(R.id.getposition);
		getposition.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				
				/* TODO
				 * call function for firing a search event */
				Toast.makeText(getApplicationContext(), "You just pushed the 'My position' button.", Toast.LENGTH_SHORT).show();
			}
		});

		/** Button "search" */
		Button search = (Button) findViewById(R.id.search);
		search.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO: Call function for firing a search event
				Toast.makeText(getApplicationContext(), "You just pushed the 'Search' button.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		// zoom handler
		zoomHandler.removeCallbacks(zoomChecker);

		this.locMgr.removeUpdates(this.locLst);
		this.locMgr = null;
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// zoom handler
		zoomHandler.postDelayed(zoomChecker, zoomCheckingDelay);

		if (this.locMgr == null)
		{
			this.locMgr = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
		}
		// Register the listener with the Location Manager to receive location updates
		// Moved from onCreate() here to avoid displaying the dialogue multiple times
		locationUpdateOrNetworkFail();
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

	/**
	 * Perform network check and alert user if nothing works
	 * Make use of {@link #isLocationSensingAvailable()} to test for available location update providers.
	 * If one was found, everything is fine. Updates are requested and the method returns.
	 * If this is not the case, the user is informed and assisted to fix the problem by displaying the network
	 * and location configuration activity
	 */
	private void locationUpdateOrNetworkFail()
	{
		if (isLocationSensingAvailable())
		{
			this.locMgr.requestLocationUpdates(this.locProvider, 1000, 10, this.locLst);
			return;
		}

		Toast.makeText(this, getString(R.string.fail_no_provider), Toast.LENGTH_LONG).show();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle(getString(R.string.gpsdisabled));
		builder.setMessage(getString(R.string.askusertoenablenetwork));
		builder.setIcon(android.R.drawable.ic_dialog_info);
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

	/**
	 * Location provider search
	 * Searches for available location providers and chooses one based on {@link createCoarseCriteria} and
	 * {@link createFineCriteria}
	 * 
	 * @return {@code True} if at least one provider was found, {@code False} if no reliable provider for
	 *         location updates is available
	 */
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
		// Even fine provider is not available
		return false;
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation)
	{
		if (currentBestLocation == null)
		{
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;
			// If the new location is more than two minutes older, it must be worse
		}
		else if (isSignificantlyOlder)
		{
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)
		{
			return true;
		}
		else if (isNewer && !isLessAccurate)
		{
			return true;
		}
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
		{
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

}
