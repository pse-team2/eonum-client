package ch.eonum;

import android.util.Log;

/**
 * Represents a single address (data set) returned by the server.
 * Can be a medical practice, hospital, doctor office etc.
 * Will provide public methods for name, type and location.
 * Type depends on TypeResolver, which matches the returned categories to names.
 */
public class MedicalLocation implements Location, Comparable<MedicalLocation>
{

	TypeResolver Resolver = new TypeResolver();
	private String name;
	private String arztTyp;
	private double latitude;
	private double longitude;
	Double[] location = new Double[2];
	private double distance;

	public MedicalLocation(String name, String arztTyp, double latitude, double longitude)
	{
		this.name = name;
		this.latitude = latitude;
		this.location[0] = latitude;
		this.longitude = longitude;
		this.location[1] = longitude;
		this.arztTyp = Resolver.resolve(arztTyp);
		if (this.arztTyp == null)
		{
			Log.w("Arzttyp is Null", "this.name = " + this.name + ", this.arztTyp = " + this.arztTyp);
		}
		this.distance = 0;
	}
	
	/* GETTERS & SETTERS */
	@Override
	public String getName()
	{
		return this.name;
	}

	public String getType()
	{
		return this.arztTyp;
	}

	@Override
	public Double[] getLocation()
	{
		return this.location;
	}

	public double getDistance()
	{
		return this.distance;
	}

	public double setDistance(double latCenter, double lngCenter)
	{
		this.distance = Math.sqrt(Math.pow(latCenter - latitude, 2) + Math.pow(lngCenter - longitude, 2));
		return this.distance;
	}

	@Override
	public int compareTo(MedicalLocation loc)
	{
		return (int) Math.signum(this.distance - loc.getDistance());
		/*
		if (this.distance > loc.getDistance()) 
		{
			return 1;
		}
		else if (this.distance < loc.getDistance()) 
		{
			return -1;
		}
		else 
		{
			return 0;
		}
		*/
	}

}
