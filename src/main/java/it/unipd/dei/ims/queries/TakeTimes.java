package it.unipd.dei.ims.queries;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.BSBMQuery10;
import it.unipd.dei.ims.data.BSBMQuery5;
import it.unipd.dei.ims.data.BSBMQuery7;
import it.unipd.dei.ims.data.BSBMQuery8;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;


/** 
 * 
 * 
 * Step 5 - final performance of queries, also take the times
 * 
 * */
public class TakeTimes {

	public TakeTimes() {

	}

	/**
	 * 
	 * @param repositoryPath  directory where the triple store is located
	 * @param queryValuesPath path of the file containing the values used to build the queries of one class
	 * @param using_named set to true if we are using named graphs
	 * @param print set it to true if you want to print the results in a txt file
	 * @param class_ the query class that we are using. One of the MyValues.QueryClass enum types
	 * 
	 * */
	public void performQueriesToTakeTimeForClassOfQueries(MyValues.QueryClass class_, String repositoryPath, String queryValuesPath, boolean using_named, boolean print) {
		if(class_ == MyValues.QueryClass.ONE) {
			this.performClass1AndTakeAvgTimes(repositoryPath, queryValuesPath, using_named, print);
		}  else if (class_ == MyValues.QueryClass.FIVE) {
			this.performClass5AndTakeAvgTimes(repositoryPath, queryValuesPath, using_named, print);
		}  else if (class_ == MyValues.QueryClass.SEVEN) {
			this.performClass7AndTakeAvgTimes(repositoryPath, queryValuesPath, using_named, print);
		}  else if (class_ == MyValues.QueryClass.EIGHT) {
			this.performClass8AndTakeAvgTimes(repositoryPath, queryValuesPath, using_named, print);
		} else if (class_ == MyValues.QueryClass.TEN) {
			this.performClass10AndTakeAvgTimes(repositoryPath, queryValuesPath, using_named, print);
		}
	}

	/** A method to dominate all others*/
	public void takeAverageTimes(MyValues.QueryClass class_, String repositoryPath, String queryValuesPath, boolean using_named, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositoryPath);
		
		ArrayList<String> timesArray = new ArrayList<String>();

		// prepare the SPARQL query
		String query = null;
		if(using_named) {
			if(class_ == MyValues.QueryClass.ONE) {
				query = BSBMQuery1.select_query_1_with_named_graphs;
			}  else if (class_ == MyValues.QueryClass.FIVE) {
				query = BSBMQuery5.select_named;
			}  else if (class_ == MyValues.QueryClass.SEVEN) {
				query = BSBMQuery7.select_named;
			}  else if (class_ == MyValues.QueryClass.EIGHT) {
				query = BSBMQuery8.select_query_with_named_graphs;
			} else if (class_ == MyValues.QueryClass.TEN) {
				query = BSBMQuery10.select_named;
			}
		} else {
			if(class_ == MyValues.QueryClass.ONE) {
				query = BSBMQuery1.select_query_1;
			}  else if (class_ == MyValues.QueryClass.FIVE) {
				query = BSBMQuery5.select;
			}  else if (class_ == MyValues.QueryClass.SEVEN) {
				query = BSBMQuery7.select;
			}  else if (class_ == MyValues.QueryClass.EIGHT) {
				query = BSBMQuery8.select_query;
			} else if (class_ == MyValues.QueryClass.TEN) {
				query = BSBMQuery10.select;
			}
		}

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
//			FileWriter fw = new FileWriter();
			BufferedWriter writer;
			
			String line = "";
			long start = System.currentTimeMillis();// starting time
			int counter = 0;// to count how many query we executed
			int hits = 0;
			double total_query_class_time = 0; // sum of the average time of each query

			// need to consume the first line, containing the attribute names
			reader.readLine();


