package ch.eonum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

/**
 * Logger which saves logs to SD card of the device.
 * 
 */
public class Logger
{
	private boolean debug = true;
	
	private String fileName = "logfile.csv";
	private Timer timer;
	private File file;
	private File sdCard;
	private FileOutputStream fos;

	public Logger()
	{
		sdCard = Environment.getExternalStorageDirectory();
		file = new File(sdCard.getAbsolutePath() + "/Logs", fileName);
		timer = new Timer();
		this.log("Logger initialized.");
	}

	public void log(String line)
	{
		Log.i(this.getClass().getName(), ">> logged: " + line);
		
		if (!debug) return;
		
		byte[] data = new String(timer.timeElapsed() + ";" + line+"\n").getBytes();
		try
		{
			fos = new FileOutputStream(file, true);
			fos.write(data);
			fos.flush();
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}