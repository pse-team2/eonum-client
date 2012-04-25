package ch.eonum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
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
	
	private double lowerLeftLatitude;
	private double lowerLeftLongitude;
	private double upperRightLatitude;
	private double upperRightLongitude;
	
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
				
				lowerLeftLatitude = mapView.getMapCenter().getLatitudeE6() - mapView.getLatitudeSpan()/2; 
				lowerLeftLongitude = mapView.getMapCenter().getLongitudeE6() - mapView.getLongitudeSpan()/2; 
				
				upperRightLatitude = mapView.getMapCenter().getLatitudeE6() + mapView.getLatitudeSpan()/2; 
				upperRightLongitude = mapView.getMapCenter().getLongitudeE6() + mapView.getLongitudeSpan()/2;

				lowerLeftLatitude /= 1000000;
				lowerLeftLongitude /= 1000000;
				upperRightLatitude /= 1000000;
				upperRightLongitude /= 1000000;
				
				launchAndDrawResults();
			}
			
			zoomHandler.removeCallbacks(zoomChecker); // remove the old callback
			zoomHandler.postDelayed(zoomChecker, zoomCheckingDelay); // register a new one
		}
	};

	private LocationListener locLst = new HealthLocationListener();
	/* Implement Location Listener */
	/*
	private LocationListener locLst = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location newLocation)
		{
			if (isBetterLocation(newLocation, location))
			{
				location = newLocation;
			}

			// Draw current location to map
			GeoPoint initGeoPoint = drawMyLocation();

			// Search for results around that point and display them
			MedicalLocation[] results = launchSearchFromCurrentLocation();

			Log.i(this.getClass().getName(), "Draw "+results.length+" results to map");
			drawSearchResults(results);
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
	*/
	/* End of implemented LocationListener */
	
