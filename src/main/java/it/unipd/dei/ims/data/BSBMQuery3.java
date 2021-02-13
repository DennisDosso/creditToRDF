package it.unipd.dei.ims.data;

public class BSBMQuery3 {

	public static String select = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"SELECT  distinct ?type ?feature1 (bsbm-inst:ProductFeature1 as ?feature2)\n" + 
			"WHERE {\n" + 
			" ?product rdfs:label ?label .\n" + 
			" ?product a %s .\n" + 
			"	?product bsbm:productFeature %s .\n" + 
			"	?product bsbm:productPropertyNumeric1 ?p1 .\n" + 
			"	FILTER ( ?p1 > 30 ) \n" + 
			"	?product bsbm:productPropertyNumeric3 ?p3 .\n" + 
			"	FILTER (?p3 < 100 )\n" + 
			" OPTIONAL { \n" + 
			" ?product bsbm:productFeature %s .\n" + 
			" ?product rdfs:label ?testVar }\n" + 
			" FILTER (!bound(?testVar)) \n" + 
			"}\n" + 
			"ORDER BY ?label\n" + 
			"LIMIT 10";
	
	public static String named_select = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"SELECT  distinct ?type ?feature1 (bsbm-inst:ProductFeature1 as ?feature2)\n" + 
			"WHERE { GRAPH <http://namedgraph/> {\n" + 
			" ?product rdfs:label ?label .\n" + 
			" ?product a %s .\n" + 
			"	?product bsbm:productFeature %s .\n" + 
			"	?product bsbm:productPropertyNumeric1 ?p1 .\n" + 
			"	FILTER ( ?p1 > 30 ) \n" + 
			"	?product bsbm:productPropertyNumeric3 ?p3 .\n" + 
			"	FILTER (?p3 < 100 )\n" + 
			" OPTIONAL { \n" + 
			" ?product bsbm:productFeature %s .\n" + 
			" ?product rdfs:label ?testVar }\n" + 
			" FILTER (!bound(?testVar)) \n" + 
			"}}\n" + 
			"ORDER BY ?label\n" + 
			"LIMIT 10";
	
	public static String construct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"#SELECT  distinct ?type ?feature1 (bsbm-inst:ProductFeature1 as ?feature2)\n" + 
			"CONSTRUCT { ?product rdfs:label ?label .\n" + 
			"?product a %s .\n" + 
			"	?product bsbm:productFeature %s .\n" + 
			"	?product bsbm:productPropertyNumeric1 ?p1 .\n" + 
			"    ?product bsbm:productPropertyNumeric3 ?p3 .\n" + 
			"}\n" + 
			"WHERE {\n" + 
			" ?product rdfs:label ?label .\n" + 
			" ?product a %s .\n" + 
			"	?product bsbm:productFeature %s .\n" + 
			"	?product bsbm:productPropertyNumeric1 ?p1 .\n" + 
			"	FILTER ( ?p1 > 30 ) \n" + 
			"	?product bsbm:productPropertyNumeric3 ?p3 .\n" + 
			"	FILTER (?p3 < 100 )\n" + 
			" OPTIONAL { \n" + 
			" ?product bsbm:productFeature %s.\n" + 
			" ?product rdfs:label ?testVar . }\n" + 
			" FILTER (!bound(?testVar)) \n" + 
			"}\n" + 
			"ORDER BY ?label\n" + 
			"LIMIT 10000";
}
