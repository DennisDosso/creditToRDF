package it.unipd.dei.ims.queries;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.Queries;

/** Performs a count query to know all the triples in a database
 *
 *
 * path.properties
 * <li>querying_index</li>
 *
 *
 * <code>
 *     java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/queries/PerformCountQuery
 * </code>
 * */
public class PerformCountQuery {

	public static void main(String[] args)
			throws IOException {
		// open already existing repository
		new MyPaths();
		String path = MyPaths.querying_index;
		
		
		File dataDir = new File(path);
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();
		
		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {
			

			// a simple query
			String queryString = Queries.count_query;
			

			// execute the query
			TupleQuery query = conn.prepareTupleQuery(queryString);

			// A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
			try (TupleQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				while (result.hasNext()) {
					BindingSet solution = result.next();
					System.out.println("total number of triples = " + solution.getValue("triples"));
				}
			}
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
	}
}
