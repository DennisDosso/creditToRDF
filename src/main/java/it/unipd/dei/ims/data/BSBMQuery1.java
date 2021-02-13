package it.unipd.dei.ims.data;

public class BSBMQuery1 {
	
	//initializes all the values
	public static void init() {
		
	}

	/** First query from BSBM set of evaluation queries */
	public static String construct_query_1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"CONSTRUCT {?product rdfs:label ?label ;\n" + 
			"     	a bsbm-inst:ProductType3 ;\n" + 
			"    	bsbm:productFeature bsbm-inst:ProductFeature44 ;\n" + 
			"        bsbm:productFeature bsbm-inst:ProductFeature54  ;\n" + 
			"        bsbm:productPropertyNumeric1 ?value1 .}\n" + 
			"WHERE { \n" + 
			"    ?product rdfs:label ?label ;\n" + 
			"     	a bsbm-inst:ProductType3 ;\n" + 
			"    	bsbm:productFeature bsbm-inst:ProductFeature44 ;\n" + 
			"        bsbm:productFeature bsbm-inst:ProductFeature54  ;\n" + 
			"        bsbm:productPropertyNumeric1 ?value1 .\n" + 
			"    FILTER (?value1 > 300) .\n" + 
			"	}\n" + 
			"ORDER BY ?label\n";			;
	
	public static String parameter_query_1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"CONSTRUCT {?product rdfs:label ?label ;\n" + 
			"     	a %s ;\n" + 
			"    	bsbm:productFeature %s ;\n" + 
			"        bsbm:productFeature %s  ;\n" + 
			"        bsbm:productPropertyNumeric1 ?value1 .}\n" + 
			"WHERE { \n" + 
			"    ?product rdfs:label ?label ;\n" + 
			"     	a %s ;\n" + 
			"    	bsbm:productFeature %s ;\n" + 
			"        bsbm:productFeature %s ;\n" + 
			"        bsbm:productPropertyNumeric1 ?value1 .\n" + 
			"    FILTER (?value1 > %s) .\n" + 
			"	}\n" + 
			"ORDER BY ?label\n"; 
//			"LIMIT 20"; // remove the limit of triples, since that limit does not correlate to the limit in the select
	// if you leave the limit here it is an error: you reduce the total number of triples returned, thus
	// you do not build the whole subgraph that creates the answer
	
	public static String select_query_1 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"\n" + 
			"SELECT DISTINCT ?product ?label\n" + 
			"WHERE { \n" + 
			" ?product rdfs:label ?label .\n" + 
			" ?product a %s .\n" + 
			" ?product bsbm:productFeature %s . \n" + 
			" ?product bsbm:productFeature %s . \n" + 
			"?product bsbm:productPropertyNumeric1 ?value1 . \n" + 
			"	FILTER (?value1 > %s) \n" + 
			"	}\n" + 
			"ORDER BY ?label\n" + 
			"LIMIT 20";
	
	public static String select_query_1_with_named_graphs = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"\n" + 
			"SELECT DISTINCT ?product ?label\n" + 
			"WHERE { GRAPH <http://namedgraph/> {\n" + 
			" ?product rdfs:label ?label .\n" + 
			" ?product a %s .\n" + 
			" ?product bsbm:productFeature %s . \n" + 
			" ?product bsbm:productFeature %s . \n" + 
			"?product bsbm:productPropertyNumeric1 ?value1 . \n" + 
			"	FILTER (?value1 > %s) \n" + 
			"	}}\n" + 
			"ORDER BY ?label\n" + 
			"LIMIT 20";
;	
	/** value to put as last parameter in the FILTER condition of parameter_query_1*/
	public static String value_query_1 = "300";
	
	public static String construct_test = "CONSTRUCT\n" + 
			"where { \n" + 
			"	?s ?p ?o .\n" + 
			"} limit 100 "; 
	
}
