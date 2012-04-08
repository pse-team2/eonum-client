package ch.eonum;

import java.util.ArrayList;
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
		/*
		types.put("allgemeinaerzte", "Allgemeinärzte");
		types.put("kinderaerzte", "Kinderärzte");
		types.put("gynaekologen", "Gynäkologen");
		types.put("akupunkturaerzte", "Akupunkturärzte");
		types.put("chirurgen", "Chirurgen");
		types.put("homoeopathieaerzte", "Homöopathieärzte");
		types.put("manuellemedizin", "Manuelle Medizin");
		types.put("neurologen", "Neurologen");
		types.put("internisten", "Internisten");
		types.put("psychiater", "Psychiater");
		types.put("rechtsmediziner", "Rechtsmediziner");
		types.put("roentgenaerzte","Röntgenärzte");
		types.put("sportmediziner", "Sportmediziner");
		types.put("tropenaerzte", "Tropenärzte");
		types.put("urologen", "Urologen");
		types.put("allergologen", "Allergologen");
		types.put("anaesthesisten", "Anästhesisten");
		types.put("arbeitsmediziner", "Arbeitsmediziner");
		types.put("augenaerzte", "Augenärzte");
		types.put("endokrinologen", "Endokrinologen");
		types.put("frauenaerzte", "Frauenärzte");
		types.put("haematologen", "Hämatologen");
		types.put("handchirurgen", "Handchirurgen");
		types.put("hautaerzte", "Hautärzte");
		types.put("infektiologen", "Infektiologen");
		types.put("kardiologen", "Kardiologen");
		types.put("kieferchirurgen", "Kieferchirurgen");
		types.put("kinderpsychiater", "Kinderpsychiater");
		types.put("lungenaerzte", "Lungenärzte");
		types.put("magendarmaerzte", "Magen-Darm-Ärzte");
		types.put("nephrologen", "Nephrologen");
		types.put("ohrenaerzte", "Ohrenärzte");
		types.put("onkologen", "Onkologen");
		types.put("orthopaeden", "Orthopäden");
		types.put("pathologen", "Pathologen");
		types.put("plastischechirurgen", "Plastische Chirurgen");
		*/
		types.put("akupunkturaerzte", "Akupunkturärzte");
		types.put("allergologen", "Allergologen");
		types.put("allgemeinaerzte", "Allgemeinärzte");
		types.put("anaesthesisten", "Anästhesisten");
		types.put("arbeitsmediziner", "Arbeitsmediziner");
		types.put("augenaerzte", "Augenärzte");
		types.put("chirurgen", "Chirurgen");
		types.put("endokrinologen", "Endokrinologen");
		types.put("frauenaerzte", "Frauenärzte");
		types.put("gynaekologen", "Gynäkologen");
		types.put("haematologen", "Hämatologen");
		types.put("handchirurgen", "Handchirurgen");
		types.put("hautaerzte", "Hautärzte");
		types.put("homoeopathieaerzte", "Homöopathieärzte");
		types.put("infektiologen", "Infektiologen");
		types.put("internisten", "Internisten");
		types.put("kardiologen", "Kardiologen");
		types.put("kieferchirurgen", "Kieferchirurgen");
		types.put("kinderaerzte", "Kinderärzte");
		types.put("kinderpsychiater", "Kinderpsychiater");
		types.put("lungenaerzte", "Lungenärzte");
		types.put("magendarmaerzte", "Magen-Darm-Ärzte");
		types.put("manuellemedizin", "Manuelle Medizin");
		types.put("nephrologen", "Nephrologen");
		types.put("neurologen", "Neurologen");
		types.put("ohrenaerzte", "Ohrenärzte");
		types.put("onkologen", "Onkologen");
		types.put("orthopaeden", "Orthopäden");
		types.put("pathologen", "Pathologen");
		types.put("plastischechirurgen", "Plastische Chirurgen");
		types.put("psychiater", "Psychiater");
		types.put("rechtsmediziner", "Rechtsmediziner");
		types.put("roentgenaerzte","Röntgenärzte");
		types.put("sportmediziner", "Sportmediziner");
		types.put("tropenaerzte", "Tropenärzte");
		types.put("urologen", "Urologen");
	}

	public String resolve(String type)
	{
		return types.get(type);
	}

	public String[] getCategories()
	{
		ArrayList<String> typesList = new ArrayList<String>();
		String[] typesArray = new String[types.size()];
		for (String description : types.values())
		{
			typesList.add(description);
		}
		return typesList.toArray(typesArray);
	}
}
