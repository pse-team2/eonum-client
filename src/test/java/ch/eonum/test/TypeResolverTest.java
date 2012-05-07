package ch.eonum.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eonum.TypeResolver;

/* Unit Test */
public class TypeResolverTest
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
	public final void testResolve()
	{
		TypeResolver tr = TypeResolver.getInstance();
		String desc = tr.resolve("allgemeinaerzte");
		assertNotNull(desc);
		assertEquals("Allgemeinärzte", desc);
	}
}
