package rdf4j.examples.from.the.website;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

/**
 * RDF Tutorial example 14: Adding an RDF file directly to the database
 *
 *For further information visit: https://rdf4j.org/documentation/programming/repository/
 *
 *
 * @author Jeen Broekstra
 */
public class Example14AddRDFToDatabase {

	public static void main(String[] args)
			throws IOException {
		// Create a new Repository in Secondary Memory
		File dataDir = new File("/Users/anonymous/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/testDatabase");
//		Repository db = new SailRepository(new MemoryStore(dataDir));
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();

		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {
			String filename = "/Users/anonymous/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/test/textut.txt";
			
//			try(FileInputStream input = new FileInputStream(filename)) {
//				// add the RDF data from the inputstream directly to our database
//				conn.add(input, "", RDFFormat.TURTLE);
//			}

			// let's check that our data is actually in the database
			try (RepositoryResult<Statement> result = conn.getStatements(null, null, null);) {
				while (result.hasNext()) {
					Statement st = result.next();
					System.out.println("db contains: " + st);
				}
			}
		} finally {
			// before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
	}
}