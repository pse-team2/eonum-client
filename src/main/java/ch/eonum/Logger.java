package ch.eonum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

/**
 * This class represents a Logger with two main responsibilities:
 *  1. Print Logs to the screen (Log.i)
 *  2. Save logs (i.e. for usability testing) to SD card of the device.
 *  
 *  By using one global boolean debugMode, we can assure that the tests will run correctly.
 *  If not in debug mode, the Logger does nothing.
 *  
 */

public class Logger
{
	static boolean debugMode = false;
	
	private static String fileName = "logfile.csv";
	private static Timer timer;
	private static File file;
	private static File sdCard;
	private static FileOutputStream fos;

	public static void init()
	{
		if (debugMode) {
			sdCard = Environment.getExternalStorageDirectory();
			file = new File(sdCard.getAbsolutePath() + "/Logs", fileName);
			timer = new Timer();
			Logger.log("Logger initialized.");
		}
	}

	public static void log(String line)
	{
		if (debugMode)
		{
			Logger.info("Logger", ">> logged: " + line);
			
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

	public static void info(String tag, String msg)
	{
		if (debugMode) {
			Log.i(tag, msg);
		}
	}

	public static void warn(String tag, String msg)
	{
		if (debugMode) {
			Log.w(tag, msg);
		}
	}

	public static void error(String tag, String msg)
	{
		if (debugMode) {
			Log.e(tag, msg);
		}
	}
}