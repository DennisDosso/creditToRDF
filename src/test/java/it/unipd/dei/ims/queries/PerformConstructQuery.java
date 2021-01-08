package it.unipd.dei.ims.queries;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.MyPaths;

/** Performs a CONSTRUCT query on an RDF triplestore saved on disk */
public class PerformConstructQuery {

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
//			String queryString = BSBMQueries.construct_test;
			String queryString = BSBMQuery1.construct_query_1;


			// execute the query
			GraphQuery query = conn.prepareGraphQuery(queryString);

			// A QueryResult is also an AutoCloseable resource, so make sure it gets
			// closed when done.
			try (GraphQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				for (Statement st: result) {
					// ... and print them out
					System.out.println(st);
				}
			}
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
	}
}
