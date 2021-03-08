package bigqueries;

public class BSBMQuery10Big {

	public static String select = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
			+ "SELECT DISTINCT ?offer ?price "
			+ "WHERE { "
			+ "?offer bsbm:product %s ." // 1
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer bsbm:deliveryDays ?deliveryDays . "
			+ "FILTER (?deliveryDays <= 3) "
			+ "?offer bsbm:validTo ?date . "
			+ "FILTER (?date > \"2008-02-10T00:00:00\"^^xsd:dateTime ) "
			+ "?offer bsbm:price ?price .  "
			+ "} "
			+ "LIMIT 5";

	public static String select_named = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
			+ "SELECT DISTINCT ?offer ?price "
			+ "WHERE { GRAPH %here {"
			+ "?offer bsbm:product %s ." // 1
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer bsbm:deliveryDays ?deliveryDays . "
			+ "FILTER (?deliveryDays <= 3) "
			+ "?offer bsbm:validTo ?date . "
			+ "FILTER (?date > \"2008-02-10T00:00:00\"^^xsd:dateTime ) "
			+ "?offer bsbm:price ?price .  "
			+ "}"
			+ "} "
			+ "LIMIT 5";

	public static String construct =  "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
			+ "CONSTRUCT{ "
			+ "?offer bsbm:product %s ." // 1
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer bsbm:deliveryDays ?deliveryDays . "
			+ "?offer bsbm:validTo ?date . "
			+ "?offer bsbm:price ?price .  "
			+ "}"
			+ "WHERE {"
			+ "?offer bsbm:product %s ." // 2
			+ "?offer bsbm:vendor ?vendor . "
			+ "?offer bsbm:deliveryDays ?deliveryDays . "
			+ "FILTER (?deliveryDays <= 3) "
			+ "?offer bsbm:validTo ?date . "
			+ "FILTER (?date > \"2008-02-10T00:00:00\"^^xsd:dateTime ) "
			+ "?offer bsbm:price ?price .  "
			+ "}"
			+ "LIMIT 5";
}
