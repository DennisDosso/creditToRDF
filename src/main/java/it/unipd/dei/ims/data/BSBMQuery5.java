package it.unipd.dei.ims.data;

/** This query contains only one parameter. The consumer has found a product that fulfills his requirements. He now wants to find products with similar features. */
public class BSBMQuery5 {

	public static String select = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"\n" + 
			"SELECT DISTINCT ?product ?productLabel\n" + 
			"WHERE { \n" + 
			"	?product rdfs:label ?productLabel .\n" + 
			" FILTER (%s != ?product)\n" + // first parameter: prpduct
			"	%s bsbm:productFeature ?prodFeature .\n" + // product
			"	?product bsbm:productFeature ?prodFeature .\n" + 
			"	%s bsbm:productPropertyNumeric1 ?origProperty1 .\n" + //product 
			"	?product bsbm:productPropertyNumeric1 ?simProperty1 .\n" + 
			"	FILTER (?simProperty1 < (?origProperty1 + 120) && ?simProperty1 > (?origProperty1 - 120))\n" + 
			"	%s bsbm:productPropertyNumeric2 ?origProperty2 .\n" + // product
			"	?product bsbm:productPropertyNumeric2 ?simProperty2 .\n" + 
			"	FILTER (?simProperty2 < (?origProperty2 + 170) && ?simProperty2 > (?origProperty2 - 170))\n" + 
			"}\n" + 
//			"ORDER BY ?productLabel\n" + 
			"LIMIT 5";
	
	public static String select_named = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"\n" + 
			"SELECT DISTINCT ?product ?productLabel\n" + 
			"WHERE { GRAPH  %here {\n" + 
			"	?product rdfs:label ?productLabel .\n" + 
			" FILTER (%s != ?product)\n" + // product (a second product different from the first one)
			"	%s bsbm:productFeature ?prodFeature .\n" + // product
			"	?product bsbm:productFeature ?prodFeature .\n" + 
			"	%s bsbm:productPropertyNumeric1 ?origProperty1 .\n" + // product 
			"	?product bsbm:productPropertyNumeric1 ?simProperty1 .\n" + 
			"	FILTER (?simProperty1 < (?origProperty1 + 120) && ?simProperty1 > (?origProperty1 - 120))\n" + 
			"	%s bsbm:productPropertyNumeric2 ?origProperty2 .\n" + // product
			"	?product bsbm:productPropertyNumeric2 ?simProperty2 .\n" + 
			"	FILTER (?simProperty2 < (?origProperty2 + 170) && ?simProperty2 > (?origProperty2 - 170))\n" + 
			"}}\n" + 
//			"ORDER BY ?productLabel\n" + 
			"LIMIT 5";
	
	/** the same parameter, the product id, repeated 7 times */
	public static String construct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dataFromProducer5: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer5/>\n" + 
			"\n" + 
			"CONSTRUCT{\n" + 
			"    ?product rdfs:label ?productLabel .\n" + 
			"    %s bsbm:productFeature ?prodFeature .\n" +  // product
			"	?product bsbm:productFeature ?prodFeature .\n" + 
			"	%s bsbm:productPropertyNumeric1 ?origProperty1 .\n" + // product
			"	?product bsbm:productPropertyNumeric1 ?simProperty1 .\n" + 
			"    	%s bsbm:productPropertyNumeric2 ?origProperty2 .\n" + // product
			"	?product bsbm:productPropertyNumeric2 ?simProperty2 .\n" + 
			"}\n" + 
			"WHERE { \n" + 
			"	?product rdfs:label ?productLabel .\n" + 
			" FILTER (%s != ?product)\n" + // product
			"	%s bsbm:productFeature ?prodFeature .\n" +  // product
			"	?product bsbm:productFeature ?prodFeature .\n" + 
			"	%s bsbm:productPropertyNumeric1 ?origProperty1 .\n" + // product
			"	?product bsbm:productPropertyNumeric1 ?simProperty1 .\n" + 
			"	FILTER (?simProperty1 < (?origProperty1 + 120) && ?simProperty1 > (?origProperty1 - 120))\n" + 
			"	%s bsbm:productPropertyNumeric2 ?origProperty2 .\n" + // product
			"	?product bsbm:productPropertyNumeric2 ?simProperty2 .\n" + 
			"	FILTER (?simProperty2 < (?origProperty2 + 170) && ?simProperty2 > (?origProperty2 - 170))\n" + 
			"}\n" + 
//			"ORDER BY ?productLabel\n" +
			"LIMIT 100"; 
}
