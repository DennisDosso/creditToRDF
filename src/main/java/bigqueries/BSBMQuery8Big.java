package bigqueries;

import it.unipd.dei.ims.data.BSBMQuery8;

public class BSBMQuery8Big extends BSBMQuery8 {

	/** This selects on average requires 20s on the 100M size */
	public static String select = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX rev: <http://purl.org/stuff/rev#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4  "
			+ "WHERE { "
			+ "?review bsbm:reviewFor %s .  "
			+ "?review dc:title ?title . "
			+ "?review rev:text ?text . "
			+ "FILTER langMatches( lang(?text), \"EN\" )  "
			+ "?review rev:reviewer ?reviewer .  } "
			+ "LIMIT 5";
	
	public static String select_named = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX rev: <http://purl.org/stuff/rev#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4  "
			+ "WHERE { GRAPH <http://namedgraph/> {"
			+ "?review bsbm:reviewFor %s .  "
			+ "?review dc:title ?title . "
			+ "?review rev:text ?text . "
			+ "FILTER langMatches( lang(?text), \"EN\" )  "
			+ "?review rev:reviewer ?reviewer .  } "
			+ "}"
			+ "LIMIT 5";
	
	public static String construct = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX rev: <http://purl.org/stuff/rev#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "CONSTRUCT {"
			+ "?review bsbm:reviewFor %s .  " // 1
			+ "?review dc:title ?title . "
			+ "?review rev:text ?text . "
			+ "?review rev:reviewer ?reviewer .   "
			+ "} "
			+ "WHERE { "
			+ "?review bsbm:reviewFor %s .  " // 2
			+ "?review dc:title ?title . "
			+ "?review rev:text ?text . "
			+ "FILTER langMatches( lang(?text), \"EN\" )  "
			+ "?review rev:reviewer ?reviewer .  } "
			+ "LIMIT 50";
}
