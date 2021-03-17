package disgenetqueries;

public class DisGeNetQuery7 {

	public static String select = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?dName ?gSymbol  "
			+ "WHERE {"
			+ "%s rdf:type ?type; " // 1
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "}  "
			+ "LIMIT 10 ";
	
	public static String select_named = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?dName ?gSymbol  "
			+ "WHERE { GRAPH %here {"
			+ "%s rdf:type ?type; " // 1
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "} } "
			+ "LIMIT 10 ";
	
	public static String construct = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ "%s rdf:type ?type; "//1
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "}"
			+ "WHERE { "
			+ "%s rdf:type ?type; "// 2
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "}  "
			+ "LIMIT 100 ";
}
