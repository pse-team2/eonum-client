package ch.eonum.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eonum.JSONParser;
import ch.eonum.Logger;
import ch.eonum.MedicalLocation;
import ch.eonum.Mode;

/* Unit Test */
public class JSONParserTest
{
	private static Mode previousMode;
	private String content;
	
	@BeforeClass
	public static void setUpBeforeClass()
	{
		previousMode = Logger.mode;
		Logger.mode = Mode.TEST;
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		Logger.mode = previousMode;
	}

	@Before
	public void setUp()
	{
		this.content = "{ \"status\": \"OK\",\"results\": [ { \"name\": \"Hans Muster\"," 
			+ "\"address\": \"Bahnhofstrasse, 3000 Bern\", \"email\": \"\", "
			+ "\"types\": [ \"allgemeinaerzte\" ], \"location\": { \"lat\": 46.12345, \"lng\": 7.54321 } } ] } ";
	}

	@After
	public void tearDown()
	{
	}

	@Test
	public final void testDeserialize()
	{
		JSONParser parser = new JSONParser();
		MedicalLocation[] locations = parser.deserializeLocations(content);
		
		assertNotNull(locations);
		MedicalLocation testLocation = locations[0];
		
		assertNotNull(testLocation);
		assertEquals("Hans Muster", testLocation.getName());
		assertEquals("", testLocation.getEmail());		
	}
}