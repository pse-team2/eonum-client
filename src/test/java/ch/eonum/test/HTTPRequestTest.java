package ch.eonum.test;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import android.os.AsyncTask;
import ch.eonum.HTTPRequest;

/* Unit Test */
public class HTTPRequestTest
{
	@BeforeClass
	public static void setUpBeforeClass()
	{
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
	}

	@Before
	public void setUp()
	{
	}

	@After
	public void tearDown()
	{
	}

	@Test
	public final void testHTTPRequest()
	{
		HTTPRequest request = new HTTPRequest(46.90, 7.50, 46.95, 7.55);
		String result = "";
		AsyncTask<Void, Void, String> httpTask = request.execute();
		try
		{
			result = httpTask.get();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		
		assertFalse(result.length() == 0);
		assertTrue(result.contains("Lobsiger"));
		assertTrue(result.contains("Rohrmattstrasse"));
		assertTrue(result.contains("internisten"));
		assertTrue(result.contains("46.92"));
	}

	@Test
	public final void testGetResults()
	{
		// fail("Noch nicht implementiert"); // TODO
	}
}
