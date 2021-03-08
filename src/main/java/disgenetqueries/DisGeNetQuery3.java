package disgenetqueries;

public class DisGeNetQuery3 {
	

	public static String select = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT * \n" +  
			"WHERE {\n" + 
			"	%s sio:SIO_000628\n" + // 1 
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	OPTIONAL {\n" + 
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" + 
			"	}\n" + 
			"} LIMIT 20";
	
	public static String select_named = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT *\n" +  
			"WHERE {GRAPH %here {\n" + 
			"	%s sio:SIO_000628\n" + //1 
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	OPTIONAL {\n" + 
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" + 
			"	}\n" + 
			"}"
			+ "} LIMIT 20";
	
	public static String construct = DisGeNetQuery1.prefixes +
			"CONSTRUCT {" +
			"	%s sio:SIO_000628\n" + //1 
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" +
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" +
			"}\n" + 
			"WHERE {GRAPH %here {\n" + 
			"	%s sio:SIO_000628\n" + // 2
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	OPTIONAL {\n" + 
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" + 
			"	}\n" + 
			"}"
			+ "} LIMIT 200";
			
}
