package ch.eonum;

import java.util.HashMap;

/**
 * Resolves a medical type to a String.
 */
public class TypeResolver
{

	// Names (german, plural)
	HashMap<String, String> types = new HashMap<String, String>();

	public TypeResolver()
	{
		types.put("allgemeinaerzte", "Allgemeinärzte");
		types.put("kinderaerzte", "Kinderärzte");
		types.put("gynaekologen", "Gynäkologen");
		types.put("akupunkturaerzte", "Akupunkturärzte");
		types.put("chirurgen", "Chirurgen");
		types.put("homoeopathieaerzte", "Homöopathieaerzte");
		types.put("manuellemedizin", "Manuelle Medizin");
		types.put("neurologen", "Neurologen");
		types.put("internisten", "Internisten");
		types.put("psychiater", "Psychiater");
	}

	public String resolve(String type)
	{
		return types.get(type);
	}
}
