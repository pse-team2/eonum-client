package ch.eonum.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eonum.Timer;

/* Unit Test */
public class TimerTest
{
	Timer t; 
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
		this.t = new Timer();
	}

	@After
	public void tearDown()
	{
	}

	@Test
	public final void testTimer()
	{
		
		double timeElapsed = t.timeElapsed();
		assertTrue(timeElapsed > 0);
	}
}