//	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final String[] CITIES = new CityResolver().getAllCities();
	private static String[] TYPES;

	protected Location getLocation()
	{
		return this.location;
	}

	protected void setLocation(Location location)
	{
		this.location = location;
	}

	protected MapView getMapView()
	{
		return this.mapView;
	}

	/**
	 * Main Activity:
	 * Called once when the activity is first created.
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
		/*
		searchforWhere.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				final KeyEvent dispatchedKeyEvent = event;
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

					// Inform user if the server returned no results
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

					// Ask user about ambiguous results
					if (resultsList.size() > 2)
					{
						final String[] ambiguousList = new String[resultsList.size()];
						for (int i = 0; i < resultsList.size(); i++)
						{
							ambiguousList[i] = resultsList.get(i).getAddressLine(0) + ", "
								+ resultsList.get(i).getAddressLine(1);
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setTitle(resultsList.size() + " ambiguous results");
						builder.setItems(ambiguousList, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int item)
							{
								AutoCompleteTextView searchForWhat = (AutoCompleteTextView) findViewById(R.id.searchforWhere);
								searchForWhat.setText(ambiguousList[item]);
								searchForWhat.dispatchKeyEvent(dispatchedKeyEvent);
							}
						});
						builder.setIcon(android.R.drawable.ic_dialog_info);
						builder.setNeutralButton(android.R.string.cancel, new OnClickListener()
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

					// Everything went fine, go to the location
					Double[] coordinates = new Double[2];
					coordinates[0] = resultsList.get(0).getLatitude();
					coordinates[1] = resultsList.get(0).getLongitude();
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
		*/

		/** AutoCompleteTextView searchforWhat */
		AsyncTask<Void, Void, String[]> typesResolved = new TypeResolver().execute();
		try
		{
			TYPES = typesResolved.get();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			// Restore the interrupted status
			Thread.currentThread().interrupt();
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
		/*
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
		*/

		/** MapView */
		this.mapView = (MapView) findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setSatellite(true);

		this.mapOverlays = this.mapView.getOverlays();
		this.drawableLocation = this.getResources().getDrawable(R.drawable.pin_red);
		this.drawableSearchresult = this.getResources().getDrawable(R.drawable.pin_green);
		this.itemizedLocationOverlay = new MapItemizedOverlay(this.drawableLocation, this);
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
				drawMyLocation(16);
			}
		});

		/** Button "search" */
		Button search = (Button) findViewById(R.id.search);
		search.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Editable searchWhere = ((AutoCompleteTextView) findViewById(R.id.searchforWhere)).getText();
				Editable searchWhat = ((AutoCompleteTextView) findViewById(R.id.searchforWhat)).getText();

				if (zoomLevel < 4)
				{
					drawMyLocation(16);
				}
				MedicalLocation[] results = launchUserDefinedSearch(searchWhere.toString(), searchWhat.toString());
				if(results != null)
				{
					if(results.length != 0)
					{
						MedicalLocation[] filteredResults = filterResults(results);
						drawSearchResults(filteredResults);
					}
					// There is no need to display an error message in case of an empty result list
					// as the search method already does this.
				}
			}
		});

		Log.i(this.getClass().getName(), "Main Activity created.");
	}

	
	public void launchAndDrawResults() {
		if (zoomLevel < 4) {
			drawMyLocation(16);
		}
		
		MedicalLocation[] answer;
		if (this.lowerLeftLatitude == 0)
 		{
			Log.i(this.getClass().getName() + ": launchUserDefinedSearch", 
					"Found no corners, querying for Long&Lat.");
			
			// TextView searchForWhere was empty, use current location
			answer = sendDataToServer(this.latitude, this.longitude);
		}
		else {
			Log.i(this.getClass().getName() + ": launchUserDefinedSearch", 
					"Querying for map rectangle.");
			answer = sendDataToServer(this.lowerLeftLatitude, this.lowerLeftLongitude, 
					this.upperRightLatitude, this.upperRightLongitude);
 		}
		MedicalLocation[] filteredResults = filterResults(answer);

		drawSearchResults(filteredResults);
	}

	/**
	 * Every time, the activity is shown.
	 */
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.i(this.getClass().getName(), "Run onStart()");
	}

	/**
	 * Every time, the user returns to the activity.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();

		Log.i(this.getClass().getName(), "Run onResume()");

		// Zoom handler
		zoomHandler.postDelayed(zoomChecker, zoomCheckingDelay);

		if (this.locMgr == null)
		{
			this.locMgr = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
		}
		// Register the listener with the Location Manager to receive location updates
		// Moved from onCreate() here to avoid displaying the dialogue multiple times
		locationUpdateOrNetworkFail();
	}

	/**
	 * Activity loses focus.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		Log.i(this.getClass().getName(), "Run onPause()");

		// zoom handler
		zoomHandler.removeCallbacks(zoomChecker);

		this.locMgr.removeUpdates(this.locLst);
		this.locMgr = null;
	}

	/**
	 * Activity is no longer visible.
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		Log.i(this.getClass().getName(), "Run onStop()");

		itemizedLocationOverlay.clear();
		itemizedSearchresultOverlay.clear();
		mapView.invalidateDrawable(drawableLocation);
		mapView.invalidateDrawable(drawableSearchresult);
		mapView.invalidate();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	protected MedicalLocation[] launchSearchFromCurrentLocation()
	{
		return sendDataToServer(location.getLatitude(), location.getLongitude());
	}

	/**
	 * Reads input from the two TextView {@code searchforWhere} and {@code searchforWhat}.
	 * Handles user interaction in case of an error.
	 * If an error occurred, the method quits with a {@code null} value.
	 * @param where Parsed input from TextView {@code searchforWhere}.
	 * @param what Parsed input from TextView {@code searchforWhat}
	 * @return Filtered results or {@code null} indicating an error.
	 */
	protected MedicalLocation[] launchUserDefinedSearch(String where, String what)
	{
		double searchAtLatitude, searchAtLongitude;
		Log.i(this.getClass().getName() + ": launchUserDefinedSearch", "Where: "+where+", What: "+what);
		
		
		if (where.length() != 0)
		{
			Geocoder geocoder = new Geocoder(HealthActivity.this, HealthActivity.this.getResources()
				.getConfiguration().locale);
			List<Address> resultsList = null;
			String error = null;
			try
			{
				resultsList = geocoder.getFromLocationName(where, 5);
			}
			catch (IOException e)
			{
				error = e.getMessage();
				e.printStackTrace();
			}

			Log.i(this.getClass().getName() + ": launchUserDefinedSearch", "resultsList: "+resultsList);
			// Inform user if the server returned no results
			if (resultsList == null || resultsList.isEmpty())
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
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
				return null;
			}

			// Ask user about ambiguous results
			if (resultsList.size() > 2)
			{
				final String[] ambiguousList = new String[resultsList.size()];
				for (int i = 0; i < resultsList.size(); i++)
				{
					ambiguousList[i] = resultsList.get(i).getAddressLine(0) + ", "
						+ resultsList.get(i).getAddressLine(1);
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
				builder.setTitle(resultsList.size() + " ambiguous results");
				builder.setItems(ambiguousList, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int item)
					{
						AutoCompleteTextView searchForWhat = (AutoCompleteTextView) findViewById(R.id.searchforWhere);
						searchForWhat.setText(ambiguousList[item]);
						Button search = (Button) findViewById(R.id.search);
						search.performClick();
					}
				});
				builder.setIcon(android.R.drawable.ic_dialog_info);
				builder.setNeutralButton(android.R.string.cancel, new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return null;
			}

			// Everything went fine, go to the location
			searchAtLatitude = resultsList.get(0).getLatitude();
			searchAtLongitude = resultsList.get(0).getLongitude();
			GeoPoint cityPoint = new GeoPoint(
				(int) (searchAtLatitude * 1000000),
				(int) (searchAtLongitude * 1000000));
			MapController mc = HealthActivity.this.mapView.getController();
			mc.setZoom(16);
			mc.animateTo(cityPoint);
		}
		else
		{
			// TextView searchForWhere was empty, use current location
			searchAtLatitude = this.latitude;
			searchAtLongitude = this.longitude;
		}
		ch.eonum.MedicalLocation[] answer = sendDataToServer(searchAtLatitude, searchAtLongitude);
		ArrayList<MedicalLocation> results = new ArrayList<MedicalLocation>(Arrays.asList(answer));

		// Do not filter anything if TextView searchForWhat was empty
		if(what.length() != 0)
		{
			for (MedicalLocation r : new ArrayList<MedicalLocation>(results))
			{
				if (!r.getType().equals(what))
				{
					results.remove(r);
				}
			}
		}

		return results.toArray(new MedicalLocation[] {});
	}

	private MedicalLocation[] filterResults(MedicalLocation[] results)
	{
		// Calculate distance of all result points from the current displayed position.
		for (MedicalLocation res : results)
		{
			res.setDistance(mapView.getMapCenter().getLatitudeE6() / 1000000,
				mapView.getMapCenter().getLongitudeE6() / 1000000);
		}

		// Sort the list, the higher the index, the longer the distance.
		Arrays.sort(results);
		
		// Continue only with the nearest MAX_RESULTS results
		final int MAX_RESULTS = 20;
		MedicalLocation[] filteredResults = new MedicalLocation[Math.min(MAX_RESULTS, results.length)];

		for (int i = 0; i < Math.min(MAX_RESULTS, results.length); i++)
		{
			filteredResults[i] = results[i];
			Log.i(this.getClass().getName(), "Dist: " + results[i].getDistance());
		}
		return filteredResults;
	}

	private MedicalLocation[] sendDataToServer(double lowerLeftLatitude,
			double lowerLeftLongitude, double upperRightLatitude,
			double upperRightLongitude) {
		
		// Search for results around that point
		ch.eonum.MedicalLocation[] results = {};
		AsyncTask<Double, Void, ch.eonum.MedicalLocation[]> queryAnswer =
			new QueryData().execute(lowerLeftLatitude, lowerLeftLongitude, 
					upperRightLatitude, upperRightLongitude);
		try
		{
			results = queryAnswer.get();
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
		return results;
	}

	/**
	 * Sends data to server and returns results as list.
	 */
	private MedicalLocation[] sendDataToServer(double latitude, double longitude)
	{
		// Search for results around that point
		ch.eonum.MedicalLocation[] results = {};
		AsyncTask<Double, Void, ch.eonum.MedicalLocation[]> queryAnswer =
			new QueryData().execute(latitude, longitude);
		try
		{
			results = queryAnswer.get();
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
		return results;
	}

	protected void drawMyLocation(int zoomLevel)
	{
		HealthActivity.this.latitude = location.getLatitude();
		HealthActivity.this.longitude = location.getLongitude();
		String Text = getString(R.string.location) + ": "
			+ HealthActivity.this.latitude + " : "
			+ HealthActivity.this.longitude;
		Log.i(this.getClass().getName() + ": drawMyLocation", HealthActivity.this.latitude + " : " + HealthActivity.this.longitude);
		Toast.makeText(HealthActivity.this.getApplicationContext(), Text, Toast.LENGTH_SHORT).show();

		// Remove other points
		HealthActivity.this.itemizedLocationOverlay.clear();

		// Draw current location
		GeoPoint initGeoPoint = new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000));
		OverlayItem overlayitem = new OverlayItem(initGeoPoint, "Our Location", "We are here");
		HealthActivity.this.itemizedLocationOverlay.addOverlay(overlayitem);
		HealthActivity.this.mapOverlays.add(HealthActivity.this.itemizedLocationOverlay);

		// Go there
		MapController mc = ((HealthActivity) HealthActivity.mainActivity).getMapView().getController();
		mc.setZoom(zoomLevel);
		mc.animateTo(initGeoPoint);
	}

	protected void drawSearchResults(MedicalLocation[] results)
	{
		// Remove other points
		HealthActivity.this.itemizedSearchresultOverlay.clear();

		// Draw results to map
		Log.i(this.getClass().getName() + ": drawSearchResults", "Draw "+results.length+" results to map");
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

	/** This criteria will settle for less accuracy, high power, and cost */
	private static Criteria createCoarseCriteria()
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
	private static Criteria createFineCriteria()
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
			// 60000 = 1min
			// 10 = 100m
			this.locMgr.requestLocationUpdates(this.locProvider, 60000, 10, this.locLst);
			Toast.makeText(HealthActivity.this, "Debug message:\n" +
				"If running on an emulator:\nSend location fix in DDMS to trigger location update.",
				Toast.LENGTH_SHORT).show();
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
	/*
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
	*/

	/** Checks whether two providers are the same */
	/*
	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	*/

}
