package ch.eonum;

/**
 * Represents a single address (data set) returned by the server.
 * Can be a medical practice, hospital, doctor office etc.
 * 
 * Will provide public methods for name, type and location.
 * Type depends on TypeResolver, which matches the returned categories to names.
 *
 */
public class Location {
	
	TypeResolver Resolver = new TypeResolver();
	private String name;
	private String arztTyp;
	@SuppressWarnings("unused")
	private double latitude;
	@SuppressWarnings("unused")
	private double longitude;
	Double[] location = new Double[2];
	
	public Location (String name, String arztTyp, double latitude, double longitude) {
		this.name = name;
		this.latitude = latitude;
		this.location[0] = latitude;
		this.longitude = longitude;
		this.location[1] = longitude;
		this.arztTyp = Resolver.resolve(arztTyp);
		System.out.println("Arzttyp: "+arztTyp);
	}
	
	/*
	 * GETTERS & SETTERS
	 */
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return arztTyp;
	}
	
	public Double[] getLocation() {
		return location;
	}
}
