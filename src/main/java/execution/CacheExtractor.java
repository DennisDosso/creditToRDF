package execution;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.NumberUtils;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;

public class CacheExtractor {

	private Connection connection;
	/** in-memory graph */
	public Model graph;
	/** in-memory repository connection to the RDF graph*/
	public RepositoryConnection rConnection; 


	/** Query to select triples in the relational database that have credit bigger than 0 
	 * */
	private String SELECT_CREDITED_TRIPLES = "select t.subject , t.predicate , t.\"object\" \n" + 
			"from %s.triplestore t \n" + 
			"where credit > ?";


	/** Prepares the connection to the relational databases and the connection to the reduced triple-store. 
	 * 
	 * Used property files:
	 * <p>
	 * <ul>
	 * <li> main.properties - database connection </li>
	 * <li> opath.properties - reduced.index.path for the path of the directory where to save the triple store</li>
	 * </ul>
	 * */
	public CacheExtractor() {
		try {
			// builds connection to relational database
			RDB.setup();
			MyPaths.setup();
			
			String jdbcString = RDB.produceJdbcString();
			this.connection = ConnectionHandler.createConnection(jdbcString);
			
			// open the in-disk repository
			this.rConnection = TripleStoreHandler.openRepositoryAndConnection(MyPaths.index_path);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch(RepositoryException e2) {
			e2.printStackTrace();
		}
	}

	/**Given the connection to the relational database of the object invoking this method,
	 * extracts triples from the RDB database with credit above a certain threshold, and uses them to 
	 * build the in-RAM cache
	 * 
	 * @param threshold minimun quantity of credit a triple needs to have (it is a > relationship, value excluded).
	 * If you put 0 as value, you get all the triples that have at least something as value.  
	 * */
	public void buildCacheUsingThreshold(double threshold) {
		long n = System.currentTimeMillis();
		try {
			// asks to the relational db all the triples with a certain quantity of credit
			String qu = String.format(this.SELECT_CREDITED_TRIPLES, RDB.schema);
			PreparedStatement stmt = this.connection.prepareStatement(qu);
			stmt.setDouble(1, threshold);
			ResultSet r = stmt.executeQuery();

			// prepare an rdf model in-memory
			ModelBuilder builder = new ModelBuilder().setNamespace("q1", "http://bsbm.query1.it/");
			ValueFactory vf = SimpleValueFactory.getInstance();

			while (r.next()) {
				// take all the triples
				String sub = r.getString(1);
				String pred = r.getString(2);
				String obj = r.getString(3);

				UrlValidator urlValidator = new UrlValidator();
				if(urlValidator.isValid(obj)) { // in case the object is a resource
					IRI o = iri(obj);					
					builder.subject(sub).add(pred, o);
				} else { // in case it is a literal
					if(NumberUtils.isInteger(obj)) {
						//in this case, it is an integer
						builder.subject(sub).add(pred, Integer.parseInt(obj));
					} else if(NumberUtils.isBSBMDate(obj)) {
						// in this case, it is a data
						builder.subject(sub).add(pred, vf.createLiteral(obj, XSD.DATETIME));
					} else {
						// in case it is a standard string, add it with an english tag
						builder.subject(sub).add(pred, vf.createLiteral(obj, "en"));	
					} 
				}


			}
			// create a graph
			this.graph = builder.build();
			// open the in-memory repository
			Repository repo = new SailRepository(new MemoryStore());
			this.rConnection = repo.getConnection();
			// add the graph to the in-memory repository
			this.rConnection.add(graph);

			long elapsed = System.currentTimeMillis() - n;
			System.out.println("built the cache in " + elapsed + "ms");
			System.out.println("size of the graph (in triples): " + this.graph.size());


			r.close();

		} catch (SQLException e) {
			System.out.println("Error producing the select query");
			e.printStackTrace();
		}

	}

	public void answerToAFewQueriesOnTheCache(MyValues.QueryClass class_, String values_path, int number_of_executed_queries) {
		// open the support files
		Path valuesPath = Paths.get(values_path);
		BufferedReader valuesReader = null;
		
		

		try {
			// open the readers
			valuesReader = Files.newBufferedReader(valuesPath);
			String valuesLine;

			// need to delete first line
			valuesReader.readLine();

			ArrayList<String[]> valuesList = new ArrayList<String[]>();

			// read the values for this query and the number of times we simulate its execution
			while((valuesLine = valuesReader.readLine()) != null) {
				String[] values = valuesLine.split(",");
				String param1 = values[0];
				String param2 = values[1];
				String param3 = values[2];

				String[] parameters = {param1, param2, param3};
				//add the parameters to the list
				valuesList.add(parameters);
			}

			NormalDistribution distribution = new NormalDistribution(valuesList.size()/2, valuesList.size()/6);
			
			long totalTime = 0, totalTimeOnDisk = 0;
			long nanoTotalTime = 0, nanoTotalTimeOnDisk = 0;
			
			int hits = 0, hitsOnDisk = 0;
			for(int i = 0; i < number_of_executed_queries; ++i) {
				int randomNum =  (int) Math.floor(distribution.sample());

				// need to be sure to have a number that can be used
				while(randomNum < 0 || randomNum > valuesList.size() - 1)
					randomNum = (int) Math.floor(distribution.sample());


				String param1 = valuesList.get(randomNum)[0];
				String param2 = valuesList.get(randomNum)[1];
				String param3 = valuesList.get(randomNum)[2];
				String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries
				
				 ReturnBox box = this.performQueryWithParameters(param1, param2, param3, param4);
				// update the number of hit in the cache
				if(box.foundSomething) {
					hits += 1;
					// update the average time on these queries
					totalTime += box.time;	
					nanoTotalTime += box.nanoTime;
				} else {
					// need to perform the query on the in-disk database
					box = this.performQueryWithParametersOnDisk(param1, param2, param3, param4);
					totalTimeOnDisk += box.time;
					nanoTotalTimeOnDisk += box.nanoTime;
					hitsOnDisk += 1;
				}
			}
			
			long average = 0, nanoAverage = 0; 
			if(hits != 0) {
				average = (long) totalTime / hits;
				nanoAverage = (long) nanoTotalTime/hits;
			}
			
			long totalAverageTimeOnDisk = 0, nanoTotalAverageTimeOnDisk  = 0; 
			if(hitsOnDisk != 0) {
				totalAverageTimeOnDisk = (long) totalTimeOnDisk / hitsOnDisk;
				nanoTotalAverageTimeOnDisk = (long) nanoTotalTimeOnDisk / hitsOnDisk;				
			}
			
			long totalAverageTime = (long) (totalTime + totalTimeOnDisk) / number_of_executed_queries;
			long nanoTotalAverageTime = (long) (nanoTotalTime + nanoTotalTimeOnDisk) / number_of_executed_queries;
			
			// print everything
			System.out.println("total number of hits in the cache: " + hits);
			System.out.println("total number of hits on disk: " + hitsOnDisk);
			System.out.println("total time spent answering the queries in cache : " + totalTime + " ms, " + nanoTotalTime + " ns");
			System.out.println("total time spent answering the queries on disk : " + totalTimeOnDisk + " ms, " + nanoTotalTimeOnDisk + " ns");
			System.out.println("average time in cache: " + average + " ms, " + nanoAverage + " ns");
			System.out.println("average time on disk: " + totalAverageTimeOnDisk + " ms, " + nanoTotalAverageTimeOnDisk + " ns");
			System.out.println("average time overall: " + totalAverageTime + " ms, " + nanoTotalAverageTime + " ns");
			

		} catch (IOException e) {
			e.printStackTrace();
		} finally {}
	}

	public ReturnBox performQueryWithParameters(String ... param) {
		String param1 = param[0];
		String param2 = param[1];
		String param3 = param[2];
		String param4 = param[3];

		// get the construct query 
		String query = BSBMQuery1.select_query_1;

		//prepare the query inserting the desired values
		query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">", param4);

		long start = System.currentTimeMillis();
		long nanoStart = System.nanoTime();
		ReturnBox box = new ReturnBox();

		//prepare the query
		TupleQuery q = this.rConnection.prepareTupleQuery(query);
		// execute the query
		try(TupleQueryResult result = q.evaluate()) {
			if(result.hasNext())
				box.foundSomething = true;
		}

		long elapsed = System.currentTimeMillis() - start;
		long nanoElapsed = System.nanoTime() - nanoStart;
		box.time = elapsed;
		box.nanoTime = nanoElapsed;
		return box;

	}
	
