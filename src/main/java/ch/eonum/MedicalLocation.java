package ch.eonum;

/**
 * Represents a single address (data set) returned by the server.
 * Can be a medical practice, hospital, doctor office etc.
 * Will provide public methods for name, type and location.
 * Type depends on {@link TypeResolver}, which matches the returned categories to names.
 */
public class MedicalLocation implements Location, Comparable<MedicalLocation>
{
	TypeResolver resolver = TypeResolver.getInstance();
	private String name;
	private String address;
	private String email;
	private String arztTyp;
	private double latitude;
	private double longitude;
	Double[] location = new Double[2];
	private double distance;

	public MedicalLocation(String name, String address, String email, String arztTyp, double latitude, double longitude)
	{
		this.name = name;
		this.address = address;
		this.email = email;
		this.latitude = latitude;
		this.location[0] = latitude;
		this.longitude = longitude;
		this.location[1] = longitude;
		this.arztTyp = resolver.resolve(arztTyp);
		if (this.arztTyp == null)
		{
			Logger.warn("Arzttyp is Null", "this.name = " + this.name + ", this.arztTyp = " + this.arztTyp);
		}
		this.distance = 0;
	}
	
	/* GETTERS & SETTERS */
	@Override
	public String getName()
	{
		return this.name;
	}
	
	public String getAddress()
	{
		return this.address;
	}

	public String getEmail()
	{
		return this.email;
	}

	public String getType()
	{
		return this.arztTyp;
	}

	public void setType(String typ)
	{
		this.arztTyp = typ;
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

	/**
	 * Calculate distance from current map center.
	 * @param latCenter Latitude of map center.
	 * @param lngCenter Longitude of map center.
	 * @return The approximate distance of the location from the current center of the map.
	 */
	public double setDistance(double latCenter, double lngCenter)
	{
		double deltaLat = Math.abs(latCenter - latitude);
		double deltaLng = Math.abs(lngCenter - longitude);

		this.distance = Math.sqrt(Math.pow(deltaLat, 2) + Math.pow(deltaLng, 2));
		return this.distance;
	}

	@Override
	public int compareTo(MedicalLocation loc)
	{
		return (int) Math.signum(this.distance - loc.getDistance());
	}
}
