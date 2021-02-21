package experiment1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import execution.ProduceValuesToPerformQueries;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.MyValues.QueryClass;

/** builds a series of queries, in CONSTRUCT and SELECT 
 * 
 * 
 * BuildAndPrintQueries
 * 
 * 
 * PHASE 1: Build and Print values that are needed to define queries
 * 
 * 
 * */
public class BuildAndPrintQueries extends ProduceValuesToPerformQueries {

	/** Needed properties: 
	 * MyPaths.querying_index
	 * 
	 * */
	public BuildAndPrintQueries() {
		MyValues.setup();
		MyPaths.setup();
	}


	/** Given one class of queries, it builds a list of values that can build that query.
	 * <p>
	 * The last values in each line is the class of that query*/
	public void buildTheseManyQueriesForThisClass(int number_of_queries, MyValues.QueryClass query_class) throws IOException {
		//open the triple store
		RepositoryConnection rc = TripleStoreHandler.openRepositoryAndConnection(MyPaths.querying_index);

		//prepare the query to get the values
		TupleQuery query = null;
		if(query_class == MyValues.QueryClass.ONE) {
			query = rc.prepareTupleQuery(queryClass1);			
		} else if (query_class == MyValues.QueryClass.TWO) {
			query = rc.prepareTupleQuery(queryClass2);
		} else if (query_class == MyValues.QueryClass.FIVE) {
			query = rc.prepareTupleQuery(queryClass5);
		} else if (query_class == MyValues.QueryClass.SIX) {
			query = rc.prepareTupleQuery(queryClass6);
		} else if (query_class == MyValues.QueryClass.SEVEN) {
			query = rc.prepareTupleQuery(queryClass7);
		} else if (query_class == MyValues.QueryClass.EIGHT) {
			query = rc.prepareTupleQuery(queryClass8);
		} else if (query_class == MyValues.QueryClass.TEN) {
			query = rc.prepareTupleQuery(queryClass10);
		} else if (query_class == MyValues.QueryClass.SEVENB) {
			query = rc.prepareTupleQuery(queryClass7Big);
		} else if (query_class == MyValues.QueryClass.EIGHTB) {
			query = rc.prepareTupleQuery(queryClass8Big);
		} else if (query_class == MyValues.QueryClass.TENB) {
			query = rc.prepareTupleQuery(queryClass10Big);
		}

		// this list will contain the parameters to perform the queries
		ArrayList<String[]> valuesList = new ArrayList<String[]>(); 

		// execute the query and get the parameters
		try(TupleQueryResult res = query.evaluate()) {
			// for each set of parameters (a different new query is created)
			for (BindingSet solution: res) {
				if(query_class == MyValues.QueryClass.ONE) {
					String param1 = solution.getValue("p").toString();
					String param2 = solution.getValue("pf1").toString();
					String param3 = solution.getValue("pf2").toString();
					String[] parameters = {param1, param2, param3, "ONE"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.TWO) {
					String param1 = solution.getValue("p1").toString();
					String[] parameters = {param1, "TWO"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.FIVE) {
					String param1 = solution.getValue("p").toString();
					String[] parameters = {param1, "FIVE"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.SIX) {
					String param1 = solution.getValue("label").toString();
					String[] parameters = {param1, "SIX"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.SEVEN) {
					String param1 =solution.getValue("p").toString();
					String param2 = solution.getValue("c").toString();
					String[] parameters = {param1, param2, "SEVEN"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.EIGHT) {
					String param1 = solution.getValue("p").toString();
					String[] parameters = {param1, "EIGHT"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.TEN) {
					String param1 = solution.getValue("p").toString();
					String[] parameters = {param1, "TEN"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.SEVENB) {
					String param1 =solution.getValue("p").toString();
					String[] parameters = {param1, "SEVENB"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.EIGHTB) {
					String param1 = solution.getValue("p").toString();
					String[] parameters = {param1, "EIGHTB"};
					valuesList.add(parameters);
				} else if (query_class == MyValues.QueryClass.TENB) {
					String param1 = solution.getValue("p").toString();
					String[] parameters = {param1, "TENB"};
					valuesList.add(parameters);
				}

			}
			// randomize a little bit the distribution of queries
			Collections.shuffle(valuesList);
		}

		// now we build and print the queries

		// get a normal distribution to choose the queries to print
		double stdv = Math.max(1, (double) valuesList.size()/MyValues.standardDeviationRatio);
		NormalDistribution distribution = new NormalDistribution(valuesList.size()/2, stdv);

		// open the writer in append
		FileWriter w = new FileWriter(MyPaths.queryValuesFile, true);
		BufferedWriter writer = new BufferedWriter(w); 
		PrintWriter pw = new PrintWriter(writer);

		// the header tells the class
		pw.println("epoch," + query_class);


		// now write the single values
		for(int i = 0; i < number_of_queries; ++i) {
			// decide which query to write
			int randomNum =  (int) Math.floor(distribution.sample());
			// need to be sure to have a number that can be used
			while(randomNum < 0 || randomNum > valuesList.size() - 1)
				randomNum = (int) Math.floor(distribution.sample());

			// get the values
			String[] parameters = valuesList.get(randomNum);
			for (int j = 0; j < parameters.length; ++j) {
				if (j == parameters.length - 1)
					pw.println(parameters[j]);
				else 
					pw.print(parameters[j] + ",");
			}
		}
		// flush and close
		pw.flush();
		pw.close();
		System.out.println("printed one class of queries");
	}

	/** Given a csv string that contains unique values of classes to be created, e.g. ONE,TWO,FIVE
	 *  it creates a list of queries that are randomly taken from these classes. The produced list 
	 *  contains an equal number of queries taken from the classes. 
	 * @throws IOException 
	 *  */
	public void writeTheseManyQueriesTakingThemRandomlyFromThisListOfClasses(int number_of_queries, String list_of_classes) throws IOException {
		System.out.println("Printing mixed queries");
		String[] classes = list_of_classes.split(",");
		// each class will be equally represented
		int times_one_class_is_built = (int) number_of_queries / list_of_classes.length();

		//open the triple store
		RepositoryConnection rc = TripleStoreHandler.openRepositoryAndConnection(MyPaths.querying_index);
		// this list will contain the parameters to perform the queries
		ArrayList<String[]> valuesList = new ArrayList<String[]>(); 

		for(String qc: classes) {
			// for each of the available classes
			MyValues.QueryClass query_class = MyValues.convertToQueryClass(qc);
			//prepare the query to get the values
			TupleQuery query = null;
			if(query_class == MyValues.QueryClass.ONE) {
				query = rc.prepareTupleQuery(queryClass1);			
			} else if (query_class == MyValues.QueryClass.TWO) {
				query = rc.prepareTupleQuery(queryClass2);
			} else if (query_class == MyValues.QueryClass.FIVE) {
				query = rc.prepareTupleQuery(queryClass5);
			} else if (query_class == MyValues.QueryClass.SIX) {
				query = rc.prepareTupleQuery(queryClass6);
			} else if (query_class == MyValues.QueryClass.SEVEN) {
				query = rc.prepareTupleQuery(queryClass7);
			} else if (query_class == MyValues.QueryClass.EIGHT) {
				query = rc.prepareTupleQuery(queryClass8);
			} else if (query_class == MyValues.QueryClass.TEN) {
				query = rc.prepareTupleQuery(queryClass10);
			} else if (query_class == MyValues.QueryClass.SEVENB) {
				query = rc.prepareTupleQuery(queryClass7Big);
			} else if (query_class == MyValues.QueryClass.EIGHTB) {
				query = rc.prepareTupleQuery(queryClass8Big);
			} else if (query_class == MyValues.QueryClass.TENB) {
				query = rc.prepareTupleQuery(queryClass10Big);
			}

			// execute the query and get the parameters
			try(TupleQueryResult res = query.evaluate()) {
				ArrayList<String[]> classList = new ArrayList<String[]>();

				while(res.hasNext()) {
					BindingSet solution = res.next();
					// for each set of parameters (a different new query is created)
					if(query_class == MyValues.QueryClass.ONE) {
						String param1 = solution.getValue("p").toString();
						String param2 = solution.getValue("pf1").toString();
						String param3 = solution.getValue("pf2").toString();
						String[] parameters = {param1, param2, param3, "ONE"};
						classList.add(parameters);
					} else if (query_class == MyValues.QueryClass.TWO) {
						String param1 = solution.getValue("p1").toString();
						String[] parameters = {param1, "TWO"};
						valuesList.add(parameters);
					} else if (query_class == MyValues.QueryClass.FIVE) {
						String param1 = solution.getValue("p").toString();
						String[] parameters = {param1, "FIVE"};
						valuesList.add(parameters);
					} else if (query_class == MyValues.QueryClass.SIX) {
						String param1 = solution.getValue("label").toString();
						String[] parameters = {param1, "SIX"};
						valuesList.add(parameters);
					} else if (query_class == MyValues.QueryClass.SEVEN) {
						String param1 =solution.getValue("p").toString();
						String param2 = solution.getValue("c").toString();
						String[] parameters = {param1, param2, "SEVEN"};
						classList.add(parameters);
					} else if (query_class == MyValues.QueryClass.EIGHT) {
						String param1 = solution.getValue("p").toString();
						String[] parameters = {param1, "EIGHT"};
						classList.add(parameters);
					} else if (query_class == MyValues.QueryClass.TEN) {
						String param1 = solution.getValue("p").toString();
						String[] parameters = {param1, "TEN"};
						classList.add(parameters);
					} else if (query_class == MyValues.QueryClass.SEVENB) {
						String param1 =solution.getValue("p").toString();
						String[] parameters = {param1, "SEVENB"};
						classList.add(parameters);
					} else if (query_class == MyValues.QueryClass.EIGHTB) {
						String param1 = solution.getValue("p").toString();
						String[] parameters = {param1, "EIGHTB"};
						classList.add(parameters);
					} else if (query_class == MyValues.QueryClass.TENB) {
						String param1 = solution.getValue("p").toString();
						String[] parameters = {param1, "TENB"};
						classList.add(parameters);
					}
				}// end scan over 1 class of queries

				// shuffle a little bit
				Collections.shuffle(classList);
				int share = Math.min(times_one_class_is_built, classList.size());
				// add this class to the values
				valuesList.addAll(classList.subList(0, share));
			}
		}// scan over all classes 
		
		System.out.println("finished asking queries, now printing them");

		// shuffle all of them 
		Collections.shuffle(valuesList);

		// get a normal distribution to choose the queries to print
		NormalDistribution distribution = new NormalDistribution(valuesList.size()/2, valuesList.size()/MyValues.standardDeviationRatio);

		// open the writer in append
		FileWriter w = new FileWriter(MyPaths.queryValuesFile, true);
		BufferedWriter writer = new BufferedWriter(w); 
		PrintWriter pw = new PrintWriter(writer);

		// tell that one year has passed
		pw.println("epoch, variable");

		// now write the single values
		for(int i = 0; i < number_of_queries; ++i) {
			// decide which query to write
			int randomNum =  (int) Math.floor(distribution.sample());
			// need to be sure to have a number that can be used
			while(randomNum < 0 || randomNum > valuesList.size() - 1)
				randomNum = (int) Math.floor(distribution.sample());

			// get the values
			String[] parameters = valuesList.get(randomNum);
			for (int j = 0; j < parameters.length; ++j) {
				if (j == parameters.length - 1)
					pw.println(parameters[j]);
				else 
					pw.print(parameters[j] + ",");
			}
		}
		// flush and close
		pw.flush();
		pw.close();
	}


	/** Provided a string, the plan, containing csv with values describing the printing plan, it 
	 * prints different values to build queries 
	 * 
	 * 
	 * */
	public void writeQueriesFollowingThePlan() throws IOException {
		if(MyValues.queryPlan == MyValues.QueryPlan.UNIFORM) {// when we want to print a series of queries in block. Each block is made by queries of the same class
			System.out.println("following the UNIFORM plan");
			
			// take a string that describes the desiderd plan to write queries
			String[] plan = MyValues.printingPlan.split(",");
			for( String sec : plan) {
				
				QueryClass p = MyValues.convertToQueryClass(sec);
				this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, p);
//				
//				if(sec.equals("ONE")) {
//					this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, MyValues.QueryClass.ONE);
//					
//				} else if (sec.equals("TWO")) {
//					this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, MyValues.QueryClass.TWO);
//				} else if (sec.equals("FIVE")) {
//					this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, MyValues.QueryClass.FIVE);
//				} else if (sec.equals("SIX")) {
//					this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, MyValues.QueryClass.SIX);
//				} else if (sec.equals("SEVEN")) {
//					this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, MyValues.QueryClass.SEVEN);
//				} else if (sec.equals("EIGHT")) {
//					this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, MyValues.QueryClass.EIGHT);
//				} else if (sec.equals("TEN")) {
//					this.buildTheseManyQueriesForThisClass(MyValues.how_many_queries, MyValues.QueryClass.TEN);
//				}
			}			
		} else if (MyValues.queryPlan == MyValues.QueryPlan.MIXED) { // in the case we want a list of queries that are of mixed classes
			this.writeTheseManyQueriesTakingThemRandomlyFromThisListOfClasses(MyValues.how_many_queries, MyValues.printingPlan);
		}
	}

	public void close() {
		TripleStoreHandler.closeRepositoryAndConnextion();
	}


	public static void main(String[] args) throws IOException {
		System.out.println("let's write some queries, baby!");
		BuildAndPrintQueries execution = new BuildAndPrintQueries();

		execution.writeQueriesFollowingThePlan();
		execution.close();

		System.out.println("done");
	}
}
