package ch.eonum;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class HealthMapView extends MapView
{
	private Logger logger;

	// Change listener
	public interface OnChangeListener
	{
		public void onChange(MapView view, GeoPoint newCenter, int newZoom);
	}

	private HealthMapView mapContext;
	private HealthMapView.OnChangeListener mapChangeListener = null;
	private MapController controller;

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
		this.logger = new Logger();
		controller =  this.getController();
		this.setBuiltInZoomControls(true);

		ZoomControls control = (ZoomControls) this.getZoomButtonsController().getZoomControls();
		control.setOnZoomOutClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Toast.makeText(mapContext.getContext(), "Zoom Out Clicked", Toast.LENGTH_LONG).show();
				HealthMapView.this.logger.log("Zoomed out.");
				controller.zoomOut();
				mapChangeListener.onChange(mapContext, mapContext.getMapCenter(), mapContext.getZoomLevel());
			}
		});
		control.setOnZoomInClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Toast.makeText(mapContext.getContext(), "Zoom In Clicked", Toast.LENGTH_LONG).show();
				HealthMapView.this.logger.log("Zoomed in.");
				controller.zoomIn();
				mapChangeListener.onChange(mapContext, mapContext.getMapCenter(), mapContext.getZoomLevel());
			}
		});
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
			Logger.log("Touch event triggered.");
			mapChangeListener.onChange(this, this.getMapCenter(), this.getZoomLevel());
		}

		return true;
	}
}
