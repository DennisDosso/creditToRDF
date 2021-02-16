package it.unipd.dei.ims.data;

/** NB: due to the nature of this query, we limited the number of returned triples in the construct,
 * so to limit the execution time
 * 
 * TODO crea il controllo della presenza delle triple nel triple-store*/
public class BSBMQuery7 {

	public static String select = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"select distinct ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle \n" + 
			" ?reviewer ?revName ?rating1 ?rating2\n" + 
			"WHERE {\n" + 
			"    %s rdfs:label ?productLabel .\n" + // first parameter
//			"    OPTIONAL {" +
			"        ?offer bsbm:product %s .\n" + // third parameter 
			"        ?offer bsbm:price ?price .\n" + 
			"        ?offer bsbm:vendor ?vendor .\n" + 
			"	     ?vendor rdfs:label ?vendorTitle .\n" + 
			"        ?vendor bsbm:country %s.\n" + // second parameter, country - not used as parameter by BSBM, I decided to introduce it
			"        ?offer dc:publisher ?vendor . \n" + 
			"        ?offer bsbm:validTo ?date .\n" + 
			"        FILTER (?date > \"2008-03-01\"^^xsd:dateTime)\n" +
//			"    }" +
			"    OPTIONAL {\n" + 
			"	?review bsbm:reviewFor %s .\n" + // fourth parameter 
			"	?review rev:reviewer ?reviewer .\n" + 
			"	?reviewer foaf:name ?revName .\n" + 
			"	?review dc:title ?revTitle .\n" + 
			" OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" + 
			"        OPTIONAL { ?review bsbm:rating2 ?rating2 . }}\n" + 
			"} LIMIT 2\n";
	
	public static String select_named = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"select distinct ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle \n" + 
			" ?reviewer ?revName ?rating1 ?rating2\n" + 
			"WHERE { GRAPH <http://namedgraph/> {\n" + 
			"    %s rdfs:label ?productLabel .\n" + // first parameter
//			"    OPTIONAL {" +
			"        ?offer bsbm:product %s .\n" + // third parameter 
			"        ?offer bsbm:price ?price .\n" + 
			"        ?offer bsbm:vendor ?vendor .\n" + 
			"	     ?vendor rdfs:label ?vendorTitle .\n" + 
			"        ?vendor bsbm:country %s.\n" + // second parameter 
			"        ?offer dc:publisher ?vendor . \n" + 
			"        ?offer bsbm:validTo ?date .\n" + 
			"        FILTER (?date > \"2008-03-01\"^^xsd:dateTime)\n" +
//			"    }" +
			"    OPTIONAL {\n" + 
			"	?review bsbm:reviewFor %s .\n" + // fourth parameter 
			"	?review rev:reviewer ?reviewer .\n" + 
			"	?reviewer foaf:name ?revName .\n" + 
			"	?review dc:title ?revTitle .\n" + 
			" OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" + 
			"        OPTIONAL { ?review bsbm:rating2 ?rating2 . }}\n" + 
			"}}"
			+ "LIMIT 2\n";
	
	
	
	public static String construct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"PREFIX dataFromProducer5: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer5/>\n" + 
			"CONSTRUCT {%s rdfs:label ?productLabel .\n" + // first parameter: product 
			"?offer bsbm:product %s .\n" + // product 
			"    ?offer bsbm:price ?price .\n" + 
			"	?offer bsbm:vendor ?vendor .\n" + 
			"	?vendor rdfs:label ?vendorTitle .\n" + 
			"    ?vendor bsbm:country %s .\n" + // country 
			"    ?offer dc:publisher ?vendor . \n" + 
			" 	?offer bsbm:validTo ?date .\n" + 
			"	?review bsbm:reviewFor %s .\n" + // product 
			"	?review rev:reviewer ?reviewer .\n" + 
			"	?reviewer foaf:name ?revName .\n" + 
			"	?review dc:title ?revTitle .\n" + 
			"	?review bsbm:rating1 ?rating1 .\n" + 
			"	?review bsbm:rating2 ?rating2 .}\n" + 
			"where {\n" + 
			"    %s rdfs:label ?productLabel .\n" + // product 
//			"    OPTIONAL {\n" + 
			"    ?offer bsbm:product %s .\n" + //product 
			"    ?offer bsbm:price ?price .\n" + 
			"	?offer bsbm:vendor ?vendor .\n" + 
			"	?vendor rdfs:label ?vendorTitle .\n" + 
			"    ?vendor bsbm:country %s .\n" + // country 
			"    ?offer dc:publisher ?vendor . \n" + 
			" 	?offer bsbm:validTo ?date .\n" + 
			"    FILTER (?date > \"2008-03-01\"^^xsd:dateTime )\n" + 
//			"}\n" + 
			"    OPTIONAL {\n" + 
			"	?review bsbm:reviewFor %s .\n" + // product 
			"	?review rev:reviewer ?reviewer .\n" + 
			"	?reviewer foaf:name ?revName .\n" + 
			"	?review dc:title ?revTitle .\n" + 
			" OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" + 
			" OPTIONAL { ?review bsbm:rating2 ?rating2 . } \n" + 
			" }\n" + 
			"} LIMIT 50";// a little tweak to limit the time
}
