package disgenetqueries;

public class DisGeNetQuery11 {

	public static String select = DisGeNetQuery1.prefixes +
			" SELECT * "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?variant,?disease; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "LIMIT 20";
	
	
	public static String select_named = DisGeNetQuery1.prefixes +
			" SELECT * "
			+ "WHERE { GRAPH %here { "
			+ "%s sio:SIO_000628 ?variant,?disease; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "}"
			+ "LIMIT 20";
	
	public static String construct = DisGeNetQuery1.prefixes +
			" CONSTRUCT {"
			+ "%s sio:SIO_000628 ?variant,?disease; " // 1
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?variant,?disease; " // 2
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "LIMIT 200";
}
