package execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;

import it.unipd.dei.ims.credit.distribution.CreditDistributor;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.BSBMQuery10;
import it.unipd.dei.ims.data.BSBMQuery5;
import it.unipd.dei.ims.data.BSBMQuery7;
import it.unipd.dei.ims.data.BSBMQuery8;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;

/**
 * 
 * Step 3 - this class is the last implementation of credit distribution. It only uses tuples that are necessary, building them on the fly and adding them in the DB
 * Thus, we do not need to keep a whole table for the DB with billions of triples.
 * 
 * 
 * *
 * @author dennisdosso
 *
 */
public class SmartCreditDistributor extends CreditDistributor {
	
	private  String CHECK_TRIPLE_PRESENCE = "SELECT subject FROM %s.triplestore where subject = ? AND predicate = ? AND object = ?";
	
	private final String updateHits = "UPDATE %s.triplestore\n" + 
			"SET hits = hits + ? \n" + 
			"WHERE subject=? AND predicate=? AND \"object\"=?;\n" + 
			"";
	
	private final String INSERT_TRIPLE = "INSERT INTO %s.triplestore\n" + 
			"(subject, predicate, \"object\", credit, hits)\n" + 
			"VALUES(?, ?, ?, 0, 1);";


	/** The constructor initializes the connections to the RDF triplestore and relational database.
	 * 
	 * @param tripleStorePath path of the triple store. Use MyPaths.index_path as value
	 * @param jdbcString string to connect to the relational DB. Use RDB.produceJdbcString() to get this value
	 * 
	 * @exception SQLException in case unable to open connection to relational database
	 * */
	public SmartCreditDistributor(String tripleStorePath, String jdbcString) throws SQLException {
		super(tripleStorePath, jdbcString);
	}


