package disgenetqueries;

public class DisGeNetQuery5 {

	public static String select =  DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT ?gene ?symbol ?protein ?proteinclassname "
			+ "WHERE { "
			+ "%s sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " //1
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "} "
			+ "LIMIT 10";
	
	public static String select_named =  DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT ?gene ?symbol ?protein ?proteinclassname "
			+ "WHERE { GRAPH %here {"
			+ "%s sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " //1
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "}} "
			+ "LIMIT 10";
	
	public static String construct =  DisGeNetQuery1.prefixes + 
			"CONSTRUCT {"
			+ "%s sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " //1
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "}"
			+ "WHERE { "
			+ "%s sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " //2
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "} "
			+ "LIMIT 100";
}
