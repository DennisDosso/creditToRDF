package it.unipd.dei.ims.queries;

import java.io.File;

import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.MyPaths;

public class TestAsk {

	public static void main(String[] args) {

		new MyPaths();
		String path = MyPaths.querying_index;
		
		File dataDir = new File(path);
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();

		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {
			String queryString = "ASK WHERE {<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite1/Review1229>\n" + 
					"<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/rating1> \"1\"^^xsd:integer}";
			
			BooleanQuery q = conn.prepareBooleanQuery(queryString);
			
			System.out.print(q.evaluate());
		}
	}

}
