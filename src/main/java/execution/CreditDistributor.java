package execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;

import it.unipd.dei.ims.credit.distribution.BSBMCreditDistributor;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;


/**
 * 
 * New Step 3
 * 
 * */
public class CreditDistributor {


	private final String updateHits = "UPDATE %s.triplestore\n" + 
			"SET hits = hits + ? \n" + 
			"WHERE subject=? AND predicate=? AND \"object\"=?;\n" + 
			"";

	private final String selectHitTuples = "select subject, predicate, object, hits\n" + 
			"from %s.triplestore\n" + 
			"where hits>0\n" + 
			"order by hits;";

	private final String updateWithCredit = "update %s.triplestore\n" + 
			"set credit = credit + ln(hits + 1)\n" + 
			"where hits > 0";

	/** The constructor initializes the connections to the RDF triplestore and relational database.
	 * 
	 * @param tripleStorePath path of the triple store. Use MyPaths.index_path as value
	 * @param jdbcString string to connect to the relational DB. Use RDB.produceJdbcString() to get this value
	 * 
	 * @exception SQLException in case unable to open connection to relational database
	 * */
	public CreditDistributor(String tripleStorePath, String jdbcString) throws SQLException {
		// init and open connection to triplestore
		TripleStoreHandler.openRepositoryAndConnection(tripleStorePath);

		// open connection to relational DB - also set autocommit to false
		ConnectionHandler.createConnectionAsOwner(jdbcString, BSBMCreditDistributor.class.getName()).setAutoCommit(false);;
	}

	/** Execute queries of class 1 many times
	 * 
	 * @param class_ class of query that we are executing
	 * @param values_path file containing values that can be used to create meaningful queries in csv format
	 * @param number_of_executed_queries number of queries that we want to execute from this class randomly
	 * */
	public void executeClass(MyValues.QueryClass class_, String values_path, int number_of_executed_queries) {

		// open the support files
		Path valuesPath = Paths.get(values_path);
		BufferedReader valuesReader = null;

		try {
			// open the readers
			valuesReader = Files.newBufferedReader(valuesPath);

			String valuesLine;
			int counter = 0;

			// need to delete first line
			valuesReader.readLine();
			ArrayList<String[]> valuesList = new ArrayList<String[]>(); // list where we put the values to build the query

			// read the values for this query and the number of times we simulate its execution
			while((valuesLine = valuesReader.readLine()) != null) {

				String[] values = valuesLine.split(",");
				String param1 = values[0];
				String param2 = values[1];
				String param3 = values[2];
				String[] parameters = {param1, param2, param3};
				//add the parameters to the list
				valuesList.add(parameters);

				//				// execute the query "many times"
				//				try {
				//					this.performQuery1withParameters(param1, param2, param3, param4);
				//				} catch (SQLException e) {
				//					System.err.println("error at query 1");
				//					e.printStackTrace();
				//				}
				//				
				//				counter++;
				//				if (counter % 100 == 0)
				//					System.out.println("Executed " + counter + " SQL queries");
			}

			// now we execute the queries many times, randomly
			// generates a random distribution with a certain median and variance. 
			// the median is in the middle of the list of queries
			// the majority of a population resides betwen +-3 standard deviations from the median
			NormalDistribution distribution = new NormalDistribution(valuesList.size()/2, valuesList.size()/6);

			for(int i = 0; i < number_of_executed_queries; ++i) {

				int randomNum =  (int) Math.floor(distribution.sample());

				// need to be sure to have a number that can be used
				while(randomNum < 0 || randomNum > valuesList.size() - 1)
					randomNum = (int) Math.floor(distribution.sample());


				String param1 = valuesList.get(randomNum)[0];
				String param2 = valuesList.get(randomNum)[1];
				String param3 = valuesList.get(randomNum)[2];
				String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries

				try {
					this.performQuery1withParameters(param1, param2, param3, param4);
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
		} catch (IOException e) {
			e.printStackTrace();

		} finally {

		}

	}

	/** performs query 1 on the triple store of the instance. We use the CONSTRUCT version, so to 
	 * discover the triples that are used by the query, and not the displayed values. 
	 * The construct query enables us to know where to put the credit in the relational database.
	 * It also  distributes the credit on the relational database.
	 * */
	public void performQuery1withParameters(String param1, String param2, String param3, String param4) throws SQLException {
		String query = BSBMQuery1.parameter_query_1;

		//prepare the query inserting the desired values
		query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">",
				"<" + param1 + ">", "<" + param2+ ">", "<" + param3 + ">", param4);

		// prepare a query with the connection to the triplestore 
		GraphQuery graphQuery = TripleStoreHandler.getRepositoryConnection().prepareGraphQuery(query);

		//evaluate the SPARQL query (only once to get the used triples)
		try (GraphQueryResult result = graphQuery.evaluate()) {

			String q = String.format(this.updateHits, RDB.schema);// set the right schema to the SQL query
			PreparedStatement stmt = ConnectionHandler.getConnection().prepareStatement(q);

			// now for each triple
			for (Statement st: result) {
				// get the three elements of the triple 
				String subject = st.getSubject().stringValue();
				String predicate = st.getPredicate().stringValue();
				String object = st.getObject().stringValue();

				// prepare the query to update the credit to the database
				stmt.setInt(1, 1); // the number of "hits" to this triple is incremented by 1
				stmt.setString(2, subject);
				stmt.setString(3, predicate);
				stmt.setString(4, object);

				stmt.addBatch();
			}
			stmt.executeBatch();
			ConnectionHandler.getConnection().commit();
		}
	}

	/** After the distribution of hits, use them to update the credit in each tuple by using */
	public void assignCreditBasedOnNumberOFHits() {
		Connection cc = ConnectionHandler.getConnection();

		// first, select the tuples with hits > 0
		try {
			String updateString = String.format(this.updateWithCredit, RDB.schema);
			PreparedStatement ps = cc.prepareStatement(updateString);
			ps.executeUpdate();
			cc.commit();
		} catch (SQLException e) {
			e.printStackTrace();
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
			CreditDistributor distributor = new CreditDistributor(MyPaths.index_path, RDB.produceJdbcString());

			// distribute hits using random queries
			distributor.executeClass(MyValues.QUERYCLASS, MyPaths.values_path, MyValues.queryNumberCredit);
			distributor.assignCreditBasedOnNumberOFHits();

			distributor.shutDown();

			System.out.println("done");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
