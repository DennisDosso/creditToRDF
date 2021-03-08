package disgenetqueries;

public class DisGeNetQuery10 {

	public static String select = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?umls ?umlsTerm ?doid ?doTerm "
			+ "WHERE {"
			+ "%s sio:SIO_000628 ?umls . "
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "} "
			+ "LIMIT 10";
	
	public static String select_named = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?umls ?umlsTerm ?doid ?doTerm "
			+ "WHERE { GRAPH %here {"
			+ "%s sio:SIO_000628 ?umls . "
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "} "
			+ "}"
			+ "LIMIT 10";
	
	public static String construct = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ "%s sio:SIO_000628 ?umls . " // 1
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "}"
			+ "WHERE {"
			+ "%s sio:SIO_000628 ?umls . " // 2
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "} "
			+ "LIMIT 100";
}
