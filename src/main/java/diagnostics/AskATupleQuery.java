package diagnostics;

import java.io.File;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyQueries;
import it.unipd.dei.ims.data.MyValues;


/** Use this main to ask a tuple query to an index. 
 * 
 * Used properties:<br>
 * <li> querying_index (path.properties)
 * <li> tuple.query (queries.properties)
 * <br>
 * To execute:
 * java -cp creditToRdf-1.0.jar:lib/* diagnostics.AskATupleQuery
 * 
 * */
public class AskATupleQuery {

	public static void main(String[] args) {
		MyValues.setup();
		MyPaths.setup();
		MyQueries.setup();
		
		File dataDir = new File( MyPaths.querying_index);
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();
		
		long start = System.currentTimeMillis();
		long elapsed = 0;
		
		try (RepositoryConnection conn = db.getConnection()) {
			String queryString = MyQueries.tupleQuery;
			
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

			elapsed = System.currentTimeMillis() - start;
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
		
		System.out.println("done in " + elapsed + " ms");
	}
}
