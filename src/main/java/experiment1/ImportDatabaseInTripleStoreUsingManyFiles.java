package experiment1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;

public class ImportDatabaseInTripleStoreUsingManyFiles {

	public ImportDatabaseInTripleStoreUsingManyFiles() {
		MyPaths.setup();
		MyValues.setup();
	}

	public void importInTripleStore() throws FileNotFoundException, IOException {
		System.out.println("importing file in a triplestore...");

		// path of the directory where the turtle files are located
		String inputDirectory = MyPaths.fragmentsOutputDirectory;
		
		int i = 0;

		// where we save the index
		File dataDir = new File(MyPaths.index_path);
		// string with the indexes that we use
		String indexes = MyValues.indexString; // the indexes

		Repository db = new SailRepository(new NativeStore(dataDir, indexes));
		db.init();
		RepositoryConnection conn = db.getConnection();

		while(true) {
			String file = inputDirectory + i + ".ttl";
			File f = new File(file);


			if(!f.exists())
				break;// ended the files

			// read data from file in Turtle format and save them in the triplestore
			try(FileInputStream input = new FileInputStream(file)) {
				// add the RDF data from the inputstream directly to our database
				conn.add(input, "", RDFFormat.TURTLE);
				System.out.println("added to the index the file " + file);
				conn.commit();
			}
			i++;
		}
		
		conn.close();
		db.shutDown();

	}

	public static void main(String[] args)  {
		ImportDatabaseInTripleStoreUsingManyFiles execution = new ImportDatabaseInTripleStoreUsingManyFiles();
		try {
			execution.importInTripleStore();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("done");
	}
}
