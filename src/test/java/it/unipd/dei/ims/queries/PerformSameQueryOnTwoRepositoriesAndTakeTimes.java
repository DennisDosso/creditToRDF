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
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.BSBMQuery8;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;


/** 
 * 
 * 
 * Step 5 - final performance of queries, also take the times
 * 
 * */
public class PerformSameQueryOnTwoRepositoriesAndTakeTimes {



	public static void performQueriesToTakeTimeForClass(String repositorypath, String queryValuesPath, boolean print) {
		if(MyValues.QUERYCLASS == MyValues.QueryClass.ONE) {
			performClass1AndTakeAvgTimes(repositorypath, queryValuesPath, print);
		} else if (MyValues.QUERYCLASS == MyValues.QueryClass.EIGHT) {
			performClass8AndTakeAvgTimes(repositorypath, queryValuesPath, print);
		}
	}
	
	
	public static void performClass1AndTakeAvgTimes(String repositorypath, String queryValuesPath, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query = BSBMQuery1.select_query_1;

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			long start = System.currentTimeMillis();// starting time
			int counter = 0;// to count how many query we executed
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
				String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries

				// format the query
				query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">", param4);

				TupleQuery tupleQuery = repo.prepareTupleQuery(query);
				TupleQueryResult result = tupleQuery.evaluate();
				if(result.hasNext()) 
				{
					hits++;
				}
				//we have a hit


				counter++;
				result.close();
			}
			
			long elapsed = System.currentTimeMillis() - start;
			
			//convert to seconds if you want
//			long elapsedSeconds = elapsed / 1000;
			
			double average = (double) elapsed / counter;
			
			if(print)
				System.out.println("    total time in ms: " + elapsed + 
						" ms, \n     average time: " + average + " ms\n"
						+ "    number of hits: " + hits + 
						"\n    average percentage of hits: " + hits/counter);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// close connection and shut down repository
			TripleStoreHandler.closeRepositoryAndConnextion();
		}
	}
	
	public static void performClass8AndTakeAvgTimes(String repositorypath, String queryValuesPath, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query = BSBMQuery8.select_query;

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			long start = System.currentTimeMillis();// starting time
			int counter = 0;// to count how many query we executed
			int hits = 0;

			// need to consume the first line, containing the attribute names
			reader.readLine();
			

			while((line = reader.readLine()) != null) {
				// get the values
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query
				String param1 = values[0];

				// format the query
				query = String.format(query, "<" + param1 + ">");

				TupleQuery tupleQuery = repo.prepareTupleQuery(query);
				try(TupleQueryResult result = tupleQuery.evaluate()) {
					if(result.hasNext()) 
					{
						//we have a hit
						hits++;
					}
					counter++;					
				}
			}
			
			long elapsed = System.currentTimeMillis() - start;
			
			//convert to seconds if you want
//			long elapsedSeconds = elapsed / 1000;
			
			double average = (double) elapsed / counter;
			
			if(print)
				System.out.println("    total time in ms: " + elapsed + 
						" ms, \n     average time: " + average + " ms\n"
						+ "    number of hits: " + hits + 
						"\n    average percentage of hits: " + hits/counter);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// close connection and shut down repository
			TripleStoreHandler.closeRepositoryAndConnextion();
		}
	}

	
	public static void performClass1AndTakeAvgTimesUsingNamedGraphs(String repositorypath, String queryValuesPath) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query = BSBMQuery1.select_query_1_with_named_graphs;

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
				String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries

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
		MyPaths.setup();; // read the paths from the properties file
		MyValues.setup();
		
		// path of the file used to build queries
		String queryValuesPath = MyPaths.values_path;
		
		// path of the index
		String tripleStorePath = MyPaths.querying_index;
		
		if(MyValues.areWeDoingTheWarmUp) {
			//do one round because we are sillybilly and we let the system crunch some queries and optimize them
			System.out.println("first round of queries on the full database");
			PerformSameQueryOnTwoRepositoriesAndTakeTimes.performQueriesToTakeTimeForClass(tripleStorePath, queryValuesPath, false);			
		}
		
		if(MyValues.areWeInterrogatingTheWholeTripleStore) {
			System.out.println("\n\nWhole database:");
			PerformSameQueryOnTwoRepositoriesAndTakeTimes.performQueriesToTakeTimeForClass(tripleStorePath, queryValuesPath, true);
		}
		
		if(MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
			System.out.println("\nNamed Database");
			PerformSameQueryOnTwoRepositoriesAndTakeTimes.performQueriesToTakeTimeForClass(tripleStorePath, queryValuesPath, true);			
		}
		
		if(MyValues.areWeInterrogatingTheReducedTripleStore) {
//			System.out.println("\n\n'reduced' database, first time for warmup");
			PerformSameQueryOnTwoRepositoriesAndTakeTimes.performQueriesToTakeTimeForClass(MyPaths.reduced_index_path, queryValuesPath, false);
			
			System.out.println("\n\n'reduced' database, second time");
			PerformSameQueryOnTwoRepositoriesAndTakeTimes.performQueriesToTakeTimeForClass(MyPaths.reduced_index_path, queryValuesPath, true);
		}
		
	}

}
