package execution;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;

/** A class that contains methods to produce the data to, in turn, be able to create queries.
 * 
 * Step 0.1. Necessary to have values to build queries
 * <p>
 * NB the limits are necessary due to the fact that, otherwise, when applied to big databases, these queries require a too big execution time. 
 * This may result in days just to build the random queries. Therefore, we limit the number of possible different queries that
 * we produce.<br>
 * Always remember, however, that the number of queries at your disposal needs to be bigger than the number that you use as denominator
 * to compute the standard deviation of the normal distribution we are using. 
 * <p>
 * Also, we removed the ORDER BY clause from the queries that were used since this clause requires many computations in-RAM, 
 * that in the case of bigger databases may halt the execution, and cause out of memory exceptions.  
 * */
public class ProduceValuesToPerformQueries {
	
	public static String queryClass1 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"\n" + 
			"SELECT DISTINCT ?p ?pf1 ?pf2 \n" + 
			"WHERE { \n" + 
			" ?product rdfs:label ?label .\n" + 
			" ?product a ?p .\n" + 
			" ?product bsbm:productFeature ?pf1 . \n" + 
			" ?product bsbm:productFeature ?pf2 . \n" + 
			"?product bsbm:productPropertyNumeric1 ?value1 . \n" + 
			"	FILTER (?value1 > 300) \n" + 
			"	}\n" + 
//			"ORDER BY ?label\n" + // the order by operation, in a large DB, breaks the query 
			"LIMIT 100";
	
	public static String queryClass2="PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"\n" + 
			"SELECT ?p1 \n" + 
			"WHERE {\n" + 
			" ?p1 rdfs:label ?label .\n" + 
			"	?p1 rdfs:comment ?comment .\n" + 
			"	?p1 bsbm:producer ?p .\n" + 
			"	?p rdfs:label ?producer .\n" + 
			" ?p1 dc:publisher ?p . \n" + 
			"	?p1 bsbm:productFeature ?f .\n" + 
			"	?f rdfs:label ?productFeature .\n" + 
			"	?p1 bsbm:productPropertyTextual1 ?propertyTextual1 .\n" + 
			"	?p1 bsbm:productPropertyTextual2 ?propertyTextual2 .\n" + 
			" ?p1 bsbm:productPropertyTextual3 ?propertyTextual3 .\n" + 
			"	?p1 bsbm:productPropertyNumeric1 ?propertyNumeric1 .\n" + 
			"	?p1 bsbm:productPropertyNumeric2 ?propertyNumeric2 .\n" + 
			"	OPTIONAL { ?p1 bsbm:productPropertyTextual4 ?propertyTextual4 }\n" + 
			" OPTIONAL { ?p1 bsbm:productPropertyTextual5 ?propertyTextual5 }\n" + 
			" OPTIONAL { ?p1 bsbm:productPropertyNumeric4 ?propertyNumeric4 }\n" + 
			"} LIMIT 100";
	
	// class 3 was not used because it contains a negation
	
	// class 4 was not used because it contains a union operator, intended as "either one of two patterns"
	
	public static String queryClass5 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"\n" + 
			"SELECT DISTINCT ?p\n" + 
			"WHERE { \n" + 
			"	?product rdfs:label ?productLabel .\n" + 
			" FILTER (?p != ?product)\n" + // first parameter: product
			"	?p bsbm:productFeature ?prodFeature .\n" + // product
			"	?product bsbm:productFeature ?prodFeature .\n" + 
			"	?p bsbm:productPropertyNumeric1 ?origProperty1 .\n" + //product 
			"	?product bsbm:productPropertyNumeric1 ?simProperty1 .\n" + 
			"	FILTER (?simProperty1 < (?origProperty1 + 120) && ?simProperty1 > (?origProperty1 - 120))\n" + 
			"	?p bsbm:productPropertyNumeric2 ?origProperty2 .\n" + // product
			"	?product bsbm:productPropertyNumeric2 ?simProperty2 .\n" + 
			"	FILTER (?simProperty2 < (?origProperty2 + 170) && ?simProperty2 > (?origProperty2 - 170))\n" + 
			"}\n" + 
//			"ORDER BY ?productLabel\n" + 
			"LIMIT 100";
	
