package ch.eonum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HealthActivity extends Activity {

    /**
     * Main Activity:
     * Called when the activity is first created.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(HealthActivity.class.getName(), "Main Activity started.");
        setContentView(R.layout.main);
        
		Button location = (Button) findViewById(R.id.location);
		location.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(new Intent(view.getContext(), ShowLocation.class));
			}
		});

		Button getdata = (Button) findViewById(R.id.getdata);
		getdata.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(new Intent(view.getContext(), DisplayData.class));
			}
		});
        
    }

}

