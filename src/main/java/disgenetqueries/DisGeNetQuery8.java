package disgenetqueries;

/** 3 parameters here
 * */
public class DisGeNetQuery8 {
	
	public static String select = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?disease ?diseaseName ?gene ?disease2 ?diseaseName2 "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "FILTER (?disease != ?disease2) "
			+ "FILTER (%s != ?gda2) }" // 2
			+ "LIMIT 10";
	
	public static String select_named = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?disease ?diseaseName ?gene ?disease2 ?diseaseName2 "
			+ "WHERE { GRAPH %here {"
			+ "%s sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "FILTER (?disease != ?disease2) "
			+ "FILTER (%s != ?gda2) " // 2
			+ "}}"
			+ "LIMIT 10";
	
	public static String construct = DisGeNetQuery1.prefixes +
			" CONSTRUCT {"
			+ "%s sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "} "
			+ "WHERE { "
			+ "%s sio:SIO_000628 ?disease,?gene . " // 2
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "FILTER (?disease != ?disease2) "
			+ "FILTER (%s != ?gda2) " // 3
			+ "}" 
			+ "LIMIT 300";


}
