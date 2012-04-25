package ch.eonum;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class HealthMapView extends MapView
{
	// Change listener
	public interface OnChangeListener
	{
		public void onChange(MapView view, GeoPoint newCenter, int newZoom);
	}

	private HealthMapView mapContext;
	private HealthMapView.OnChangeListener mapChangeListener = null;

	public HealthMapView(Context context, String apiKey)
	{
		super(context, apiKey);
		initialize();
	}

	public HealthMapView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize();
	}

	public HealthMapView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize();
	}

	private void initialize()
	{
		mapContext = this;
	}

	public void setOnChangeListener(HealthMapView.OnChangeListener listener)
	{
		mapChangeListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			mapChangeListener.onChange(this, this.getMapCenter(), this.getZoomLevel());
			Toast.makeText(mapContext.getContext(), "Touch Event Triggered", Toast.LENGTH_LONG).show();
		}

		return true;
	}
}
