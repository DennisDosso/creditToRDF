package it.unipd.dei.ims.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

/** Reads one RDF file in input, and saves it in a triple store
 * using rdf4j
 * 
 * 
 * step 1
 * */
public class FromTurtleToTripleStore {

	// name of the file in input
	static String filename = "/Users/dennisdosso/eclipse-workspace/bsbmtools-0.2/jar/dataset.ttl";

	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// open repository where I save the RDF file - we use the implementation that uses the native on disk repository implementation
		// everything is done on secondary memory. Maybe we can try something on primary memory sometimes
		File dataDir = new File("/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/bsbm100k");
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();
		
		try (RepositoryConnection conn = db.getConnection()) {
			
			// read data from file in Turtle format and save them in the triplestore
			try(FileInputStream input = new FileInputStream(filename)) {
				// add the RDF data from the inputstream directly to our database
				conn.add(input, "", RDFFormat.TURTLE);
			}

			// let's check that our data is actually in the database
//			try (RepositoryResult<Statement> result = conn.getStatements(null, null, null);) {
//				while (result.hasNext()) {
//					Statement st = result.next();
//					System.out.println("db contains: " + st);
//				}
//			}
			
		} finally {
			// before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}

		System.out.println("done");
		
	}
}
