package bigqueries;

import it.unipd.dei.ims.data.BSBMQuery7;

public class BSBMQuery7Big extends BSBMQuery7 {
	
	public static String select = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
			+ "PREFIX rev: <http://purl.org/stuff/rev#> "
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "select distinct "
			+ "?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle  ?reviewer ?revName ?rating1 ?rating2 "
			+ "WHERE { "
			+ "%s rdfs:label ?productLabel . "
			+ "?offer bsbm:product %s . "
			+ "?offer bsbm:price ?price . "
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer dc:publisher ?vendor . }  "
			+ "LIMIT 5";
	
	public static String select_named = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
			+ "PREFIX rev: <http://purl.org/stuff/rev#> "
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "select distinct "
			+ "?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle  ?reviewer ?revName ?rating1 ?rating2 "
			+ "WHERE { GRAPH <http://namedgraph/> {"
			+ "%s rdfs:label ?productLabel . "
			+ "?offer bsbm:product %s . "
			+ "?offer bsbm:price ?price . "
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer dc:publisher ?vendor . } }  "
			+ "LIMIT 5";
	
	public static String construct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
			+ "PREFIX rev: <http://purl.org/stuff/rev#> "
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "CONSTRUCT { "
			+ "%s rdfs:label ?productLabel . " // 1
			+ "?offer bsbm:product %s . " // 2
			+ "?offer bsbm:price ?price . "
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer dc:publisher ?vendor .  } " // remove this line if it requires too much time
			+ "WHERE { "
			+ "%s rdfs:label ?productLabel . " // 3
			+ "?offer bsbm:product %s . " // 4
			+ "?offer bsbm:price ?price . "
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer dc:publisher ?vendor . }  "
			+ "LIMIT 50";

}
