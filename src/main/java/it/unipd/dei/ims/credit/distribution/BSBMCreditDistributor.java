package it.unipd.dei.ims.credit.distribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;

import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.BSBMQuery8;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;

/** Contains methods to distribute credit via queries to the relational BSBM triplestore relational table
 * 
 * Step 3 - you need to have the values saved in a csv file to create the SPARQL queries. 
 * You have many methods, one per class of query, to perform different distributions, i.e.
 * different 'color' of credit.
 * 
 * 
 * @author Dennis Dosso
 * */
public class BSBMCreditDistributor {


	private final String updateTripleStroreWithTimes = "UPDATE %s.triplestore\n" + 
			"SET credit= credit + ? \n" + 
			"WHERE subject=? AND predicate=? AND \"object\"=?;\n" + 
			"";


	/** The constructor initializes the connections to the RDF triplestore and relational database.
	 * 
	 * @param tripleStorePath path of the triple store. Use MyPaths.index_path as value
	 * @param jdbcString string to connect to the relational DB. Use RDB.produceJdbcString() to get this value
	 * 
	 * @exception SQLException in case unable to open connection to relational database
	 * */
	public BSBMCreditDistributor(String tripleStorePath, String jdbcString) throws SQLException {
		// init and open connection to triplestore
		TripleStoreHandler.openRepositoryAndConnection(tripleStorePath);

		// open connection to relational DB - also set autocommit to false
		ConnectionHandler.createConnectionAsOwner(jdbcString, BSBMCreditDistributor.class.getName()).setAutoCommit(false);;
	}



	/** Hub method to decide the class of queries that you want to execute */
	public void distributeCreditForQueryOfClass(MyValues.QueryClass class_, String values_path, String times_path) {
		if(class_ == MyValues.QueryClass.ONE) {
			this.executeClass1(values_path, times_path);
		} else if (class_ == MyValues.QueryClass.EIGHT) {
			this.executeClass8(values_path, times_path);
		}
		// todo - serve fare le altre classi di query
	}



	/** Execute queries of class 1 many times, as specified in the values_path and times_path files*/
	public void executeClass1(String values_path, String times_path) {

		// open the support files
		Path valuesPath = Paths.get(values_path);
		Path timesPath = Paths.get(times_path);
		BufferedReader valuesReader = null, timesReader = null;

		try {
			// open the readers
			valuesReader = Files.newBufferedReader(valuesPath);
			timesReader = Files.newBufferedReader(timesPath);

			String valuesLine, timesLine = null;
			int counter = 0;
			
			// need to delete first line
			valuesReader.readLine();

			// read the values for this query and the number of times we simulate its execution
			while((valuesLine = valuesReader.readLine()) != null) {
				timesLine = timesReader.readLine();
				int times = Integer.parseInt(timesLine);

				String[] values = valuesLine.split(",");
				String param1 = values[0];
				String param2 = values[1];
				String param3 = values[2];
				String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries

				// execute the query "many times"
				try {
					this.performQuery1withParameters(param1, param2, param3, param4, times);
				} catch (SQLException e) {
					System.err.println("error at query 1");
					e.printStackTrace();
				}
				
				counter++;
				if (counter % 100 == 0)
					System.out.println("Executed " + counter + " SQL queries");
			}
			System.out.println("credit distribution concluded");

			valuesReader.close();
			timesReader.close();
		} catch (IOException e) {
			e.printStackTrace();

		} finally {

		}

	}
	
	/** Execute queries of class 8 many times, as specified in the values_path and times_path files*/
	public void executeClass8(String values_path, String times_path) {

		// open the support files
		Path valuesPath = Paths.get(values_path);
		Path timesPath = Paths.get(times_path);
		BufferedReader valuesReader = null, timesReader = null;

		try {
			// open the readers
			valuesReader = Files.newBufferedReader(valuesPath);
			timesReader = Files.newBufferedReader(timesPath);

			String valuesLine, timesLine = null;
			int counter = 0;
			
			// need to go past the first line
			valuesReader.readLine();

			// read the values for this query and the number of times we simulate its execution
			while((valuesLine = valuesReader.readLine()) != null) {
				timesLine = timesReader.readLine();
				int times = Integer.parseInt(timesLine);

				String[] values = valuesLine.split(",");
				String param1 = values[0];// only one parameter here
			

				// execute the query "many times"
				try {
					this.performQuery8withParameters(param1, times);
				} catch (SQLException e) {
					System.err.println("error at query 1");
					e.printStackTrace();
				}
				
				counter++;
				if (counter % 100 == 0)
					System.out.println("Executed " + counter + " SQL queries");
			}
			System.out.println("credit distribution concluded");

			valuesReader.close();
			timesReader.close();
		} catch (IOException e) {
			e.printStackTrace();

		} finally {

		}

	}