			while((line = reader.readLine()) != null) { // for each query
				long query_time = 0;

				// get the values that define the query
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query, depending on the class
				if(class_ == MyValues.QueryClass.ONE) {
					String param1 = values[0];
					String param2 = values[1];
					String param3 = values[2];
					String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries
					
					// format the query
					query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">", param4);
				} else if (class_ == MyValues.QueryClass.FIVE) {
					String param1 = values[0];
					query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">");
				}  else if (class_ == MyValues.QueryClass.SEVEN) {
					String param1 = values[0];
					String param2 = values[1];
					query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param2 + ">", "<" + param1 + ">");
				}  else if (class_ == MyValues.QueryClass.EIGHT) {
					String param1 = values[0];
					query = String.format(query, "<" + param1 + ">");
				} else if (class_ == MyValues.QueryClass.TEN) {
					String param1 = values[0];
					query = String.format(query, "<" + param1 + ">", "<" + param1 + ">");
				}

				TupleQuery tupleQuery;
				TupleQueryResult result = null;


				for( int j = 0; j < MyValues.execute_a_query_this_many_times; ++j) {
					//start the timer
					long query_start_time = System.currentTimeMillis();

					tupleQuery = repo.prepareTupleQuery(query);
					result = tupleQuery.evaluate();

					if(j == 0 && result.hasNext())
						hits++;// we have a hit

					result.close();



					//stop the timer
					long current_execution_query_time = System.currentTimeMillis() - query_start_time;

					//add this time to the overall execution time of this query
					query_time += current_execution_query_time;
				}


				counter++; 
				// compute the average of this execution
				double query_average = (double) query_time / MyValues.execute_a_query_this_many_times;

				// add the average to the total execution time
				total_query_class_time += query_average;
			}


			double average_class_time = (double) total_query_class_time /counter; 

			if(print)
				System.out.println("    total time in ms: " + total_query_class_time + 
						" ms, \n     average time for one query: " + average_class_time + " ms\n"
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


	public void performClass1AndTakeAvgTimes(String repositorypath, String queryValuesPath, boolean using_named, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query;
		if(using_named) {
			query = BSBMQuery1.select_query_1_with_named_graphs;
		} else {
			query = BSBMQuery1.select_query_1;
		}

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			long start = System.currentTimeMillis();// starting time
			int counter = 0;// to count how many query we executed
			int hits = 0;
			double total_query_class_time = 0; // sum of the average time of each query

			// need to consume the first line, containing the attribute names
			reader.readLine();


			while((line = reader.readLine()) != null) { // for each query
				long query_time = 0;

				// get the values that define the query
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query
				String param1 = values[0];
				String param2 = values[1];
				String param3 = values[2];
				String param4 = BSBMQuery1.value_query_1; // this value is fixed for all queries

				// format the query
				query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">", param4);

				TupleQuery tupleQuery;
				TupleQueryResult result = null;


				for( int j = 0; j < MyValues.execute_a_query_this_many_times; ++j) {
					//start the timer
					long query_start_time = System.currentTimeMillis();

					tupleQuery = repo.prepareTupleQuery(query);
					result = tupleQuery.evaluate();

					if(j == 0 && result.hasNext())
						hits++;// we have a hit

					result.close();



					//stop the timer
					long current_execution_query_time = System.currentTimeMillis() - query_start_time;

					//add this time to the overall execution time of this query
					query_time += current_execution_query_time;
				}


				counter++; 
				// compute the average of this execution
				double query_average = (double) query_time / MyValues.execute_a_query_this_many_times;

				// add the average to the total execution time
				total_query_class_time += query_average;
			}


			double average_class_time = (double) total_query_class_time /counter; 

			if(print)
				System.out.println("    total time in ms: " + total_query_class_time + 
						" ms, \n     average time for one query: " + average_class_time + " ms\n"
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

	public void performClass5AndTakeAvgTimes(String repositorypath, String queryValuesPath, boolean using_named, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query;
		if(using_named) {
			query = BSBMQuery5.select_named;
		} else {
			query = BSBMQuery5.select;
		}

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			int counter = 0;// to count how many query we executed
			int hits = 0;
			double total_query_class_time = 0; // sum of the average time of each query

			// need to consume the first line, containing the attribute names
			reader.readLine();


			while((line = reader.readLine()) != null) { // for each query
				long query_time = 0;

				// get the values that define the query
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query
				String param1 = values[0];

				// format the query
				query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">");

				TupleQuery tupleQuery;
				TupleQueryResult result = null;


				for( int j = 0; j < MyValues.execute_a_query_this_many_times; ++j) {
					//start the timer
					long query_start_time = System.currentTimeMillis();

					tupleQuery = repo.prepareTupleQuery(query);
					result = tupleQuery.evaluate();

					if(j == 0 && result.hasNext())
						hits++;// we have a hit

					result.close();



					//stop the timer
					long current_execution_query_time = System.currentTimeMillis() - query_start_time;

					//add this time to the overall execution time of this query
					query_time += current_execution_query_time;
				}


				counter++; 
				// compute the average of this execution
				double query_average = (double) query_time / MyValues.execute_a_query_this_many_times;

				// add the average to the total execution time
				total_query_class_time += query_average;
			}


			double average_class_time = (double) total_query_class_time /counter; 

			if(print)
				System.out.println("    total time in ms: " + total_query_class_time + 
						" ms, \n     average time for one query: " + average_class_time + " ms\n"
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

	public void performClass7AndTakeAvgTimes(String repositorypath, String queryValuesPath, boolean using_named, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query;
		if(using_named) {
			query = BSBMQuery7.select_named;
		} else {
			query = BSBMQuery7.select;
		}

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			int counter = 0;// to count how many query we executed
			int hits = 0;
			double total_query_class_time = 0; // sum of the average time of each query

			// need to consume the first line, containing the attribute names
			reader.readLine();


			while((line = reader.readLine()) != null) { // for each query
				long query_time = 0;

				// get the values that define the query
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query
				String param1 = values[0];
				String param2 = values[1];

				// format the query
				query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param2 + ">", "<" + param1 + ">");

				TupleQuery tupleQuery;
				TupleQueryResult result = null;


				for( int j = 0; j < MyValues.execute_a_query_this_many_times; ++j) {
					//start the timer
					long query_start_time = System.currentTimeMillis();

					tupleQuery = repo.prepareTupleQuery(query);
					result = tupleQuery.evaluate();

					if(j == 0 && result.hasNext())
						hits++;// we have a hit

					result.close();



					//stop the timer
					long current_execution_query_time = System.currentTimeMillis() - query_start_time;

					//add this time to the overall execution time of this query
					query_time += current_execution_query_time;
				}


				counter++; 
				// compute the average of this execution
				double query_average = (double) query_time / MyValues.execute_a_query_this_many_times;

				// add the average to the total execution time
				total_query_class_time += query_average;
			}


			double average_class_time = (double) total_query_class_time /counter; 

			if(print)
				System.out.println("    total time in ms: " + total_query_class_time + 
						" ms, \n     average time for one query: " + average_class_time + " ms\n"
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

	public void performClass8AndTakeAvgTimes(String repositorypath, String queryValuesPath, boolean using_named, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query;
		if(using_named) { // if we use the named graphs
			query = BSBMQuery8.select_query_with_named_graphs;
		} else { // otherwise
			query = BSBMQuery8.select_query;
		}

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			long start = System.currentTimeMillis();// starting time
			int counter = 0;// to count how many query we executed
			int hits = 0;
			double total_query_class_time = 0; // sum of the average time of each query

			// need to consume the first line, containing the attribute names
			reader.readLine();


			while((line = reader.readLine()) != null) { // for each query
				long query_time = 0;

				// get the values that define the query
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query
				String param1 = values[0];

				// format the query
				query = String.format(query, "<" + param1 + ">");

				TupleQuery tupleQuery;
				TupleQueryResult result = null;


				for( int j = 0; j < MyValues.execute_a_query_this_many_times; ++j) {
					//start the timer
					long query_start_time = System.currentTimeMillis();

					tupleQuery = repo.prepareTupleQuery(query);
					result = tupleQuery.evaluate();

					if(j == 0 && result.hasNext())
						hits++;// we have a hit

					result.close();



					//stop the timer
					long current_execution_query_time = System.currentTimeMillis() - query_start_time;

					//add this time to the overall execution time of this query
					query_time += current_execution_query_time;
				}


				counter++; 
				// compute the average of this execution
				double query_average = (double) query_time / MyValues.execute_a_query_this_many_times;

				// add the average to the total execution time
				total_query_class_time += query_average;
			}


			double average_class_time = (double) total_query_class_time /counter; 

			if(print)
				System.out.println("    total time in ms: " + total_query_class_time + 
						" ms, \n     average time for one query: " + average_class_time + " ms\n"
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


	public void performClass10AndTakeAvgTimes(String repositorypath, String queryValuesPath, boolean using_named, boolean print) {
		// open the triplestore
		RepositoryConnection repo = TripleStoreHandler.openRepositoryAndConnection(repositorypath);

		// prepare the SPARQL query
		String query;
		if(using_named) {
			query = BSBMQuery10.select_named;
		} else {
			query = BSBMQuery10.select;
		}

		// read the values. Each set of values is a query, thus one execution
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(queryValuesPath))) {
			String line = "";
			long start = System.currentTimeMillis();// starting time
			int counter = 0;// to count how many query we executed
			int hits = 0;
			double total_query_class_time = 0; // sum of the average time of each query

			// need to consume the first line, containing the attribute names
			reader.readLine();


			while((line = reader.readLine()) != null) { // for each query
				long query_time = 0;

				// get the values that define the query
				String[] values = line.split(",");

				// let us isolate the parameters that build up this query
				String param1 = values[0];

				// format the query
				query = String.format(query, "<" + param1 + ">", "<" + param1 + ">");

				TupleQuery tupleQuery;
				TupleQueryResult result = null;


				for( int j = 0; j < MyValues.execute_a_query_this_many_times; ++j) {
					//start the timer
					long query_start_time = System.currentTimeMillis();

					tupleQuery = repo.prepareTupleQuery(query);
					result = tupleQuery.evaluate();

					if(j == 0 && result.hasNext())
						hits++;// we have a hit

					result.close();



					//stop the timer
					long current_execution_query_time = System.currentTimeMillis() - query_start_time;

					//add this time to the overall execution time of this query
					query_time += current_execution_query_time;
				}


				counter++; 
				// compute the average of this execution
				double query_average = (double) query_time / MyValues.execute_a_query_this_many_times;

				// add the average to the total execution time
				total_query_class_time += query_average;
			}


			double average_class_time = (double) total_query_class_time /counter; 

			if(print)
				System.out.println("    total time in ms: " + total_query_class_time + 
						" ms, \n     average time for one query: " + average_class_time + " ms\n"
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




	/** Test main, to use this class as a script */
	public static void main(String[] args) {
		// get the file of values
		MyPaths.setup();; // read the paths from the properties file
		MyValues.setup();

		TakeTimes execution = new TakeTimes();

		// path of the file used to build queries
		String queryValuesPath = MyPaths.values_path;

		// path of the index
		String tripleStorePath = MyPaths.querying_index;


		// whole database, warm up round
		if(MyValues.areWeDoingTheWarmUp && MyValues.areWeInterrogatingTheWholeTripleStore) {
			//do one round because we are sillybilly and we let the system crunch some queries and optimize them
			System.out.println("Whole database, no warm up");
			execution.performQueriesToTakeTimeForClassOfQueries(MyValues.QUERYCLASS,tripleStorePath, queryValuesPath, false, true);			
		}

		// whole database
		if(MyValues.areWeInterrogatingTheWholeTripleStore) {
			System.out.println("\n\nWhole database, after warm up:");
			execution.performQueriesToTakeTimeForClassOfQueries(MyValues.QUERYCLASS, tripleStorePath, queryValuesPath, false, true);
		}

		// Named database
		if(MyValues.areWeDoingTheWarmUp && MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
			System.out.println("\nNamed Database, no warmup");
			execution.performQueriesToTakeTimeForClassOfQueries(MyValues.QUERYCLASS, tripleStorePath, queryValuesPath, true, true);			
		}

		if(MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
			System.out.println("\nNamed Database, after warmup");
			execution.performQueriesToTakeTimeForClassOfQueries(MyValues.QUERYCLASS, tripleStorePath, queryValuesPath, true, true);			
		}

		// reduced database
		if(MyValues.areWeDoingTheWarmUp && MyValues.areWeInterrogatingTheReducedTripleStore) {
			System.out.println("\n\n'reduced' database, no warmup");
			execution.performQueriesToTakeTimeForClassOfQueries(MyValues.QUERYCLASS, MyPaths.reduced_index_path, queryValuesPath, false, true);
		}

		if(MyValues.areWeInterrogatingTheReducedTripleStore) {
			System.out.println("\n\n'reduced' database, after warmup");
			execution.performQueriesToTakeTimeForClassOfQueries(MyValues.QUERYCLASS, MyPaths.reduced_index_path, queryValuesPath, false, true);
		}

	}

}