	public static String queryClass6 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"\n" + 
			"SELECT DISTINCT ?label\n" + 
			"WHERE {\n" + 
			"	?product rdfs:label ?label .\n" + 
			" ?product rdf:type bsbm:Product .\n" + 
			"} LIMIT 100";
	
	public static String queryClass7 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"select distinct ?p ?c \n" + 
			"WHERE {\n" + 
			"    ?p rdfs:label ?productLabel .\n" + // first parameter
//			"    OPTIONAL {" +
			"        ?offer bsbm:product ?p .\n" + // third parameter 
			"        ?offer bsbm:price ?price .\n" + 
			"        ?offer bsbm:vendor ?vendor .\n" + 
			"	     ?vendor rdfs:label ?vendorTitle .\n" + 
			"        ?vendor bsbm:country ?c.\n" + // second parameter, country - not used as parameter by BSBM, I decided to introduce it
			"        ?offer dc:publisher ?vendor . \n" + 
			"        ?offer bsbm:validTo ?date .\n" + 
			"        FILTER (?date > \"2008-03-01\"^^xsd:dateTime)\n" +
//			"    }" +
			"    OPTIONAL {\n" + 
			"	?review bsbm:reviewFor ?p .\n" + // fourth parameter 
			"	?review rev:reviewer ?reviewer .\n" + 
			"	?reviewer foaf:name ?revName .\n" + 
			"	?review dc:title ?revTitle .\n" + 
			" OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" + 
			"        OPTIONAL { ?review bsbm:rating2 ?rating2 . }}\n" + 
			"} LIMIT 100\n";
	
	public static String queryClass7Big = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"select distinct ?p \n" + 
			"WHERE {\n" + 
			"    ?p rdfs:label ?productLabel .\n" + // first parameter
			"        ?offer bsbm:product ?p .\n" + // second parameter 
			"        ?offer bsbm:price ?price .\n" + 
			"        ?offer bsbm:vendor ?vendor .\n" + 
			"        ?offer dc:publisher ?vendor . \n" + 
			"} LIMIT 100\n";
	
	public static String queryClass8 = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
			+ "SELECT ?p \n" + 
			"WHERE { \n" + 
			"	?review bsbm:reviewFor ?p .\n" + //param 
			"	?review dc:title ?title .\n" + 
			"	?review rev:text ?text .\n" + 
			"	FILTER langMatches( lang(?text), \"EN\" ) \n" + 
			"	?review bsbm:reviewDate ?reviewDate .\n" + 
			"	?review rev:reviewer ?reviewer .\n" + 
			"	?reviewer foaf:name ?reviewerName .\n" + 
			"	OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" + 
			"	OPTIONAL { ?review bsbm:rating2 ?rating2 . }\n" + 
			"	OPTIONAL { ?review bsbm:rating3 ?rating3 . }\n" + 
			"	OPTIONAL { ?review bsbm:rating4 ?rating4 . }\n" + 
			"}\n" + 
//			"ORDER BY DESC(?reviewDate)\n" + 
			"LIMIT 100";
	
	public static String queryClass8Big = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
			+ "SELECT ?p \n" + 
			"WHERE { \n" + 
			"	?review bsbm:reviewFor ?p .\n" + //param 
			"	?review dc:title ?title .\n" + 
			"	?review rev:text ?text ." +
			"	FILTER langMatches( lang(?text), \"EN\" ) \n" + 
			"	?review rev:reviewer ?reviewer .\n" + // to be removed, if it requires too much time 
			"}\n" + 
			"LIMIT 100";
	
