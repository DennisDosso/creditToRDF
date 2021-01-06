package it.unipd.dei.ims.queries;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.Queries;

/**
 * RDF Tutorial example 15: executing a simple SPARQL query on the database
 *
 * @author Jeen Broekstra
 */
public class PerformSPARQLQuery {

	public static void main(String[] args)
			throws IOException {
		// open already existing repository
		String path = MyPaths.index_path;
		
		
		File dataDir = new File(path);
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();
		
		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {
			

			// a simple query
			String queryString = Queries.test_query;
			
			
			
//			String queryString = "PREFIX ex: <http://example.org/> \n";
//			queryString += "PREFIX foaf: <" + FOAF.NAMESPACE + "> \n";
//			queryString += "SELECT ?s ?n \n";
//			queryString += "WHERE { \n";
//			queryString += "    ?s a ex:Artist; \n";
//			queryString += "       foaf:firstName ?n .";
//			queryString += "}";

			// execute the query
			TupleQuery query = conn.prepareTupleQuery(queryString);

			// A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
			try (TupleQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				while (result.hasNext()) {
					BindingSet solution = result.next();
					// ... and print out the value of the variable binding for ?s and ?n
					System.out.println("?s = " + solution.getValue("s"));
					System.out.println("?p = " + solution.getValue("p"));
					System.out.println("?o = " + solution.getValue("o"));
				}
			}
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
	}
}