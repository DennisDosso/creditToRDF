package it.unipd.dei.ims.data;

import java.util.Map;

import it.unipd.dei.ims.credit.distribution.BSBMCreditDistributor;
import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;

public class MyValues {

	/** number of different queries for each class*/
	public static int how_many_queries = 10000;

	/** Maximum number of times a query has been performed*/
	public static int max_times_per_one_query = 300;

	public static String namedGraph;
	
	public static int creditThreshold = 0;
	
	public static boolean areWeDoingTheWarmUp, areWeInterrogatingTheWholeTripleStore, areWeInterrogatingTheWholeNamedTripleStore, areWeInterrogatingTheReducedTripleStore;
	
	/** Classes of queries we are going to use */
	public enum QueryClass {
		ONE, 
		TWO,
		THREE,
		FOUR,
		FIVE,
		EIGHT;
	}

	/** Class of queries we are using. Change between ONE, TWO, THREE etc. */
	public static QueryClass QUERYCLASS;
	
	/** Used to dictate how many times one query needs to be repeated
	 * to take the average time. Default is 10.
	 * */
	public static int execute_a_query_this_many_times = 10;

	public MyValues() {
		Map<String, String> map = PropertiesUtils.getPropertyMap("properties/values.properties");

		String class_ = map.get("class");
		if(class_.equals("1"))
			QUERYCLASS = QueryClass.ONE;

		how_many_queries = Integer.parseInt(map.get("how_many_queries"));
		max_times_per_one_query = Integer.parseInt(map.get("max_times_per_one_query"));
		namedGraph = map.get("named.graph");
		creditThreshold = Integer.parseInt(map.get("credit.threshold"));
	}

	public static void setup() {
		Map<String, String> map = PropertiesUtils.getPropertyMap("properties/values.properties");

		String class_ = map.get("class");
		if(class_.equals("1"))
			QUERYCLASS = QueryClass.ONE;
		if(class_.contentEquals("8"))
			QUERYCLASS = QueryClass.EIGHT;

		how_many_queries = Integer.parseInt(map.get("how_many_queries"));
		max_times_per_one_query = Integer.parseInt(map.get("max_times_per_one_query"));
		namedGraph = map.get("named.graph");
		creditThreshold = Integer.parseInt(map.get("credit.threshold"));
		
		areWeDoingTheWarmUp = Boolean.parseBoolean(map.get("are.we.doing.the.warmup"));
		areWeInterrogatingTheWholeTripleStore = Boolean.parseBoolean(map.get("are.we.interrogating.the.whole.triplestore"));
		areWeInterrogatingTheWholeNamedTripleStore= Boolean.parseBoolean(map.get("are.we.interrogating.the.whole.named.triplestore"));
		areWeInterrogatingTheReducedTripleStore = Boolean.parseBoolean(map.get("are.we.interrogating.the.reduced.triplestore"));
		execute_a_query_this_many_times = Integer.parseInt(map.get("execute.a.query.this.many.times"));
	}
}
