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
	private static boolean debugMode = true;
	
	private static String fileName = "logfile.csv";
	private static Timer timer;
	private static File file;
	private static File sdCard;
	private static FileOutputStream fos;

	public static void init()
	{
		sdCard = Environment.getExternalStorageDirectory();
		file = new File(sdCard.getAbsolutePath() + "/Logs", fileName);
		timer = new Timer();
		Logger.log("Logger initialized.");
	}

	public static void log(String line)
	{
		Log.i("Logger", ">> logged: " + line);

		if (debugMode)
		{
			byte[] data = new String(timer.timeElapsed() + ";" + line + "\n").getBytes();
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
}