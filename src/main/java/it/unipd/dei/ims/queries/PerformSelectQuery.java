package it.unipd.dei.ims.queries;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.Queries;

public class PerformSelectQuery {

	public static void main(String[] args)
			throws IOException {
		// open already existing repository
		new MyPaths();
		String path = MyPaths.querying_index;
//		path = MyPaths.reduced_index_path;


		File dataDir = new File(path);
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();

		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {


			// a simple query

			String queryString;

			//			String queryString = "SELECT * WHERE {<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer4/Product175> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o ;"
			//					+ "<http://www.w3.org/2000/01/rdf-schema#label> ?label} limit 100";


			//			String queryString = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" + 
			//					"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			//					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			//					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			//					"\n" + 
			//					"SELECT  *\n" + 
			//					"WHERE { \n" + 
			//					"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer4/Product175> rdfs:label ?label ;\n" +
			//					"     	a bsbm-inst:ProductType1 ;\n" + 
			//					"    	bsbm:productFeature bsbm-inst:ProductFeature100 ;\n" + 
			//					"        bsbm:productFeature bsbm-inst:ProductFeature100 ;\n" + 
			//					"        bsbm:productPropertyNumeric1 ?value1 .\n" + 
			//					"    FILTER (?value1 > 300). \n" +  
			//					"	}\n" + 
			//					"ORDER BY ?label\n" + 
			//					"LIMIT 1000";

			// COUNT query
//						queryString = "SELECT (COUNT(*) as ?triple_count) WHERE { GRAPH <http://named/graph/query10> {?s ?p ?o} } LIMIT 1000";
			queryString = "SELECT (COUNT(*) as ?triple_count) WHERE {?s ?p ?o}  LIMIT 1000";

			//			queryString = "SELECT * WHERE "
			////					+ "{ GRAPH <http://example/bookStore> "
			//					+ "{ <http://example/book1>  ?p ?o}"
			////					+ "} LIMIT 1000"
			//					;

			//query number 8
			//			queryString = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			//					"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			//					"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			//					"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
			//					+ "SELECT ?text \n" + 
			//					"WHERE { \n" + 
			//					"	?review bsbm:reviewFor ?ProductXYZ .\n" + 
			//					"	?review dc:title ?title .\n" + 
			//					"	?review rev:text ?text .\n" + 
			//					"	FILTER langMatches( lang(?text), \"EN\" ) \n" + 
			//					"	?review bsbm:reviewDate ?reviewDate .\n" + 
			//					"	?review rev:reviewer ?reviewer .\n" + 
			//					"	?reviewer foaf:name ?reviewerName .\n" + 
			//					"	OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" + 
			//					"	OPTIONAL { ?review bsbm:rating2 ?rating2 . }\n" + 
			//					"	OPTIONAL { ?review bsbm:rating3 ?rating3 . }\n" + 
			//					"	OPTIONAL { ?review bsbm:rating4 ?rating4 . }\n" + 
			//					"}\n" + 
			//					"ORDER BY DESC(?reviewDate)\n" + 
			//					"LIMIT 20"
			//					;
			//
			//			queryString = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" + 
			//					"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" + 
			//					"PREFIX rev: <http://purl.org/stuff/rev#>\n" + 
			//					"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
			//					+ "SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4 \n" + 
			//					"WHERE { GRAPH <http://named/graph/query8> {\n" + 
			//					"	?review bsbm:reviewFor ?ProductXYZ .\n" + 
			//					"	?review dc:title ?title .\n" + 
			//					"	?review rev:text ?text .\n" + 
			//					"	FILTER langMatches( lang(?text), \"EN\" ) \n" + 
			//					"	?review bsbm:reviewDate ?reviewDate .\n" + 
			//					"	?review rev:reviewer ?reviewer .\n" + 
			//					"	?reviewer foaf:name ?reviewerName .\n" + 
			//					"	OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" + 
			//					"	OPTIONAL { ?review bsbm:rating2 ?rating2 . }\n" + 
			//					"	OPTIONAL { ?review bsbm:rating3 ?rating3 . }\n" + 
			//					"	OPTIONAL { ?review bsbm:rating4 ?rating4 . }\n" + 
			//					"}}\n" + 
			//					"ORDER BY DESC(?reviewDate)\n" + 
			//					"LIMIT 20"
			//					;


			// execute the query
			TupleQuery query = conn.prepareTupleQuery(queryString);

			// A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
			try (TupleQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				while (result.hasNext()) {
					BindingSet solution = result.next();
					System.out.println(solution);
					//					System.out.println("total number of triples = " + solution.getValue("triples"));
				}
			}
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
	}
}
