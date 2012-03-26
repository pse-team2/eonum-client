package ch.eonum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HealthActivity extends Activity
{

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

		Button location = (Button) findViewById(R.id.location);
		location.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				LocationManager locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				if (!locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setMessage(getString(R.string.askusertoenablegps))
							.setCancelable(true)
							.setPositiveButton(android.R.string.yes,
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

}
