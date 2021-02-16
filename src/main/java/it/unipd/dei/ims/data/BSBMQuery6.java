package it.unipd.dei.ims.data;

public class BSBMQuery6 {

	public static String select = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"\n" + 
			"SELECT ?product \n" + 
			"WHERE {\n" + 
			"	?product rdfs:label %s .\n" + 
			" ?product rdf:type bsbm:Product .\n" + 
			"} LIMIT 10";
	
	public static String select_named = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"\n" + 
			"SELECT ?product \n" + 
			"WHERE { GRAPH <http://namedgraph/> {\n" + 
			"	?product rdfs:label %s .\n" + 
			" ?product rdf:type bsbm:Product .\n" + 
			"}}"
			+ " LIMIT 10"; 
	
	public static String construct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"\n" +
			"CONSTRUCT {" +
			"	?product rdfs:label %s .\n" + //1
			" ?product rdf:type bsbm:Product .\n" + 
			"}"+ 
			"WHERE { \n" + 
			"	?product rdfs:label %s .\n" + //2
			" ?product rdf:type bsbm:Product .\n" + 
			"}"
			+ " LIMIT 100";  
}
