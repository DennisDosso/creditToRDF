package disgenetqueries;

public class DisGeNetQuery4 {

	public static String select = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * \n" + 
			"WHERE {\n" + 
			"	%s sio:SIO_000628 ?gene, ?disease ;\n" + //1 
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	FILTER (?score >= 0.4)\n" + 
			"}\n" + 
			"ORDER BY DESC(?score)\n" + 
			"LIMIT 20";
	
	public static String select_named = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * \n" + 
			"WHERE { GRAPH %here {\n" + 
			"	%s sio:SIO_000628 ?gene, ?disease ;\n" + //1
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	FILTER (?score >= 0.4)\n" + 
			"}"
			+ "}\n" + 
			"ORDER BY DESC(?score)\n" + 
			"LIMIT 20";
	
	public static String construct = DisGeNetQuery1.prefixes + 
			"CONSTRUCT{" +
			"	%s sio:SIO_000628 ?gene, ?disease ;\n" + //1
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n"
			+ "} \n" + 
			"WHERE {\n" + 
			"	%s sio:SIO_000628 ?gene, ?disease ;\n" + //1
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	FILTER (?score >= 0.4)\n" + 
			"}" + 
			"ORDER BY DESC(?score)\n" + 
			"LIMIT 200";
}
