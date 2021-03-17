package disgenetqueries;

public class DisGeNetQuery1 {
	
	public static String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
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
			"PREFIX dto: <http://diseasetargetontology.org/dto/> \n";

	public static String select = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX sio: <http://semanticscience.org/resource/> "
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX dcterms: <http://purl.org/dc/terms/> "
			+ "PREFIX void: <http://rdfs.org/ns/void#> "
			+ "SELECT * "
			+ "WHERE {"
			+ "%s rdf:type ?type; " // 1
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ " dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. "
			+ "FILTER(?type=sio:SIO_001122) } "
			+ "LIMIT 20";
	
	public static String select_named = prefixes 
			+ "SELECT * "
			+ "WHERE { GRAPH %here {"
			+ "%s rdf:type ?type; " // 1
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ " dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. "
			+ "FILTER(?type=sio:SIO_001122) } "
			+ "}"
			+ "LIMIT 20";
	
	public static String construct = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX sio: <http://semanticscience.org/resource/> "
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX dcterms: <http://purl.org/dc/terms/> "
			+ "PREFIX void: <http://rdfs.org/ns/void#> "
			+ "CONSTRUCT{ "
			+ "%s rdf:type ?type; " // 1
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ "dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. }"
			+ "WHERE { "
			+ "%s rdf:type ?type; " // 2
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ " dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. "
			+ "FILTER(?type=sio:SIO_001122) } ";
	
	
}
