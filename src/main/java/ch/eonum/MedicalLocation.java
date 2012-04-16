package ch.eonum;

import android.util.Log;

/**
 * Represents a single address (data set) returned by the server.
 * Can be a medical practice, hospital, doctor office etc.
 * Will provide public methods for name, type and location.
 * Type depends on TypeResolver, which matches the returned categories to names.
 */
public class MedicalLocation implements Location
{

	TypeResolver Resolver = new TypeResolver();
	private String name;
	private String arztTyp;
	@SuppressWarnings("unused")
	private double latitude;
	@SuppressWarnings("unused")
	private double longitude;
	Double[] location = new Double[2];

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
}
