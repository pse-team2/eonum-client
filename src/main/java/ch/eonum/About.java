package ch.eonum;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * Displays the application info.
 */
public class About extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView aboutText = (TextView) findViewById(R.id.textviewAbout);
		aboutText.setMovementMethod(new ScrollingMovementMethod());
	}
}
