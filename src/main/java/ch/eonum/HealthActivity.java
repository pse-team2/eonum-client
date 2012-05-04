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
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class HealthActivity extends MapActivity implements HealthMapView.OnChangeListener
{
	/* Activity */
	public static Activity mainActivity;

	/* Other static variables */
	private static final String[] CITIES = CityResolver.getInstance().getAllCities();
	private static String[] TYPES;

	/* Location */
	/**
	 * Variables indicating the last known physical location of the user.
	 * The value {@link #location} is provided by an external source to
	 * the {@link HealthLocationListener#onLocationChanged(Location)} method.
	 * From there these variables are updated using using the {@link #setLocation(Location)} method.
	 */
	private double latitude, longitude;
	/**
	 * Variable that is provided from an external source to
	 * the {@link HealthLocationListener#onLocationChanged(Location)} method.
	 * From there the method {@link #setLocation(Location)} is used to update 
	 * the variables {@link #latitude} and {@link #longitude} which are dependent from this value.
	 */
	private Location location = null;
	/** Variables involved in the process of getting location updates. */
	private LocationManager locMgr;
	private String locProvider;
	private LocationListener locLst = new HealthLocationListener();
	/**
	 * Involved in location updates to help decide if location updates should cause
	 * {@link #drawMyLocation(int)} to run.
	 */
	public static boolean userRequestedMyLocation = true;

	/* Location Listener */
	/** Constants defining when a location update shall be delivered. */
	private final int LOCATION_UPDATE_AFTER_SECONDS = 5;
	private final float LOCATION_UPDATE_AFTER_DISTANCE = 100;

	/* Zoom */
	private int currentZoomLevel;

	/* Map */
	HealthMapView mapView;
	List<Overlay> mapOverlays;
	Drawable drawableLocation, drawableSearchresult;
	MapItemizedOverlay itemizedLocationOverlay, itemizedSearchresultOverlay;

	protected Location getLocation()
	{
		return this.location;
	}

	protected void setLocation(Location location)
	{
		this.location = location;
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
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
				City city = CityResolver.getInstance().getCoordinates(cityString);
				GeoPoint cityPoint = new GeoPoint(
					(int) (city.getLocation()[0] * 1000000),
					(int) (city.getLocation()[1] * 1000000));
				MapController mc = HealthActivity.this.mapView.getController();
				mc.setZoom(16);
				mc.animateTo(cityPoint);
			}
		});

		/** AutoCompleteTextView searchforWhat */
		TypeResolver typesResolved = TypeResolver.getInstance();
		TYPES = typesResolved.getAllCategories();

		AutoCompleteTextView searchforWhat = (AutoCompleteTextView) findViewById(R.id.searchforWhat);
		ArrayAdapter<String> adapterWhat =
			new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, TYPES);
		searchforWhat.setAdapter(adapterWhat);

		/** MapView */
		this.mapView = (HealthMapView) findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setSatellite(true);
		// Add listener
		this.mapView.setOnChangeListener(this);

		this.mapOverlays = this.mapView.getOverlays();
		this.drawableLocation = this.getResources().getDrawable(R.drawable.pin_red);
		this.drawableSearchresult = this.getResources().getDrawable(R.drawable.pin_green);
		this.itemizedLocationOverlay = new MapItemizedOverlay(this.drawableLocation, this);
		this.itemizedSearchresultOverlay = new MapItemizedOverlay(this.drawableSearchresult, this);

		// Use the LocationManager class to obtain GPS locations
		this.locMgr = (LocationManager) this.getSystemService(LOCATION_SERVICE);

		/** ImageButton "getposition" */
		ImageButton getposition = (ImageButton) findViewById(R.id.getposition);
		getposition.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Logger.log("Location button pressed.");
				userRequestedMyLocation = true;
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
				userRequestedMyLocation = false;
				Editable searchWhere = ((AutoCompleteTextView) findViewById(R.id.searchforWhere)).getText();
				Editable searchWhat = ((AutoCompleteTextView) findViewById(R.id.searchforWhat)).getText();
				Logger.log("Search button pressed. where: " + searchWhere + ", what: " + searchWhat);

				if (currentZoomLevel < 4)
				{
					drawMyLocation(16);
				}
				MedicalLocation[] results = launchUserDefinedSearch(searchWhere.toString(), searchWhat.toString());
				if (results != null)
				{
					if (results.length != 0)
					{
						MedicalLocation[] filteredResults = filterResults(results);
						drawSearchResults(filteredResults);
					}
					// There is no need to display an error message in case of an empty result list
					// as the search method already does this.
				}
				// getApplicationContext().getSystemService(LOCATION_SERVICE);
				InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(
					INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});

		Log.i(this.getClass().getName(), "Main Activity created.");
	}

	/**
	 * Every time, the activity is shown.
	 */
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.i(this.getClass().getName(), "Run onStart()");

		Logger.init();
		Logger.log("App started.");
	}

	/**
	 * Every time, the user returns to the activity.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		Logger.log("App resumed.");

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
		Logger.log("App paused.");

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
		Logger.log("App stopped.");

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

	/**
	 * This method is triggered every time the user moves onwards on the map.
	 * It calculates the new location and visible map rectangle.
	 * After this it launches a new search from the targeted location.
	 */
	@Override
	public void onChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom)
	{
		this.currentZoomLevel = newZoom;
		// Do not call drawMyLocation as this resets the display
		// Editable searchWhat = ((AutoCompleteTextView) findViewById(R.id.searchforWhat)).getText();
		// MedicalLocation[] results = launchUserDefinedSearch("",
		// searchWhat.toString());//launchSearchFromCurrentLocation(false);

		MedicalLocation[] results = launchSearchFromCurrentLocation(false);
		MedicalLocation[] filteredResults = filterResults(results);
		drawSearchResults(filteredResults);
		// Do not display error messages if there were no results returned
		// because we do not want to disturb the user moving around the map.
	}

	/**
	 * Initial search after launch of the application.
	 */
	public void launchInitialSearch()
	{
		MedicalLocation[] results = launchSearchFromCurrentLocation(true);
		MedicalLocation[] filteredResults = filterResults(results);
		drawSearchResults(filteredResults);
	}

	/**
	 * Depending of the input, this method launches a search with different arguments for the server.
	 * A search is launched either with the coordinates where the MyLocation marker is placed
	 * or with the coordinates where the user has just navigated.
	 * This method assumes that no user input is involved here and as such does no input checking
	 * and no error handling either.
	 * If user input has to be taken into consideration, {@link #launchUserDefinedSearch(String, String)}
	 * should be used instead.
	 * 
	 * @param equalsPhysicalLocation
	 *            Set {@code true} if there should be searched for results near the MyLocation marker
	 *            or {@code false} if it should be the location where the user has just navigated.
	 * @return Filtered results.
	 */
	protected MedicalLocation[] launchSearchFromCurrentLocation(boolean equalsPhysicalLocation)
	{
		MedicalLocation[] answer;
		double lowerLeftLatitude = mapView.getMapCenter().getLatitudeE6() - mapView.getLatitudeSpan() / 2;
		double lowerLeftLongitude = mapView.getMapCenter().getLongitudeE6() - mapView.getLongitudeSpan() / 2;

		double upperRightLatitude = mapView.getMapCenter().getLatitudeE6() + mapView.getLatitudeSpan() / 2;
		double upperRightLongitude = mapView.getMapCenter().getLongitudeE6() + mapView.getLongitudeSpan() / 2;

		if (equalsPhysicalLocation
			|| (lowerLeftLatitude == 0 || lowerLeftLongitude == 0 || upperRightLatitude == 0 || upperRightLongitude == 0))
		{
			Log.i(this.getClass().getName() + ": launchUserDefinedSearch", "Found no corners, querying for Long&Lat.");
			answer = sendDataToServer(this.latitude, this.longitude);
		}
		else
		{
			lowerLeftLatitude /= 1000000;
			lowerLeftLongitude /= 1000000;
			upperRightLatitude /= 1000000;
			upperRightLongitude /= 1000000;

			Log.i(this.getClass().getName() + ": launchUserDefinedSearch", "Querying for map rectangle.");
			answer = sendDataToServer(lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
		}

		return answer;
	}

	/**
	 * Reads two strings as input and passes them to the server in order to get results.
	 * Assumes they are coming from the two TextViews {@code searchforWhere} and {@code searchforWhat} and no
	 * other values should be taken into consideration.
	 * If this is not the case, {@link #launchSearchFromCurrentLocation(boolean)} might be more suitable.
	 * Handles user interaction in case of an error.
	 * If an error occurred, the method quits with a {@code null} value.
	 * 
	 * @param where
	 *            Parsed input from TextView {@code searchforWhere}.
	 * @param what
	 *            Parsed input from TextView {@code searchforWhat}
	 * @return Filtered results or {@code null} indicating an error.
	 */
	protected MedicalLocation[] launchUserDefinedSearch(String where, String what)
	{
		double searchAtLatitude, searchAtLongitude;
		Log.i(this.getClass().getName() + ": launchUserDefinedSearch", "Where: " + where + ", What: " + what);

		// Evaluate TextView searchforWhere
		if (where.length() != 0)
		{
			Geocoder geocoder = new Geocoder(this, getResources().getConfiguration().locale);
			List<Address> resultsList = null;
			String error = null;
			try
			{
				resultsList = geocoder.getFromLocationName(where + ", Schweiz", 5);
			}
			catch (IOException e)
			{
				error = e.getMessage();
				e.printStackTrace();
			}

			Log.i(this.getClass().getName() + ": launchUserDefinedSearch", "resultsList: " + resultsList);
			// Inform user if the server returned no results
			if (resultsList == null || resultsList.isEmpty())
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(String.format(getString(R.string.ambiguousresults), resultsList.size()));
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
				InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(
					INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);

				return null;
			}

			// Everything went fine, go to the location
			searchAtLatitude = resultsList.get(0).getLatitude();
			searchAtLongitude = resultsList.get(0).getLongitude();
			GeoPoint cityPoint = new GeoPoint(
				(int) (searchAtLatitude * 1000000),
				(int) (searchAtLongitude * 1000000));
			MapController mc = this.mapView.getController();
			mc.setZoom(16);
			mc.animateTo(cityPoint);
		}
		else
		{
			// TextView searchForWhere was empty, use current location
			searchAtLatitude = this.latitude;
			searchAtLongitude = this.longitude;
		}

		MedicalLocation[] answer = new MedicalLocation[] {};

		// Evaluate TextView searchForWhat
		if (what.length() != 0)
		{
			String whatValue = TypeResolver.getKeyByValue(what);
			if(whatValue == null)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(String.format(getString(R.string.no_valid_category), what));
				builder.setMessage(R.string.choose_category_from_list);
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

			answer = sendDataToServer(searchAtLatitude, searchAtLongitude, whatValue);
		}
		else
		{
			answer = sendDataToServer(searchAtLatitude, searchAtLongitude);
		}

		ArrayList<MedicalLocation> results = new ArrayList<MedicalLocation>(Arrays.asList(answer));

		return results.toArray(new MedicalLocation[] {});
	}

	/**
	 * Limits number of results to the amount defined in the MAX_RESULTS constant.
	 * 
	 * @param results
	 *            Result list from the server.
	 * @return Result list shortened to the given amount.
	 */
	private MedicalLocation[] filterResults(MedicalLocation[] results)
	{
		// Calculate distance of all result points from the current displayed position.
		for (MedicalLocation res : results)
		{
			double lat = (double) (mapView.getMapCenter().getLatitudeE6()) / 1000000;
			double lng = (double) (mapView.getMapCenter().getLongitudeE6()) / 1000000;
			res.setDistance(lat, lng);
		}

		// Sort the list, the higher the index, the longer the distance.
		Arrays.sort(results);

		// Continue only with the nearest MAX_RESULTS results
		final int MAX_RESULTS = 50;
		MedicalLocation[] filteredResults = new MedicalLocation[Math.min(MAX_RESULTS, results.length)];

		for (int i = 0; i < Math.min(MAX_RESULTS, results.length); i++)
		{
			filteredResults[i] = results[i];
			Log.i(this.getClass().getName(), "Dist: " + results[i].getDistance());
		}
		return filteredResults;
	}

	/**
	 * With rectangle (2 coordinate points):
	 * Send data to server and returns results as list.
	 */
	private MedicalLocation[] sendDataToServer(double lowerLeftLatitude,
		double lowerLeftLongitude, double upperRightLatitude,
		double upperRightLongitude)
	{
		// Search for results around that point
		Log.i(this.getClass().getName(), "Map rectangle to query: " + lowerLeftLatitude + "/" + lowerLeftLongitude+ "," + upperRightLatitude + "/" + upperRightLongitude);
		// Send query to server
		HTTPRequest request = new HTTPRequest(lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
		String resultString = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			resultString = httpTask.get();
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
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserializeLocations(resultString);
	}

	/**
	 * With location (1 coordinate point):
	 * Send data to server and returns results as list.
	 */
	private MedicalLocation[] sendDataToServer(double latitude, double longitude)
	{
		// Search for results around that point
		double d = 0.05;
		Log.i(this.getClass().getName(), "Single location to query: " + latitude + " : " + longitude);
		// Send query to server
		HTTPRequest request = new HTTPRequest(latitude-d, longitude-d, latitude+d, longitude+d);
		String resultString = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			resultString = httpTask.get();
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
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserializeLocations(resultString);
	}

	private MedicalLocation[] sendDataToServer(double latitude, double longitude, String category)
	{
		// Search for results around that point
		double d = 0.05;
		Log.i(this.getClass().getName(), "Location to query: " + latitude + " : " + latitude + ", limited to category " + category);
		// Send query to server
		HTTPRequest request = new HTTPRequest(latitude-d, longitude-d, latitude+d, longitude+d, category);
		String resultString = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			resultString = httpTask.get();
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
		Log.i(this.getClass().getName(), "Size of results in queryServer: " + resultString.length());
		// Parse results
		JSONParser parser = new JSONParser();
		return parser.deserializeLocations(resultString);
	}

	/**
	 * Draws the actual location
	 * 
	 * @param zoomLevel
	 *            Google API zoom level, ranging from 1 (far away) to 16 (near)
	 */
	protected void drawMyLocation(int zoomLevel)
	{
		// Avoid NullPointerException in case location is not yet available
		if (this.location == null)
		{
			return;
		}

		this.latitude = this.location.getLatitude();
		this.longitude = this.location.getLongitude();
		// String Text = getString(R.string.location) + ": " + this.latitude + " : " + this.longitude;
		// Toast.makeText(this, Text, Toast.LENGTH_SHORT).show();
		Log.i(this.getClass().getName() + ": drawMyLocation", this.latitude + " : " + this.longitude);

		// Remove other points
		this.itemizedLocationOverlay.clear();

		// Get address from current location
		Address myLocationAddress;
		try
		{
			Geocoder geocoder = new Geocoder(this);
			myLocationAddress = geocoder.getFromLocation(this.latitude, this.longitude, 1).get(0);
		}
		catch(IOException e)
		{
			Address unknownAddress = new Address(getResources().getConfiguration().locale);
			myLocationAddress=unknownAddress;
		}
		String myAddressDescription = "";
		for (int i = 0; i <= myLocationAddress.getMaxAddressLineIndex(); i++)
		{
			myAddressDescription += myLocationAddress.getAddressLine(i) + (i + 1 <= myLocationAddress.getMaxAddressLineIndex() ? "\n" : "");
		}
		// Draw current location
		GeoPoint initGeoPoint = new GeoPoint((int) (this.latitude * 1000000), (int) (this.longitude * 1000000));
		OverlayItem overlayitem = new OverlayItem(initGeoPoint, getString(R.string.myposition), myAddressDescription);
		this.itemizedLocationOverlay.addOverlay(overlayitem);
		this.mapOverlays.add(this.itemizedLocationOverlay);

		// Go there
		MapController mc = getMapView().getController();
		mc.setZoom(zoomLevel);
		mc.animateTo(initGeoPoint);
	}

	/**
	 * Draws received MedicalLocations as GeoPoints onto the map.
	 * 
	 * @param results
	 *            MedicalLocations to draw
	 */
	protected void drawSearchResults(MedicalLocation[] results)
	{
		// Remove other points
		this.itemizedSearchresultOverlay.clear();

		// Draw results to map
		Log.i(this.getClass().getName() + ": drawSearchResults", "Draw " + results.length + " results to map");
		Log.i("GeoPoint", "Start drawing");

		int count = 0;

		for (MedicalLocation point : results)
		{
			/* at the moment, 20 results are shown */
			if (count < 20)
			{
				count++;
				Log.i(String.format("GeoPoint is at %f : %f", point.getLocation()[0], point.getLocation()[1]),
					String.format("Draw GeoPoint \"%s (%s)\"", point.getName(), point.getType()));
				GeoPoint matchingResult = new GeoPoint(
					(int) (point.getLocation()[0] * 1000000),
					(int) (point.getLocation()[1] * 1000000)
					);
				OverlayItem matchingOverlayitem = new OverlayItem(matchingResult, point.getName(), point.getType());
				this.itemizedSearchresultOverlay.addOverlay(matchingOverlayitem);
			}
		}
		// putting this lines outside the loop improved the perfomance drastically
		// TODO Remove old Geopoints
		this.mapOverlays.add(this.itemizedLocationOverlay);
		this.mapOverlays.add(this.itemizedSearchresultOverlay);

		Log.i("GeoPoint", "Finished drawing");
		// TODO: Center Map and adjust zoom factor so that all results are displayed.
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
			this.locMgr.requestLocationUpdates(this.locProvider, LOCATION_UPDATE_AFTER_SECONDS * 1000,
				LOCATION_UPDATE_AFTER_DISTANCE, this.locLst);
			/* Toast.makeText(this, "Debug message:\n" +
			 * "If running on an emulator:\nSend location fix in DDMS to trigger location update.",
			 * Toast.LENGTH_SHORT).show(); */
			return;
		}

		Toast.makeText(this, getString(R.string.fail_no_provider), Toast.LENGTH_LONG).show();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle(getString(R.string.locationservice));
		builder.setMessage(getString(R.string.ask_user_to_enable_network));
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setPositiveButton(R.string.settings,
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
		builder.setNegativeButton(android.R.string.cancel,
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
}
