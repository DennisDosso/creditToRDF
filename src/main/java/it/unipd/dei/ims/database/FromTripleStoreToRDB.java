package it.unipd.dei.ims.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.Queries;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.RDBQueries;

/** Given a triple store, takes the triple and inserts them in a relational DB.
 * You need to already have a DB and a table in place
 * 
 * step 2
 * 
 * @author Dennis Dosso
 * @license Apache 2.0
 * 
 * 
 * */
public class FromTripleStoreToRDB {

	public static void main(String[] args) throws SQLException {
		//open the triplestore
		// open already existing repository
		String path = MyPaths.index_path;


		File dataDir = new File(path);
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.init();

		// open connection to relational database
		String jdbcString = "jdbc:postgresql://" + RDB.host + ":" + RDB.port + "/" + RDB.database + "?user=" + RDB.user + "&password=" + RDB.password; 
		Connection cc = ConnectionHandler.createConnection(jdbcString);
		cc.setAutoCommit(false);


		// Open a connection to triplestore
		try (RepositoryConnection conn = db.getConnection()) {


			// prepare query to get all triples
			String queryString = Queries.get_all_triples;
			TupleQuery query = conn.prepareTupleQuery(queryString);
			
			//prepare the query to insert a triple
			PreparedStatement stmt = cc.prepareStatement(RDBQueries.insert_triple);
			
			int counter = 0;
			System.out.print("Number of tuples inserted so far: ");

			try (TupleQueryResult result = query.evaluate()) { // for each triple
				// we just iterate over all solutions in the result...
				while (result.hasNext()) {
					counter++;
					
					// get the tuple
					BindingSet solution = result.next();
					// prepare the insert - we need to insert strings at most long 255 characters
					String s = solution.getValue("s").stringValue();
					stmt.setString(1, solution.getValue("s").stringValue().substring(0, Math.min(s.length(), 254)));
					
					s = solution.getValue("p").stringValue();
					stmt.setString(2, solution.getValue("p").stringValue().substring(0, Math.min(s.length(), 254)));
					
					s = solution.getValue("o").stringValue();
					stmt.setString(3, solution.getValue("o").stringValue().substring(0, Math.min(s.length(), 254)));
					
					stmt.setDouble(4, 0);
					
					stmt.addBatch();
					if(counter % 1000 == 0) {
						int[] res = stmt.executeBatch();
//						System.out.println("inserted " + res.length + " tuples");
						cc.commit();
						if(counter % 10000 == 0) {
							System.out.print(counter + ", ");
						}
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				cc.rollback();
			}
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
			ConnectionHandler.closeConnection();
		}

		System.out.println("done");

	}
}