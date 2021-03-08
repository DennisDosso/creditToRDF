package disgenetqueries;

public class DisGeNetQuery6 {

	public static String select = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?gene,?disease ; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . } "
			+ "LIMIT 10";
	
	public static String select_named = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * "
			+ "WHERE { GRAPH %here {"
			+ "%s sio:SIO_000628 ?gene,?disease ; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "} }"
			+ "LIMIT 10";
	
	public static String construct = DisGeNetQuery1.prefixes + 
			"CONSTRUCT {"
			+ "%s sio:SIO_000628 ?gene,?disease ; " // 1
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "} "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?gene,?disease ; " // 2
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . } "
			+ "LIMIT 50";
}
