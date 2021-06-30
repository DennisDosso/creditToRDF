package it.unipd.dei.ims.queries;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;


/**
 * 
 * 
 * java -cp creditToRdf-1.0.jar:lib/* it.unipd.dei.ims.queries.PerformConstructQuery
 * 
 * */
public class PerformConstructQuery {

	public static void main(String[] args) {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX void: <http://rdfs.org/ns/void#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX so: <http://purl.obolibrary.org/obo/SO_> PREFIX ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> PREFIX up: <http://purl.uniprot.org/core/> PREFIX dcat: <http://www.w3.org/ns/dcat#> PREFIX dctypes: <http://purl.org/dc/dcmitype/> PREFIX wi: <http://http://purl.org/ontology/wi/core#> PREFIX eco: <http://http://purl.obolibrary.org/obo/eco.owl#> PREFIX prov: <http://http://http://www.w3.org/ns/prov#> PREFIX pav: <http://http://http://purl.org/pav/> PREFIX obo: <http://purl.obolibrary.org/obo/> PREFIX dto: <http://diseasetargetontology.org/dto/> "
				+ "CONSTRUCT {\n" + 
				"<http://rdf.disgenet.org/resource/gda/DGN7042f2c97bd93d137d69416d0017cd2e> sio:SIO_000628 ?disease,?gene .\n" + 
				"	?gda2 sio:SIO_000628 ?disease2,?gene .\n" + 
				"	?disease dcterms:title ?diseaseName .\n" + 
				"	?disease2 dcterms:title ?diseaseName2 .\n" + 
				"	}\n" + 
				"WHERE {\n" + 
				"	<http://rdf.disgenet.org/resource/gda/DGN7042f2c97bd93d137d69416d0017cd2e> sio:SIO_000628 ?disease,?gene .\n" + 
				"	?gda2 sio:SIO_000628 ?disease2,?gene .\n" + 
				"	?disease dcterms:title ?diseaseName .\n" + 
				"	?disease2 dcterms:title ?diseaseName2 .\n" + 
				"FILTER (?disease != ?disease2)\n" + 
				"FILTER (<http://rdf.disgenet.org/resource/gda/DGN7042f2c97bd93d137d69416d0017cd2e> != ?gda2)\n" + 
				"}\n" + 
				"LIMIT 50";
		
//		query = "CONSTRUCT  WHERE {?s ?p ?o} LIMIT 10";


		String path = "/nfsd/exadata/an/creditToRDF/DisGeNet/triplestore";
		
		TripleStoreHandler.initRepository(path);
		Repository repository = TripleStoreHandler.getRepository();
		
		System.out.println("let's go!!!");

		try(RepositoryConnection conn = repository.getConnection()) {
			//execute the query
			GraphQuery graphQuery = conn.prepareGraphQuery(query);
			try (GraphQueryResult result = graphQuery.evaluate()) {
				// we just iterate over all solutions in the result...
				for (Statement st: result) {
					// get the three elements 
					String subject = st.getSubject().stringValue();
					String predicate = st.getPredicate().stringValue();
					String object = st.getObject().stringValue();
					
					System.out.println(subject + " " + predicate + " " + object);
				}
			}
		}
	}

}