	// class 9 was not used because it is of type DESCRIBE
	
	
	public static String queryClass10 = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"\n" + 
			"SELECT DISTINCT ?p\n" + 
			"WHERE {\n" + 
			"	?offer bsbm:product ?p .\n" + // param 
			"	?offer bsbm:vendor ?vendor .\n" + 
			" ?offer dc:publisher ?vendor .\n" + 
			"	?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .\n" + 
			"	?offer bsbm:deliveryDays ?deliveryDays .\n" + 
			"	FILTER (?deliveryDays <= 3)\n" + 
			"	?offer bsbm:price ?price .\n" + 
			" ?offer bsbm:validTo ?date .\n" + 
			" FILTER (?date > \"2008-02-10T00:00:00\"^^xsd:dateTime )\n" + // I fixed the date
			"}\n" + 
//			"ORDER BY xsd:double(str(?price))\n" + 
			"LIMIT 100";
	
	public static String queryClass10Big = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			"\n" + 
			"SELECT DISTINCT ?p\n" + 
			"WHERE {\n" + 
			"	?offer bsbm:product ?p .\n" + // param 
			"	?offer bsbm:vendor ?vendor .\n" + 
			"	?offer bsbm:deliveryDays ?deliveryDays .\n" + 
			"	FILTER (?deliveryDays <= 3)\n" + 
			" ?offer bsbm:validTo ?date .\n" + 
			" FILTER (?date > \"2008-02-10T00:00:00\"^^xsd:dateTime )\n" + 
			"?offer bsbm:price ?price .  " +
			"}\n" + 
			"LIMIT 100";

	public void printValuesForQueries(MyValues.QueryClass class_, String outputFile, String tripleStorePath) throws IOException {
		RepositoryConnection rc = TripleStoreHandler.openRepositoryAndConnection(tripleStorePath);
		
		Path p = Paths.get(outputFile);
		BufferedWriter w = Files.newBufferedWriter(p);
		
		
		w.write("query values\n");
		
		
		TupleQuery query = null;
		if(class_ == MyValues.QueryClass.ONE) {
			query = rc.prepareTupleQuery(queryClass1);			
		} else if (class_ == MyValues.QueryClass.FIVE) {
			query = rc.prepareTupleQuery(queryClass5);
		} else if (class_ == MyValues.QueryClass.SEVEN) {
			query = rc.prepareTupleQuery(queryClass7);
		} else if (class_ == MyValues.QueryClass.EIGHT) {
			query = rc.prepareTupleQuery(queryClass8);
		} else if (class_ == MyValues.QueryClass.TEN) {
			query = rc.prepareTupleQuery(queryClass10);
		}
		
		
		try(TupleQueryResult res = query.evaluate()) {
			for (BindingSet solution: res) {
//				System.out.println(solution.getValue("p") + "," + solution.getValue("pf1") + "," + solution.getValue("pf2"));
				if(class_ == MyValues.QueryClass.ONE) {
					w.write(solution.getValue("p") + "," + solution.getValue("pf1") + "," + solution.getValue("pf2"));
					w.newLine();			
				} else if (class_ == MyValues.QueryClass.FIVE) {
					w.write(solution.getValue("p") + "");
					w.newLine();
				} else if (class_ == MyValues.QueryClass.SEVEN) {
					w.write(solution.getValue("p") + "," + solution.getValue("c"));
					w.newLine();
				} else if (class_ == MyValues.QueryClass.EIGHT) {
					w.write(solution.getValue("p") + "");
					w.newLine();
				} else if (class_ == MyValues.QueryClass.TEN) {
					w.write(solution.getValue("p") + "");
					w.newLine();
				}
				
			}
			w.close();
		}
	}
	
	public void close() {
		TripleStoreHandler.closeRepositoryAndConnextion();
	}
	
	
	public static void main(String[] args) throws IOException {
		MyValues.setup();
		MyPaths.setup();
		
		ProduceValuesToPerformQueries execution = new ProduceValuesToPerformQueries();
		execution.printValuesForQueries(MyValues.QUERYCLASS, MyPaths.values_path,  MyPaths.querying_index);
		execution.close();
		System.out.println("done");
	}
}
