package disgenetqueries;

public class DisGeNetQuery9 {

	public static String select = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?gene ?geneName ?disease ?gene2 ?geneName2 "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "FILTER (?gene != ?gene2) "
			+ "FILTER (%s != ?gda2) } " // 2
			+ "LIMIT 10";
	
	
	public static String select_named = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?gene ?geneName ?disease ?gene2 ?geneName2 "
			+ "WHERE { GRAPH %here {"
			+ "%s sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "FILTER (?gene != ?gene2) "
			+ "FILTER (%s != ?gda2) } " // 2
			+ "}" 
			+ "LIMIT 10";
	
	
	public static String construct = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ "%s sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "} "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?disease,?gene . " // 2
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "FILTER (?gene != ?gene2) "
			+ "FILTER (%s != ?gda2) } " // 3
			+ "LIMIT 100";

}
