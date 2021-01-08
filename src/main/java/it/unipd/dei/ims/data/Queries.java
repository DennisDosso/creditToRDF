package it.unipd.dei.ims.data;

public class Queries {

	public static String test_query = "SELECT ?s ?p ?o \n"
			+ "WHERE { ?s ?p ?o . } LIMIT 100";
	
	public static String count_query = "select (COUNT(*) as ?triples)\n" + 
			"where {?s ?p ?o}";
	
	public static String get_all_triples = "SELECT ?s ?p ?o WHERE { ?s ?p ?o.}";
	
	public static String SPARQL_SELECT_QUERY;
	public static String SPARQL_CONSTRUCT_QUERY;
}
