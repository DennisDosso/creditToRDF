package it.unipd.dei.ims.queries;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.google.common.base.Stopwatch;

import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQueries;
import it.unipd.dei.ims.data.MyPaths;


/** 
 * 
 * 
 * Step 5 - final performance of queries, also take the times
 * 
 * */
public class PerformSameQueryOnTwoRepositoriesAndTakeTimes {

	public PerformSameQueryOnTwoRepositoriesAndTakeTimes() {

	}


	public static void performClass1AndTakeAvgTimes(String repositorypath, String queryValuesPath) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query = BSBMQueries.select_query_1;

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			long start = System.currentTimeMillis();
			int counter = 0;
			int hits = 0;

			// need to consume the first line, containing the attribute names
			reader.readLine();

			while((line = reader.readLine()) != null) {
				// get the values
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query
				String param1 = values[0];
				String param2 = values[1];
				String param3 = values[2];
				String param4 = BSBMQueries.value_query_1; // this value is fixed for all queries

				// format the query
				query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">", param4);

				// take the time required for this query
				Stopwatch timer = Stopwatch.createStarted();

				TupleQuery tupleQuery = repo.prepareTupleQuery(query);
				TupleQueryResult result = tupleQuery.evaluate();
				if(result.hasNext()) 
				{
					hits++;
				}
				//we have a hit

				//					System.out.println(timer.stop());

				counter++;
				result.close();
				if (counter % 1000 == 0) {
//					System.out.println("processed " + counter + " queries");
				}
			}
			
			long elapsed = System.currentTimeMillis() - start;
			
			//convert to seconds
//			long elapsedSeconds = elapsed / 1000;
			
			double average = (double) elapsed / counter;
			
			System.out.println(" total time in ms: " + elapsed + 
					" ms, \n average time: " + average + " ms\n "
							+ "number of hits: " + hits + 
							"\n average percentage of hits: " + hits/counter);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// close connection and shut down repository
			TripleStoreHandler.closeRepositoryAndConnextion();
		}
	}

	/** Test main, to use this class as a script */
	public static void main(String[] args) {
		// get the file of values
		MyPaths paths = new MyPaths(); // read the paths from the properties file
		
		// path of the file used to build queries
		String queryValuesPath = MyPaths.values_path;
		
		// path of the reduced index
		String tripleStorePath = MyPaths.querying_index;

		PerformSameQueryOnTwoRepositoriesAndTakeTimes.performClass1AndTakeAvgTimes(tripleStorePath, queryValuesPath);		
	}

}
