package it.unipd.dei.ims.data;

/** Note that we put limits both on the select and construct queries, also in the other classes of this type.
 * We did this because if we do not put a limit in the select, the execution time becames too long. 
 * We also need to put  alimit in the construct query for the same reason. Obviously, 
 * this second limit has to be larger, indicatively equal to <number of triples of an answer> * value of LIMIT 
 * in the corresponding select query. This is necessary in order to capture the whole graph that produces the output of the
 * select query. */
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
			"ORDER BY ?label\n";			
	
	public static String construct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
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
//			"ORDER BY ?label\n" +
			"LIMIT 100";  
	
	
	public static String select = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
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
//			"ORDER BY ?label\n" + 
			"LIMIT 5";
	
	public static String select_named = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
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
//			"ORDER BY ?label\n" + 
			"LIMIT 5";
;	
	/** value to put as last parameter in the FILTER condition of parameter_query_1*/
	public static String value_query_1 = "300";
	
	public static String construct_test = "CONSTRUCT\n" + 
			"where { \n" + 
			"	?s ?p ?o .\n" + 
			"} limit 100 "; 
	
}
