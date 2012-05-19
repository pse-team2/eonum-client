package ch.eonum;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MapItemizedOverlay extends ItemizedOverlay<OverlayItem>
{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;

	public MapItemizedOverlay(Drawable defaultMarker, Context context)
	{
		super(boundCenterBottom(defaultMarker));
		this.mContext = context;
		populate();
	}

	public void addOverlay(OverlayItem overlay)
	{
		this.mOverlays.add(overlay);
		populate();
	}

	public void clear()
	{
		this.mOverlays.clear();
	}

	@Override
	protected OverlayItem createItem(int i)
	{
		return this.mOverlays.get(i);
	}

	@Override
	public int size()
	{
		return this.mOverlays.size();
	}

	@Override
	protected boolean onTap(int index)
	{
		OverlayItem item = this.mOverlays.get(index);
		Logger.log("Touched Geopoint " + item.getTitle() + ".");

		if (item.getTitle().equals(HealthActivity.mainActivity.getString(R.string.myposition)))
		{
			showMyPositionDialog(item);
		}
		else
		{
			showMedialLocationDialog(item);
		}

		return true;
	}

	private void showMedialLocationDialog(OverlayItem item)
	{
		final String[] items = item.getSnippet().split("\n");

		AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
		builder.setTitle(item.getTitle());
		builder.setItems(items, new DialogInterface.OnClickListener()
		{
			// On click, trigger the appropriate intent
			@Override
			public void onClick(DialogInterface dialog, int item)
			{
				switch (item)
				{
					case 0: // Category
						System.out.println();
						// TODO: Do nothing (at the moment)
						break;

					case 1: // Address
						String address = items[item].replace(",", "+");
						String uri = "geo:" + 0 + "," + 0 + "?q=" + address;
						System.out.println("address to be given: " + address);
						mContext.startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
						break;

					case 2: // Email
						if (!items[item].equals(mContext.getString(R.string.no_email)))
						{
							final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
							emailIntent.setType("plain/text");
							emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {items[item]});
							emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
							emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
							mContext.startActivity(Intent.createChooser(emailIntent, "E-Mail senden"));
						}
						else
						{
							Toast toast = Toast.makeText(mContext, R.string.there_is_no_email, Toast.LENGTH_SHORT);
							toast.show();
						}
						break;

					case 3: // Email, maybe telephone, not used yet
						if (!items[item].equals(mContext.getString(R.string.no_tel)))
						{
							// Just dial, do not call the number. ACTION_CALL would call the number 
							// and requires the 'android.permission.CALL_PHONE' permission.
							Intent callIntent = new Intent(Intent.ACTION_DIAL);
							callIntent.setData(Uri.parse("tel:" + items[item]));
							mContext.startActivity(callIntent);
						}
						else
						{
							Toast toast = Toast.makeText(mContext, R.string.there_is_no_tel, Toast.LENGTH_SHORT);
							toast.show();
						}
						break;

					default:
						break;
				}
			}
		});
		builder.show();
	}

	private void showMyPositionDialog(OverlayItem item)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
		builder.setTitle(item.getTitle());
		builder.setItems(new String[] {item.getSnippet()}, null);
		builder.show();
	}
}
