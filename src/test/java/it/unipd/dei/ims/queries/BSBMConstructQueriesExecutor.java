package it.unipd.dei.ims.queries;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.RDB;

/** Contain methods to execute the different CONSTRUCT queries
 * connected to BSBM. We have a different method for each class we consider.
 * 
 * */
public class BSBMConstructQueriesExecutor {

	private Repository repository;

	private Connection connection;

	/** query to update the value in the triple store*/
	private final String updateTripleStrore = "UPDATE public.triplestore\n" + 
			"SET credit= credit + 1 \n" + 
			"WHERE subject=? AND predicate=? AND \"object\"=?;\n" + 
			"";
	
	private final String updateTripleStroreWithTimes = "UPDATE public.triplestore\n" + 
			"SET credit= credit + ? \n" + 
			"WHERE subject=? AND predicate=? AND \"object\"=?;\n" + 
			"";

	public BSBMConstructQueriesExecutor(String tripleStorePath) throws SQLException {
		// open connection to triplestore
		TripleStoreHandler.initRepository(tripleStorePath);
		this.repository = TripleStoreHandler.getRepository();

		//open connection to relational DB
		String jdbcString = RDB.produceJdbcString();
		this.connection = ConnectionHandler.createConnectionAsOwner(jdbcString, BSBMConstructQueriesExecutor.class.getName());
	}

	/** Executes the first class of BSBM queries 
	 * @throws SQLException 
	 * 
	 * 
	 * */
	public void executeQuery1() throws SQLException {
		// get the query
		String query = BSBMQuery1.parameter_query_1;

		//prepare the query inserting the desired values
		query = String.format(query, "bsbm-inst:ProductType3", "bsbm-inst:ProductFeature44", "bsbm-inst:ProductFeature54",
				"bsbm-inst:ProductType3", "bsbm-inst:ProductFeature44", "bsbm-inst:ProductFeature54", "300");

		// open a connection to the database
		try (RepositoryConnection conn = this.repository.getConnection()) {
			//execute the query
			GraphQuery graphQuery = conn.prepareGraphQuery(query);
			try (GraphQueryResult result = graphQuery.evaluate()) {
				// we just iterate over all solutions in the result...
				for (Statement st: result) {
					// get the three elements 
					String subject = st.getSubject().stringValue();
					String predicate = st.getPredicate().stringValue();
					String object = st.getObject().stringValue();

					// now that we have these three values, we can add +1 to their credit
					PreparedStatement stmt = this.connection.prepareStatement(this.updateTripleStrore);
					stmt.setString(1, subject);
					stmt.setString(2, predicate);
					stmt.setString(3, object);

					int res = stmt.executeUpdate();
					System.out.println(res);
				}
			}
		}
	}


	/**
	 * 
	 * @param times the number of times to simulate the execution of this query. Concretely, 
	 * the method adds +times to the credit of each triple used.
	 * */
	public void performQuery1withParameters(String param1, String param2, String param3, String param4, int times) throws SQLException {
		// get the query
		String query = BSBMQuery1.parameter_query_1;

		//prepare the query inserting the desired values
		query = String.format(query, param1, param2, param3,
				param1, param2, param3, param4);

		// open a connection to the database
		try (RepositoryConnection conn = this.repository.getConnection()) {
			//execute the query
			GraphQuery graphQuery = conn.prepareGraphQuery(query);
			try (GraphQueryResult result = graphQuery.evaluate()) {
				// we just iterate over all solutions in the result...
				for (Statement st: result) {
					// get the three elements 
					String subject = st.getSubject().stringValue();
					String predicate = st.getPredicate().stringValue();
					String object = st.getObject().stringValue();

					// now that we have these three values, we can add +1 to their credit
					PreparedStatement stmt = this.connection.prepareStatement(this.updateTripleStroreWithTimes);
					stmt.setInt(1, times); // times this query is executed
					stmt.setString(2, subject);
					stmt.setString(3, predicate);
					stmt.setString(4, object);

					int res = stmt.executeUpdate();
					System.out.println(res);
				}
			}
		}
	}

	/** Performs the class 1 of queries many times. 
	 * It uses the values_path file to get the values to format the SPARQL query, and 
	 * the times_path file to decide how many times to "execute" the query. 
	 * <p>
	 * We assume that the values_path and times_path have the same length, i.e. the
	 * same number of lineas. In each line in the values_path we have the values to add to the query
	 * In each line of the times_path file we have the number of times that query is executed
	 * */
	public void performQuery1ManyTimes(String values_path, String times_path) {
		// SPARQL construct query
		String query = BSBMQuery1.parameter_query_1;

		// open the support files
		Path valuesPath = Paths.get(values_path);
		Path timesPath = Paths.get(times_path);
		BufferedReader valuesReader = null, timesReader = null;

		try {
			valuesReader = Files.newBufferedReader(valuesPath);
			timesReader = Files.newBufferedReader(timesPath);

			String valuesLine, timesLine = null;

			// read the values for this query and the number of times we simulate its execution
			while((valuesLine = valuesReader.readLine()) != null) {
				timesLine = timesReader.readLine();
				int times = Integer.parseInt(timesLine);

				String[] values = valuesLine.split(",");
				String param1 = values[0];
				String param2 = values[1];
				String param3 = values[2];
				String param4 = BSBMQuery1.parameter_query_1; // this value is fixed for all queries

				// execute the query many times
				try {
					this.performQuery1withParameters(param1, param2, param3, param4, times);
				} catch (SQLException e) {
					System.err.println("error at query 1");
					e.printStackTrace();
				}
			}

			valuesReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} finally {

		}
	}




	/** Test main 
	 * @throws SQLException */
	public static void main(String[] args) throws SQLException {
		BSBMConstructQueriesExecutor execution = new BSBMConstructQueriesExecutor(MyPaths.index_path);
		execution.executeQuery1();
	}
}