	/** performs query 1 on the triple store of the instance. 
	 * It also  distributes the credit on the relational database
	 * */
	public void performQuery1withParameters(String param1, String param2, String param3, String param4, int times) throws SQLException {
		// get the query
		String query = BSBMQuery1.parameter_query_1;

		//prepare the query inserting the desired values
		query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">",
				"<" + param1 + ">", "<" + param2+ ">", "<" + param3 + ">", param4);

		// prepare a query with the connection to the triplestore 
		GraphQuery graphQuery = TripleStoreHandler.getRepositoryConnection().prepareGraphQuery(query);

		//evaluate the SPARQL query (only once to get the used triples)
		try (GraphQueryResult result = graphQuery.evaluate()) {

			String q = String.format(this.updateTripleStroreWithTimes, RDB.schema);// set the right schema to the SQL query
			PreparedStatement stmt = ConnectionHandler.getConnection().prepareStatement(q);

			// now for each triple
			for (Statement st: result) {
				// get the three elements of the triple 
				String subject = st.getSubject().stringValue();
				String predicate = st.getPredicate().stringValue();
				String object = st.getObject().stringValue();

				// prepare the query to update the credit to the database
				stmt.setInt(1, times); // times this query is executed, thus the triple is used
				stmt.setString(2, subject);
				stmt.setString(3, predicate);
				stmt.setString(4, object);

				stmt.addBatch();
				//				int res = stmt.executeUpdate();
				//				System.out.println(res);
			}
			stmt.executeBatch();
			ConnectionHandler.getConnection().commit();
		}
	}

	
	/** performs query 1 on the triple store of the instance. 
	 * It also  distributes the credit on the relational database
	 * */
	public void performQuery8withParameters(String param1, int times) throws SQLException {
		// get the query
		String query = BSBMQuery8.parametrixed_construct_query;

		//prepare the query inserting the desired values
		query = String.format(query, "<" + param1 + ">", "<" + param1 + ">");

		// prepare a query with the connection to the triplestore 
		GraphQuery graphQuery = TripleStoreHandler.getRepositoryConnection().prepareGraphQuery(query);

		//evaluate the SPARQL query (only once to get the used triples)
		try (GraphQueryResult result = graphQuery.evaluate()) {

			String q = String.format(this.updateTripleStroreWithTimes, RDB.schema);
			PreparedStatement stmt = ConnectionHandler.getConnection().prepareStatement(q);

			// now for each triple
			for (Statement st: result) {
				// get the three elements of the triple 
				String subject = st.getSubject().stringValue();
				String predicate = st.getPredicate().stringValue();
				
				String object = st.getObject().stringValue();
				// since the object may be text, and in the RDB we put strings with a limited quantity of text, we need to truncate the string if necessary
				object = object.substring(0, Math.min(object.length(), 254));

				// prepare the query to update the credit to the database
				stmt.setInt(1, times); // times this query is executed, thus the triple is used
				stmt.setString(2, subject);
				stmt.setString(3, predicate);
				stmt.setString(4, object);

				stmt.addBatch();
			}
			stmt.executeBatch();// execute all the updates
			ConnectionHandler.getConnection().commit();
			
			stmt.close();
		} finally {
			
		}
	}


	public void shutDown() {
		try {
			ConnectionHandler.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		TripleStoreHandler.closeRepositoryAndConnextion();
	}







	/** test main */
	public static void main(String[] args) {
		try {
			RDB.setup();// read parameters for connection to RDB
			MyPaths.setup(); // read parameters for paths
			MyValues.setup();
			BSBMCreditDistributor distributor = new BSBMCreditDistributor(MyPaths.index_path, RDB.produceJdbcString());
			
			distributor.distributeCreditForQueryOfClass(MyValues.QUERYCLASS, MyPaths.values_path, MyPaths.times_file_path);
			distributor.shutDown();
			
			System.out.println("done");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