	/** Used to randomly generate a certain amount of queries and distribute the number of hits with them
	 * 
	 * @param number_of_executed_queries How many qieries you want to execute
	 * 
	 * @throws SQLException 
	 * */
	public void updateHitsInTheRDBFofClass(MyValues.QueryClass class_, String values_path, int number_of_executed_queries) throws SQLException {
		// open the support files
		Path valuesPath = Paths.get(values_path);
		BufferedReader valuesReader = null;

		ArrayList<String[]> valuesList = new ArrayList<String[]>(); // list where we put the values to build the query
		try {
			// open the readers
			valuesReader = Files.newBufferedReader(valuesPath);

			String valuesLine;
			int counter = 0;

			// need to delete first line
			valuesReader.readLine();

			// read the values for this query and the number of times we simulate its execution
			while((valuesLine = valuesReader.readLine()) != null) {
				String[] values = valuesLine.split(",");
				
				if(class_ == MyValues.QueryClass.ONE) {
					String param1 = values[0];
					String param2 = values[1];
					String param3 = values[2];
					String[] parameters = {param1, param2, param3};
					//add the parameters to the list
					valuesList.add(parameters);
				} else if(class_ == MyValues.QueryClass.FIVE) {
					String param1 = values[0];
					String[] parameters = {param1};
					valuesList.add(parameters);
				} else if(class_ == MyValues.QueryClass.SEVEN) {
					String param1 = values[0];
					String param2 = values[1];
					String[] parameters = {param1, param2};
					valuesList.add(parameters);
				} else if(class_ == MyValues.QueryClass.EIGHT) {
					String param1 = values[0];
					String[] parameters = {param1};
					valuesList.add(parameters);
				} else if(class_ == MyValues.QueryClass.TEN) {
					String param1 = values[0];
					String[] parameters = {param1};
					valuesList.add(parameters);
				}
			} // we have read all the parameters
			
			valuesReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {}
		
		
		NormalDistribution distribution = new NormalDistribution(valuesList.size()/2, valuesList.size()/6);
		
		for(int i = 0; i < number_of_executed_queries; ++i) {
			int randomNum =  (int) Math.floor(distribution.sample());

			// need to be sure to have a number that can be used
			while(randomNum < 0 || randomNum > valuesList.size() - 1)
				randomNum = (int) Math.floor(distribution.sample());
			
			if(class_ == MyValues.QueryClass.ONE) {
				String param1 = valuesList.get(randomNum)[0];
				String param2 = valuesList.get(randomNum)[1];
				String param3 = valuesList.get(randomNum)[2];
				String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries
				
				this.updateHitsForOnlyOneQueryOfClassAndParameters(class_, param1, param2, param3, param4);
			} else if(class_ == MyValues.QueryClass.FIVE) {
				// TODO
			} else if(class_ == MyValues.QueryClass.SEVEN) {
				
			} else if(class_ == MyValues.QueryClass.EIGHT) {
				
			} else if(class_ == MyValues.QueryClass.TEN) {
				
			}
			
		}
	}
	/**
	 * Given a query class and its parameters, it performs the construct query and updates/inserts the tuples in the relational databases.
	 * The new inserted tuples present the field hits with value 1. The triples already present are updated with hits += 1
	 * 
	 * */
	public void updateHitsForOnlyOneQueryOfClassAndParameters(MyValues.QueryClass class_, String ... params) throws SQLException {
		String query = null;
		// depending on the class, prepare the corresponding construct query to perform
		if(class_ == MyValues.QueryClass.ONE) {
			String param1 = params[0];
			String param2 = params[1];
			String param3 = params[2];
			String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries
	
			query = BSBMQuery1.select;
			query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">",
					"<" + param1 + ">", "<" + param2+ ">", "<" + param3 + ">", param4);
		} else if (class_ == MyValues.QueryClass.FIVE) {
			String param1 = params[0];
			query = BSBMQuery5.select;
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">");
		} else if (class_ == MyValues.QueryClass.SEVEN) {
			String param1 = params[0];
			String param2 = params[1];
			query = BSBMQuery7.select;
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param2 + ">",
					"<" + param1 + ">");
		} else if (class_ == MyValues.QueryClass.EIGHT) {
			String param1 = params[0];
			query = BSBMQuery8.select_query;
			query = String.format(query, "<" + param1 + ">");
		} else if (class_ == MyValues.QueryClass.TEN) {
			String param1 = params[0];
			query = BSBMQuery10.select;
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">");
		}
		
		// prepare a query with the connection to the triplestore 
		GraphQuery graphQuery = TripleStoreHandler.getRepositoryConnection().prepareGraphQuery(query);
		
		// statement used later to update a tuple already present in the DB
		PreparedStatement update_stmt = null;
		String q = String.format(this.updateHits, RDB.schema);// set the right schema to the SQL query
		update_stmt = ConnectionHandler.getConnection().prepareStatement(q);
		
		String insert_q = String.format(this.INSERT_TRIPLE, RDB.schema);
		PreparedStatement insert_stmt = ConnectionHandler.getConnection().prepareStatement(insert_q);
		
		
		
		try (GraphQueryResult result = graphQuery.evaluate()) {
			// for each triple in the result of the construct...
			for (Statement st: result) {
				// check if that triple is present in the RDB via a query to the RDB itself
				String subject = st.getSubject().stringValue();
				subject = subject.substring(0, Math.min(subject.length(), 254));
				String predicate = st.getPredicate().stringValue();
				predicate = predicate.substring(0, Math.min(predicate.length(), 254));
				String object = st.getObject().stringValue();
				
				
				String check_query = String.format(this.CHECK_TRIPLE_PRESENCE, RDB.schema);
				PreparedStatement check_stmt = ConnectionHandler.getConnection().prepareStatement(check_query);
				
				check_stmt.setString(1, subject);
				check_stmt.setString(2, predicate);
				check_stmt.setString(3, object);
				
				//do the thang
				ResultSet check_rs = check_stmt.executeQuery();
				
				if(check_rs.next()) {
					// the triple is already present in the DB
					update_stmt.setInt(1, 1);
					update_stmt.setString(2, subject);
					update_stmt.setString(3, predicate);
					update_stmt.setString(4, object);
					
					update_stmt.addBatch();
				} else {
					// need to insert the triple
					insert_stmt.setString(1, subject);
					insert_stmt.setString(2, predicate);
					insert_stmt.setString(3, object);
					
					insert_stmt.executeUpdate();
					ConnectionHandler.getConnection().commit();
				}
			}// covered all statements in the graph
			
			update_stmt.executeBatch();
			ConnectionHandler.getConnection().commit();
		}
	}




	/** test main */
	public static void main(String[] args) {
		try {
			RDB.setup();// read parameters for connection to RDB
			MyPaths.setup(); // read parameters for paths
			MyValues.setup();
			SmartCreditDistributor distributor = new SmartCreditDistributor(MyPaths.index_path, RDB.produceJdbcString());

			distributor.updateHitsInTheRDBFofClass(MyValues.QUERYCLASS, MyPaths.values_path, MyValues.queryNumberCredit);
			distributor.assignCreditBasedOnNumberOFHits();

			distributor.shutDown();

			System.out.println("done");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
