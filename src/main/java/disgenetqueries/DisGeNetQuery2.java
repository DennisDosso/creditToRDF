package disgenetqueries;

public class DisGeNetQuery2 {

	public static String select = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX dcterms: <http://purl.org/dc/terms/>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
			"PREFIX void: <http://rdfs.org/ns/void#>\n" + 
			"PREFIX sio: <http://semanticscience.org/resource/>\n" + 
			"PREFIX so: <http://purl.obolibrary.org/obo/SO_>\n" + 
			"PREFIX ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>\n" + 
			"PREFIX up: <http://purl.uniprot.org/core/>\n" + 
			"PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" + 
			"PREFIX dctypes: <http://purl.org/dc/dcmitype/>\n" + 
			"PREFIX wi: <http://http://purl.org/ontology/wi/core#>\n" + 
			"PREFIX eco: <http://http://purl.obolibrary.org/obo/eco.owl#>\n" + 
			"PREFIX prov: <http://http://http://www.w3.org/ns/prov#>\n" + 
			"PREFIX pav: <http://http://http://purl.org/pav/>\n" + 
			"PREFIX obo: <http://purl.obolibrary.org/obo/>\n" + 
			"PREFIX dto: <http://diseasetargetontology.org/dto/>"
			+ "SELECT DISTINCT * \n" + 
			"	WHERE {\n" + 
			"		%s sio:SIO_000628 ?gene,?disease .\n" + // 1 
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" + 
			"	}\n" + 
			"	LIMIT 20";
	
	public static String select_named = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX dcterms: <http://purl.org/dc/terms/>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
			"PREFIX void: <http://rdfs.org/ns/void#>\n" + 
			"PREFIX sio: <http://semanticscience.org/resource/>\n" + 
			"PREFIX so: <http://purl.obolibrary.org/obo/SO_>\n" + 
			"PREFIX ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>\n" + 
			"PREFIX up: <http://purl.uniprot.org/core/>\n" + 
			"PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" + 
			"PREFIX dctypes: <http://purl.org/dc/dcmitype/>\n" + 
			"PREFIX wi: <http://http://purl.org/ontology/wi/core#>\n" + 
			"PREFIX eco: <http://http://purl.obolibrary.org/obo/eco.owl#>\n" + 
			"PREFIX prov: <http://http://http://www.w3.org/ns/prov#>\n" + 
			"PREFIX pav: <http://http://http://purl.org/pav/>\n" + 
			"PREFIX obo: <http://purl.obolibrary.org/obo/>\n" + 
			"PREFIX dto: <http://diseasetargetontology.org/dto/>"
			+ "SELECT DISTINCT * \n" + 
			"	WHERE {GRAPH %here {\n" + 
			"		%s sio:SIO_000628 ?gene,?disease .\n" + // 1 
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" + 
			"	}"
			+ "}\n" + 
			"	LIMIT 20";
	
	public static String construct = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX dcterms: <http://purl.org/dc/terms/>\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
			"PREFIX void: <http://rdfs.org/ns/void#>\n" + 
			"PREFIX sio: <http://semanticscience.org/resource/>\n" + 
			"PREFIX so: <http://purl.obolibrary.org/obo/SO_>\n" + 
			"PREFIX ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>\n" + 
			"PREFIX up: <http://purl.uniprot.org/core/>\n" + 
			"PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" + 
			"PREFIX dctypes: <http://purl.org/dc/dcmitype/>\n" + 
			"PREFIX wi: <http://http://purl.org/ontology/wi/core#>\n" + 
			"PREFIX eco: <http://http://purl.obolibrary.org/obo/eco.owl#>\n" + 
			"PREFIX prov: <http://http://http://www.w3.org/ns/prov#>\n" + 
			"PREFIX pav: <http://http://http://purl.org/pav/>\n" + 
			"PREFIX obo: <http://purl.obolibrary.org/obo/>\n" + 
			"PREFIX dto: <http://diseasetargetontology.org/dto/>"
			+ "CONSTRUCT {" +
			"		%s sio:SIO_000628 ?gene,?disease .\n" + // 1 
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" 
			+ "} \n" + 
			"	WHERE {\n" + 
			"		%s sio:SIO_000628 ?gene,?disease .\n" + // 1 
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" + 
			"	}\n" + 
			"	LIMIT 200";
}