	public ReturnBox performQueryWithParametersOnDisk(String ... param) {
		String param1 = param[0];
		String param2 = param[1];
		String param3 = param[2];
		String param4 = param[3];
		

		// get the query 
		String query = BSBMQuery1.select_query_1;

		//prepare the query inserting the desired values
		query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">", param4);

		long start = System.currentTimeMillis();
		long nanoStart = System.nanoTime();
		ReturnBox box = new ReturnBox();

		//prepare the query
		TupleQuery q = this.rConnection.prepareTupleQuery(query);
		// execute the query
		try(TupleQueryResult result = q.evaluate()) {
			if(result.hasNext())
				box.foundSomething = true;
		}

		long elapsed = System.currentTimeMillis() - start;
		long nanoElapsed = System.nanoTime() - nanoStart;
		box.time = elapsed;
		box.nanoTime = nanoElapsed;
		return box;
	}

	public void close() {
		try {
			ConnectionHandler.closeConnection();
			this.rConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		RDB.setup();// read parameters for connection to RDB
		MyPaths.setup(); // read parameters for paths
		MyValues.setup();
		CacheExtractor execution = new CacheExtractor();
		
		//first create the cache
		execution.buildCacheUsingThreshold(MyValues.creditThreshold);
		
		//then answer queries
		execution.answerToAFewQueriesOnTheCache(MyValues.QUERYCLASS, MyPaths.values_path, MyValues.queryNumberHit);

		execution.close();
		System.out.println("done");
	}

}
