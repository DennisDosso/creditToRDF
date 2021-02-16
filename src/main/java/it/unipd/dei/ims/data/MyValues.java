package it.unipd.dei.ims.data;

import java.util.Map;

import it.unipd.dei.ims.credit.distribution.BSBMCreditDistributor;
import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;

public class MyValues {

	/** number of different queries for each class. Default at 100*/
	public static int how_many_queries = 100;

	/** Maximum number of times a query has been performed*/
	public static int max_times_per_one_query = 300;

	public static String namedGraph;

	/** Threshold used for credit. Default at 0*/
	public static int creditThreshold = 0;

	/** String containing the specifications for the indexes that we want to use for the RDF triplestore
	 * Default is "spoc,posc" */
	public static String indexString = "spoc,posc";

	/** How many queries we execute queries to distribute credit */
	public static int queryNumberCredit = 100;

	/** Number of queries we execute to measure hits on cache*/
	public static int queryNumberHit = 100;

	/** number used to compute the standard deviation of the normal distribution. Default at 6.
	 * The bigger, the more the curve is "concentrated" around the mean*/
	public static int standardDeviationRatio = 6;

	public static boolean areWeDoingTheWarmUp, 
	areWeInterrogatingTheWholeTripleStore, 
	areWeInterrogatingTheWholeNamedTripleStore, 
	areWeInterrogatingTheReducedTripleStore, 
	areWeInterrogatingTheCache;

	public static boolean areWeDistributingCredit;

	/** Describes the plan of queries to be written */
	public static String printingPlan;

	/** Classes of queries we are going to use */
	public enum QueryClass {
		ONE, 
		TWO,
		FIVE,
		SIX,
		SEVEN,
		EIGHT,
		TEN;
	}

	/** Class of queries we are using. Change between ONE, TWO, THREE etc. */
	public static QueryClass QUERYCLASS;

	public enum CoolDownStrategy {
		NONE, // no cooldown is used
		TIME, // we use the cooldown based on temporal queries
		FUNCTION; // we use a cooldown function
	}

	public static CoolDownStrategy coolDownStrategy;

	public enum ThresholdStrategy {
		COSTANT,
		ADAPTIVE;
	}

	/** Strategy used to set the threshold*/
	public static ThresholdStrategy thresholdStrategy;
	
	public enum QueryPlan {
		UNIFORM,
		MIXED;
	}
	
	public static QueryPlan queryPlan;
	
	public static int howManyEpochs;
	
	/** Set this to true if it is necessary to check that the triples produced
	 * by the construct queries are or not present in the original triplestore */
	public static boolean constructCheck;
	
	public static int epochLength;



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
		if(class_.equals("5"))
			QUERYCLASS = QueryClass.FIVE;
		if(class_.equals("7"))
			QUERYCLASS = QueryClass.SEVEN;
		if(class_.contentEquals("8"))
			QUERYCLASS = QueryClass.EIGHT;
		if(class_.equals("10"))
			QUERYCLASS = QueryClass.TEN;

		String cds = map.get("cool.down.strategy");
		if(cds.equals("NONE"))
			coolDownStrategy = CoolDownStrategy.NONE;
		if(cds.equals("TIME"))
			coolDownStrategy = CoolDownStrategy.TIME;
		if(cds.equals("FUNCTION"))
			coolDownStrategy = CoolDownStrategy.FUNCTION;

		String thr = map.get("threshold.strategy");
		if(thr.contentEquals("CONSTANT"))
			thresholdStrategy = ThresholdStrategy.COSTANT;
		if(thr.contentEquals("ADAPTIVE"))
			thresholdStrategy = ThresholdStrategy.ADAPTIVE;
		
		String qp = map.get("query.plan");
		if(qp.equals("UNIFORM"))
			queryPlan = QueryPlan.UNIFORM;
		if(qp.equals("MIXED"))
			queryPlan = QueryPlan.MIXED;
			

		how_many_queries = Integer.parseInt(map.get("how_many_queries"));
		max_times_per_one_query = Integer.parseInt(map.get("max_times_per_one_query"));
		namedGraph = map.get("named.graph");
		creditThreshold = Integer.parseInt(map.get("credit.threshold"));

		areWeDoingTheWarmUp = Boolean.parseBoolean(map.get("are.we.doing.the.warmup"));
		areWeInterrogatingTheWholeTripleStore = Boolean.parseBoolean(map.get("are.we.interrogating.the.whole.triplestore"));
		areWeInterrogatingTheWholeNamedTripleStore= Boolean.parseBoolean(map.get("are.we.interrogating.the.whole.named.triplestore"));
		areWeInterrogatingTheReducedTripleStore = Boolean.parseBoolean(map.get("are.we.interrogating.the.reduced.triplestore"));
		areWeInterrogatingTheCache = Boolean.parseBoolean(map.get("are.we.interrogating.the.cache"));

		execute_a_query_this_many_times = Integer.parseInt(map.get("execute.a.query.this.many.times"));
		indexString = map.get("indexes.string");
		queryNumberCredit = Integer.parseInt(map.get("query.number.credit"));
		queryNumberHit = Integer.parseInt(map.get("query.number.hit"));
		standardDeviationRatio = Integer.parseInt(map.get("standard.deviation.ratio"));
		printingPlan = map.get("printing.plan");

		areWeDistributingCredit = Boolean.parseBoolean(map.get("are.we.distributing.credit"));
		
		howManyEpochs = Integer.parseInt(map.get("how.many.epochs"));
		
		constructCheck = Boolean.parseBoolean(map.get("construct.check"));
		
		epochLength = Integer.parseInt(map.get("epoch.length"));
	}

	public static QueryClass convertToQueryClass(String c) {
		if(c.equals("ONE")) {
			return QueryClass.ONE;
		}
		else if(c.equals("TWO"))
			return QueryClass.TWO;
		else if(c.equals("FIVE"))
			return QueryClass.FIVE;
		else if(c.equals("SIX"))
			return QueryClass.SIX;
		else if(c.equals("SEVEN"))
			return QueryClass.SEVEN;
		else if(c.equals("EIGHT"))
			return QueryClass.EIGHT;
		else if(c.equals("TEN"))
			return QueryClass.TEN;
		
		return null;
		

	}
}
