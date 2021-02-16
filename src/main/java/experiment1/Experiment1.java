package experiment1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import it.unipd.dei.ims.credittordf.utils.CacheHandler;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.BSBMQuery1;
import it.unipd.dei.ims.data.BSBMQuery10;
import it.unipd.dei.ims.data.BSBMQuery2;
import it.unipd.dei.ims.data.BSBMQuery5;
import it.unipd.dei.ims.data.BSBMQuery6;
import it.unipd.dei.ims.data.BSBMQuery7;
import it.unipd.dei.ims.data.BSBMQuery8;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;

/** Executes queries of one or more classes, on different storages (whole database, a subset, a cache), distributes
 * the credit.
 * 
 * NB: when starting fro scatch, remember to truncate the triplestore table*/
public class Experiment1 {


	private final String updateHits = "UPDATE %s.triplestore\n" + 
			"SET hits = hits + ? \n" + 
			"WHERE subject=? AND predicate=? AND \"object\"=?;\n" + 
			"";

	private final String INSERT_TRIPLE = "INSERT INTO %s.triplestore\n" + 
			"(subject, predicate, \"object\", credit, hits)\n" + 
			"VALUES(?, ?, ?, 0, 1);";

	private final String INSERT_TRIPLE_WITH_HITS = "INSERT INTO %s.triplestore\n" + 
			"(subject, predicate, \"object\", credit, hits)\n" + 
			"VALUES(?, ?, ?, 0, ?);";

	private  String CHECK_TRIPLE_PRESENCE = "SELECT subject FROM %s.triplestore where subject = ? AND predicate = ? AND object = ?";

	private final String updateWithCredit = "update %s.triplestore\n" + 
			"set credit = credit + ln(hits + 1)\n" + 
			"where hits > 0";

	protected RepositoryConnection repoConnection;

	protected CacheHandler cacheHandler;

	/** Query to select triples in the relational database that have credit bigger than 0 
	 * */
	private final String SELECT_CREDITED_TRIPLES = "select t.subject , t.predicate , t.\"object\" \n" + 
			"from %s.triplestore t \n" + 
			"where credit > ?";

	/** Updates a triple by changing its named graph. 
	 * */
	private final String SPARQL_UPDATE = "DELETE WHERE { <%s> <%s> %s}; "
			+ "INSERT DATA {"
			+ "GRAPH <%s> {"
			+ "<%s> <%s> %s }}";

	private final String SPARQL_ASK = "ASK WHERE {<%s> <%s> %s}";


	/** A map that I use to keep track of the presence/absence of triples in the triplestore. 
	 * This is necessary due to the fact that we use queries with an OPTIONAL operation, 
	 * thus some triples may not be actually present in the graph. This method makes sure that this is not the case.
	 * Obviously, the mao may become too big, thus there may be a time when we need to keep it smaller.
	 * */
	private Map<String, Boolean> presenceOfTriplesMap;

	/** Used to update the RDB with hits in a quick way*/
	private Map<String, Integer> triplesToUpdate, triplesToInsert;

	/** A map containing the lineage of queries already produced */
	private Map<String, List<String[]>> lineageMap;

	/** Sets up the values.properties, rdb.properties, paths.properties and the connection to the RDB
	 * @throws SQLException */
	public Experiment1() throws SQLException {
		MyValues.setup();
		MyPaths.setup();
		RDB.setup();
		TripleStoreHandler.openRepositoryAndConnection(MyPaths.querying_index);

		// create cache in memory
		this.cacheHandler = new CacheHandler();
		this.repoConnection = this.cacheHandler.cacheConnection;

		presenceOfTriplesMap = new HashMap<String, Boolean>();
		triplesToUpdate = new HashMap<String, Integer>();
		triplesToInsert = new HashMap<String, Integer>();

		this.lineageMap = new HashMap<String, List<String[]>>();

		ConnectionHandler.createConnection(RDB.produceJdbcString());
		ConnectionHandler.getConnection().setAutoCommit(false);
	}


	//MAIN METHOD
	/** Given a file defining a query plan, it executes it following different possibilities of execution. 
	 * @throws SQLException 
	 * 
	 * */
	public void executeTheQueryPlan() throws SQLException {
		System.out.println("starting the query plan...");
		MyValues.QueryClass query_class = null;

		int cacheHit = 0, cacheMiss = 0;

		List<Long> updateCacheTimes = new ArrayList<Long>(); // time to update cache, each epoch
		List<Long> updateNamedGraphTimes = new ArrayList<Long>();// time to update named graph, each epoch

		List<Long> updateRDBTimes = new ArrayList<Long>(); // time to update the support RDB


		List<Long> wholeDbTimes = new ArrayList<Long>();
		List<Long> namedDbTimes = new ArrayList<Long>();
		List<Long> cacheTimes = new ArrayList<Long>();

		// get the plan file and read it
		Path p = Paths.get(MyPaths.queryValuesFile);

		// timer to decide when one epoch has passed and it is time to refresh the cache 
		int epochTimer = 0; 

		try(BufferedReader reader = Files.newBufferedReader(p)) {

			String line = "";

			while((line = reader.readLine()) != null) {
				// for each query in the query plan

				// take the values forming the query
				String[] values = line.split(",");
				if(values[0].equals("epoch")) {
					//change of query class, do nothing
					try {
						System.out.println("Change of class: " + values[1]);						
					} catch(Exception e) {
						// I do not want a stupid index exception in case I change the format of the csv file to ruin my execution
					}
				} else  
				{
					if(  epochTimer % MyValues.epochLength == 0 && epochTimer != 0 ) { // new epoch - need to update some things
						// in this special case one epoch has passed or we changed query class

						ReturnBox box = this.oneYearHasPassed(MyValues.coolDownStrategy);

						// take note of the time required to update cache/named graph
						if(MyValues.areWeInterrogatingTheCache)
							updateCacheTimes.add(box.nanoTime);
						if(MyValues.areWeInterrogatingTheWholeNamedTripleStore)
							updateNamedGraphTimes.add(box.nanoTime);


						System.out.println("one epoch has passed, cache size: " + box.size + " cache hits: " + cacheHit);
						if(values.length == 2 && values[0].equals("epoch"))
							System.out.println("\nchange of class!!\n");

					}	
					// TODO debug, to be removed
					if(epochTimer % 20 == 0)
						System.out.println("processed " + epochTimer + " queries");

					// now we process the query

					//get the class of this query
					query_class  = MyValues.convertToQueryClass(values[values.length - 1]);

					// distribute the credit  - this may require some time due to the operations on the support RDB and the check for the LINEAGE
					if(MyValues.areWeDistributingCredit) {
						// these two lineas are a little strange, but they are necessary because of how I built assignHitsWithOneQuery. 
						List<String[]> v = new ArrayList<String[]>();
						v.add(values);

						long overheadTime = this.assignHitsWithOneQuery(query_class, v, 0);

						// take note of the time required to distribute the credit
						updateRDBTimes.add(overheadTime);
					}

					// query the whole database
					if(MyValues.areWeInterrogatingTheWholeTripleStore) {
						ReturnBox box = this.queryTheTripleStore(query_class, false, values);
						wholeDbTimes.add(box.nanoTime);
					}

					// query the named graph
					if(MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
						ReturnBox box = this.queryTheTripleStore(query_class, true, values);
						long time = box.nanoTime;
						if(!box.foundSomething) { // cache miss
							box = this.queryTheTripleStore(query_class, false, values);
							time += box.nanoTime;
							cacheMiss ++;
						} else {
							// cache hit
							cacheHit ++;
						}
						namedDbTimes.add(time);
					}

					// query the cache
					if(MyValues.areWeInterrogatingTheCache) {
						ReturnBox box = this.queryTheCache(query_class, values, this.repoConnection);
						Long time = box.nanoTime;
						if(!box.foundSomething) { 
							// cache miss
							box = this.queryTheTripleStore(query_class, false, values);
							time += box.nanoTime;
							cacheMiss ++;
						} else {
							// cache hit
							cacheHit ++;
						}
						cacheTimes.add(time);
					}

					epochTimer++; // one line read, proceed
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// at the end print the different execution times
		if(MyValues.areWeDistributingCredit) {
			this.printOneArrayOfTimes(updateRDBTimes, "updateRDBTime");// overhead due to maintaining the hits
		} 

		if(MyValues.areWeInterrogatingTheWholeTripleStore) {
			this.printOneArrayOfTimes(wholeDbTimes, "whole");
		}

		if(MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
			this.printOneArrayOfTimes(namedDbTimes, "named");
			this.printOneArrayOfTimes(updateNamedGraphTimes, "update_named"); // overhead due to updating the named graph at each epoch
		}

		if(MyValues.areWeInterrogatingTheCache) {
			this.printOneArrayOfTimes(cacheTimes, "cache");
			this.printOneArrayOfTimes(updateCacheTimes, "update_cache"); // overhead due to refreshing the cache each new epoch has passed
		}

		// TODO adesso serve testare che tutto fili liscio col primo set di esperimenti, e con threshold 0
		System.out.println("cache hits: " + cacheHit + ", cache miss: " + cacheMiss);
	}

	protected void printOneArrayOfTimes(List<Long> times, String what) {
		Path pa = null;

		// we take the right path, based on 'what' we are doing, where to write the times
		if(what.equals("updateRDBTime")) {
			pa = Paths.get(MyPaths.overheadTimes);
		} else if (what.equals("whole"))
			pa = Paths.get(MyPaths.wholeDbTimes);
		else if (what.equals("named"))
			pa = Paths.get(MyPaths.namedDbTimes);
		else if (what.equals("cache"))
			pa = Paths.get(MyPaths.cacheTimes);
		else if (what.equals("update_cache"))
			pa = Paths.get(MyPaths.updateCacheTimes);
		else if (what.equals("update_named"))
			pa = Paths.get(MyPaths.updateNamedTimes);


		try(BufferedWriter writer = Files.newBufferedWriter(pa)) {
			for(Long time : times) {
				writer.write(time + "");
				writer.newLine();
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected ReturnBox queryTheCache(MyValues.QueryClass query_class, String[] values, RepositoryConnection repoConnection) {
		String query = this.decideTheQuery(query_class, false, values);

		ReturnBox box = new ReturnBox();

		// prepare to execute the query and take the time
		long query_start_time = System.nanoTime();
		TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
		TupleQueryResult result = tupleQuery.evaluate();
		if(result.hasNext()) {
			result.next();
			box.foundSomething = true;
		} else {
			box.foundSomething = false;
		}

		result.close();

		long required_time = System.nanoTime() - query_start_time;
		box.nanoTime = required_time;
		return box;
	}

	/** Convenience method used to decide the select query depending on the situation */
	private String decideTheQuery(MyValues.QueryClass query_class, boolean using_named_graph, String[] values) {
		String query = "", named_query = "", whole_query = "";

		// decide which named query based on the class
		if (query_class == MyValues.QueryClass.ONE) {
			named_query = BSBMQuery1.select_named;
		} else if (query_class == MyValues.QueryClass.TWO) {
			named_query = BSBMQuery2.select_named;
		} else if (query_class == MyValues.QueryClass.FIVE) {
			named_query = BSBMQuery5.select_named;
		} else if (query_class == MyValues.QueryClass.SIX) {
			named_query = BSBMQuery6.select_named;
		} else if (query_class == MyValues.QueryClass.SEVEN) {
			named_query = BSBMQuery7.select_named;
		} else if (query_class == MyValues.QueryClass.EIGHT) {
			named_query = BSBMQuery8.select_query_with_named_graphs;
		} else if (query_class == MyValues.QueryClass.TEN) {
			named_query = BSBMQuery10.select_named;
		}

		// decide whole query based on the class
		if (query_class == MyValues.QueryClass.ONE) {
			whole_query = BSBMQuery1.select;
		} else if (query_class == MyValues.QueryClass.TWO) {
			whole_query = BSBMQuery2.select;
		} else if (query_class == MyValues.QueryClass.FIVE) {
			whole_query = BSBMQuery5.select;
		} else if (query_class == MyValues.QueryClass.SIX) {
			whole_query = BSBMQuery6.select;
		} else if (query_class == MyValues.QueryClass.SEVEN) {
			whole_query = BSBMQuery7.select;
		} else if (query_class == MyValues.QueryClass.EIGHT) {
			whole_query = BSBMQuery8.select_query;
		} else if (query_class == MyValues.QueryClass.TEN) {
			whole_query = BSBMQuery10.select;
		}

		if(using_named_graph)
			query = named_query;
		else
			query = whole_query;

		// let us isolate the parameters that build up this query, depending on the
		// class - the construction is equal if we have named or non-named queries
		if (query_class == MyValues.QueryClass.ONE) {
			String param1 = values[0];
			String param2 = values[1];
			String param3 = values[2];
			String param4 = BSBMQuery1.value_query_1; 
			query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">", param4);
		} else if (query_class == MyValues.QueryClass.TWO) {
			String param1 = values[0];
			query = String.format(query, 
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">");
		} else if (query_class == MyValues.QueryClass.FIVE) {
			String param1 = values[0];
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">");
		} else if (query_class == MyValues.QueryClass.SIX) {
			String param1 = values[0];
			query = String.format(query,
					param1 );
		} else if (query_class == MyValues.QueryClass.SEVEN) {
			String param1 = values[0];
			String param2 = values[1];
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", "<" + param2 + ">",
					"<" + param1 + ">");
		} else if (query_class == MyValues.QueryClass.EIGHT) {
			String param1 = values[0];
			query = String.format(query, "<" + param1 + ">");
		} else if (query_class == MyValues.QueryClass.TEN) {
			String param1 = values[0];
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">");
		}

		return query;
	}

	protected ReturnBox queryTheTripleStore(MyValues.QueryClass query_class, boolean using_named_graph, String[] values) {
		// prepare the SPARQL query
		// three queries: the first query we execute, the query to the named graph, the query to the whole database
		String query = this.decideTheQuery(query_class, using_named_graph, values);


		ReturnBox box = new ReturnBox();

		// prepare to execute the query and take the time
		long query_start_time = System.nanoTime();
		TupleQuery tupleQuery = TripleStoreHandler.getRepositoryConnection().prepareTupleQuery(query);
		TupleQueryResult result = tupleQuery.evaluate();
		if(result.hasNext()) {// potentially time consuming operation
			result.next();
			box.foundSomething = true;
		} else {
			box.foundSomething = false;
		}

		result.close();

		long required_time = System.nanoTime() - query_start_time;
		box.nanoTime = required_time;
		return box;

	}



	protected MyValues.QueryClass assignClass(String c) {
		if(c.equals("ONE"))
			return MyValues.QueryClass.ONE;
		else if(c.equals("FIVE"))
			return MyValues.QueryClass.FIVE;
		else if(c.equals("SEVEN"))
			return MyValues.QueryClass.SEVEN;
		else if(c.equals("EIGHT"))
			return MyValues.QueryClass.EIGHT;
		else if(c.equals("TEN"))
			return MyValues.QueryClass.TEN;

		return null;
	}


	/** This method reports the operations necessary to operate each time one epoch has passed.
	 * That is, this method is updating the cache or the named graph
	 * */
	private ReturnBox oneYearHasPassed(MyValues.CoolDownStrategy strategy) {
		// update the relational database with the credit from the hits
		this.assignCreditBasedOnNumberOFHits();

		ReturnBox rb = null;
		// nessun update
		if(strategy == MyValues.CoolDownStrategy.NONE) {
			// update della cache
			if(MyValues.areWeInterrogatingTheCache)
				rb = this.cacheHandler.updateCacheUsingThreshold(MyValues.creditThreshold);
			// update del named graph
			if(MyValues.areWeInterrogatingTheWholeNamedTripleStore)
				try {
					rb = this.updateNamedGraphUsingThreshold(MyValues.creditThreshold, MyValues.namedGraph);
				} catch (RepositoryException | MalformedQueryException | UpdateExecutionException | SQLException e) {
					e.printStackTrace();
				}
		} else if (strategy == MyValues.CoolDownStrategy.TIME) {
			// I decided to do a new class for this eventuality, to keep the code cleaner
		} else if (strategy == MyValues.CoolDownStrategy.FUNCTION) {
			// as above - TODO devo ancora farla però!!!
		}

		return rb;
	}

	/** Given the threshold to decide which are the triples to add and the name of the graph,
	 * it updates the triples in  the main database, moving them in the named graph.
	 * <p>
	 * In case you have some doubt, the query that performs the update always first delete a triple, and then adds it again
	 * in the graph within a named graph. The delete query does not specify the named graph, thus it is executed on the whole database.  
	 * */
	private ReturnBox updateNamedGraphUsingThreshold(int threshold, String nameOfTheGraph) throws RepositoryException, MalformedQueryException, UpdateExecutionException, SQLException {
		ReturnBox box = new ReturnBox();

		long start = System.nanoTime();

		// asks to the relational db all the triples with a certain quantity of credit
		String qu = String.format(this.SELECT_CREDITED_TRIPLES, RDB.schema);
		PreparedStatement stmt;
		ResultSet r = null;
		stmt = ConnectionHandler.getConnection().prepareStatement(qu);
		stmt.setDouble(1, threshold);
		r = stmt.executeQuery();

		int counter = 0;


		while (r.next()) { // for each triple that needs to stay in the named graph
			// get the values from the RDB. Prepare a new triple so we can rename it in the triple store
			String sub = r.getString(1);
			String pred = r.getString(2);
			String obj = r.getString(3);

			obj = TripleStoreHandler.prepareObjectStringForQuery(obj);

			// format the query
			String updateQuery = String.format(this.SPARQL_UPDATE, sub, pred, obj, nameOfTheGraph, sub, pred, obj);

			// perform the update
			Update q = TripleStoreHandler.getRepositoryConnection().prepareUpdate(QueryLanguage.SPARQL, updateQuery);
			q.execute();
			counter++;
			if(counter % 100 == 0) {
				TripleStoreHandler.getRepositoryConnection().commit();
			}
		}
		TripleStoreHandler.getRepositoryConnection().commit();

		long elapsed = System.nanoTime() - start;
		box.nanoTime = elapsed;
		return box;
	}


	/** Given a list of queries, updates the hits on a support RDB with the 
	 * hits to the lineages of these queries
	 * <p>
	 * As of now, this method sometimes requires a lot of time, in particular
	 * when we have SPARQL queries that encompass a very big lineage. 
	 * Apart from some minor improvements for efficiency, that I a currently too lazy to do but may be nice in the future, 
	 * I do not think there is a lot that can be done here.
	 * 
	 * 
	 * @return the required time to assign hits with this query */
	protected long assignHitsWithOneQuery(MyValues.QueryClass query_class, List<String[]> valuesList, int queryNum) throws SQLException {

		long startTime = System.nanoTime();

		// FIRST: perform a CONSTRUCT to get the lineage
		String query = this.buildConstructQuery(query_class, valuesList, queryNum);

		// here we prepare two jdbc statements, one to insert a new triple and one to update an already present triple in the support RDB
		String q = String.format(this.updateHits, RDB.schema);// set the right schema to the SQL query
		PreparedStatement update_stmt = ConnectionHandler.getConnection().prepareStatement(q);

		String insert_q = String.format(this.INSERT_TRIPLE, RDB.schema);
		PreparedStatement insert_stmt = ConnectionHandler.getConnection().prepareStatement(insert_q);

		// get the LINEAGE
		GraphQuery graphQuery = TripleStoreHandler.getRepositoryConnection().prepareGraphQuery(query);

		try (GraphQueryResult result = graphQuery.evaluate()) {
			//			System.out.println("LINEAGE computed");//XXX
			for (Statement st: result) { // for each triple of the "LINEAGE" (we still aren't sure that each triple is actually in the lineage)

				if(MyValues.constructCheck) { // need to perform a check on the triples 
					boolean presence = this.checkTriplePresenceInTriplestore(st);
					if(!presence) // the triple is not present in the triplestore, thus there is no need to deal with it
						continue;
				}

				// check if that triple is already present in the RDB, and decide if we need to insert or update it 
				if(this.checkTriplePresence(st)) { // TODO si potrebbe migliorare questo metodo aggiungendo mappe in RAM per rendere il tutto più veloce, soprattutto nell'inserimento
					// the triple is already present in the RDB
					//					this.dealWithAlreadySeenTriple(st);

					update_stmt.setInt(1, 1);
					update_stmt.setString(2, st.getSubject().stringValue());
					update_stmt.setString(3, st.getPredicate().stringValue());
					update_stmt.setString(4, st.getObject().toString());

					update_stmt.addBatch();
				} else {
					//					this.dealWithNewTriple(st);

					// need to insert the triple
					insert_stmt.setString(1, st.getSubject().stringValue());
					insert_stmt.setString(2, st.getPredicate().stringValue());
					insert_stmt.setString(3, st.getObject().toString());

					insert_stmt.executeUpdate();
					ConnectionHandler.getConnection().commit(); // need to do the commit so I'll find the triple later
				}
			} // covered all the lineage

			//			System.out.println("inserted all triples");//XXX

			// update triples already present
			update_stmt.executeBatch();

			//			this.insertNewTriples(); // to add for efficiency
			// this.updateAlreadySeenTriples(); // to add for efficiency
			ConnectionHandler.getConnection().commit();
			//			System.out.println("updated relational DB with hits");//XXX
		}
		//stop timer
		long totalTime = System.nanoTime() - startTime;
		return totalTime;

	}

	/** A method that is used to add hits to the support relational database. 
	 * This method uses a support in-memory HashMap, so to avoid to repeadly ask to the triplestore
	 * for the lineage of a query, an operation that may cost several seconds depending on its size. 
	 * @throws SQLException 
	 * */
	protected long assignHitsWithOneQueryMoreEfficiently(MyValues.QueryClass query_class, List<String[]> valuesList, int queryNum) throws SQLException  {
		long startTime = System.nanoTime();

		// FIRST: pdefine the construct query
		String query = this.buildConstructQuery(query_class, valuesList, queryNum);

		// here we prepare two jdbc statements, one to insert a new triple and one to update an already present triple in the support RDB
		String q = String.format(this.updateHits, RDB.schema);// set the right schema to the SQL query
		PreparedStatement update_stmt = ConnectionHandler.getConnection().prepareStatement(q);

		String insert_q = String.format(this.INSERT_TRIPLE, RDB.schema);
		PreparedStatement insert_stmt = ConnectionHandler.getConnection().prepareStatement(insert_q);

		List<String[]> lineage = this.getTheLineageOfThisQuery(query);

		for(String[] triple: lineage) {
			// for each triple in the lineage
			if(this.checkTriplePresence(triple)) { // if it is already in the RDB
				update_stmt.setInt(1, 1);
				update_stmt.setString(2, triple[0]);
				update_stmt.setString(3, triple[1]);
				update_stmt.setString(4, triple[2]);

				update_stmt.addBatch();
			} else {
				// we need to insert it into the RDB
				insert_stmt.setString(1, triple[0]);
				insert_stmt.setString(2, triple[1]);
				insert_stmt.setString(3, triple[2]);

				insert_stmt.executeUpdate();
				ConnectionHandler.getConnection().commit();
			}
		} // covered the lineage
		update_stmt.executeBatch();
		ConnectionHandler.getConnection().commit();
		
		//stop timer
				long totalTime = System.nanoTime() - startTime;
				return totalTime;

	}

	protected boolean checkTriplePresence(String[] triple) throws SQLException {
		String subject = triple[0];
		String predicate = triple[1];
		String object = triple[2];

		// this is really the first time we see this triple, thus it is necessary to ask to the relational DB
		String check_query = String.format(this.CHECK_TRIPLE_PRESENCE, RDB.schema);
		PreparedStatement check_stmt = ConnectionHandler.getConnection().prepareStatement(check_query);
		check_stmt.setString(1, subject);
		check_stmt.setString(2, predicate);
		check_stmt.setString(3, object);

		ResultSet check_rs = check_stmt.executeQuery();
		if(check_rs.next())
			return true;
		else
			return false;
	}

	/** tihis method first looks in RAM to see if we already computed the lineage of this query.
	 * If not, it computes it and inserts it in an HASH MAP. Then it returns the lineage. 
	 * <br>
	 * This method, if left unchecked, may become inefficient when we have a huge number of different
	 * queries with big lineages. Therefore, one optimization in this method
	 * could be to implement strategies to deal with the lineages that are kept in memory. 
	 * That is, this, in itself, is a form of caching used to help another form of caching. 
	 * */
	protected List<String[]> getTheLineageOfThisQuery(String query) {
		String queryHash = "";
		// convert the long query into a more manageable hash string
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
			mDigest.update(query.getBytes());
			queryHash = new String(mDigest.digest());

		} catch (NoSuchAlgorithmException e) {
			System.err.println("No such algorithm as SHA-256 here");
			e.printStackTrace();
		}

		// now check if the query was already answered in the past
		List<String[]> lineage = this.lineageMap.get(queryHash);

		if(lineage != null)
			return lineage;

		//else, it is a new query, and we need to compute it
		lineage = new ArrayList<String[]>();
		// get the LINEAGE
		GraphQuery graphQuery = TripleStoreHandler.getRepositoryConnection().prepareGraphQuery(query);

		try (GraphQueryResult result = graphQuery.evaluate()) {
			for (Statement st: result) {
				if(MyValues.constructCheck) { // need to perform a check on the triples 
					boolean presence = this.checkTriplePresenceInTriplestore(st);
					if(!presence) // the triple is not present in the triplestore, thus there is no need to deal with it
						continue;
				}

				String[] lin = new String[] {st.getSubject().stringValue(), st.getPredicate().stringValue(), st.getObject().toString()};
				lineage.add(lin);
			}
		}
		return lineage;
	}

	/** Method used to build the construct query used to compute the Lineage */
	private String buildConstructQuery(MyValues.QueryClass query_class, List<String[]> valuesList, int queryNum) {
		String query = "";
		// deal with the parameters depending on the class, prepare the construct query
		if(query_class == MyValues.QueryClass.ONE) {
			String param1 = valuesList.get(queryNum)[0];
			String param2 = valuesList.get(queryNum)[1];
			String param3 = valuesList.get(queryNum)[2];
			String param4 = BSBMQuery1.value_query_1;

			query = BSBMQuery1.construct;
			query = String.format(query, "<" + param1 + ">", "<" + param2 + ">", "<" + param3 + ">",
					"<" + param1 + ">", "<" + param2+ ">", "<" + param3 + ">", param4);
		} else if (query_class == MyValues.QueryClass.TWO) {
			String param1 = valuesList.get(queryNum)[0];
			query = BSBMQuery2.construct;
			query = String.format(query,
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", 
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", 
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", 
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">", "<" + param1 + ">");
		} else if (query_class == MyValues.QueryClass.FIVE) {
			String param1 = valuesList.get(queryNum)[0];
			query = BSBMQuery5.construct;
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", 
					"<" + param1 + ">", "<" + param1 + ">",
					"<" + param1 + ">", "<" + param1 + ">", "<" + param1 + ">");
		} else if (query_class == MyValues.QueryClass.SIX) {
			String param1 = valuesList.get(queryNum)[0];
			query = BSBMQuery6.construct;
			query = String.format(query, 
					param1 , param1 );
		} else if (query_class == MyValues.QueryClass.SEVEN) {
			String param1 = valuesList.get(queryNum)[0];
			String param2 = valuesList.get(queryNum)[1];
			query = BSBMQuery7.construct;
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">", 
					"<" + param2 + ">", "<" + param1 + ">", 
					"<" + param1 + ">", "<" + param1 + ">", 
					"<" + param2 + ">", "<" + param1 + ">");
		} else if (query_class == MyValues.QueryClass.EIGHT) {
			String param1 = valuesList.get(queryNum)[0];
			query = BSBMQuery8.parametrixed_construct_query;
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">"); 
		} else if (query_class == MyValues.QueryClass.TEN) {
			String param1 = valuesList.get(queryNum)[0];
			query = BSBMQuery10.construct;
			query = String.format(query, "<" + param1 + ">", "<" + param1 + ">");
		}

		return query;
	}

	private void dealWithNewTriple(Statement st) {
		// build the key
		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().toString();
		String key = subject+ "," + predicate + "," + object;

		// check if this is REALLY the first time you have never seen a triple before
		Integer hits = this.triplesToInsert.get(key);
		if(hits != null) {
			// this is not really the first time we have seen this triple
			hits ++;
			// update the number of hits with which this triple will enter the DB
			this.triplesToInsert.put(key, hits);
		} else {
			// else, this is really the first time, add the triple for the first time
			this.triplesToInsert.put(key, 1);			
		}

	}

	private void dealWithAlreadySeenTriple(Statement st) {
		// build the key
		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().toString();
		String key = subject+ "," + predicate + "," + object;

		Integer hits = this.triplesToUpdate.get(key);
		if(hits != null) {
			// update
			hits++;
			this.triplesToUpdate.put(key, hits);
		} else {
			// this is the first time we see it locally - this should be impossible, since before it should have passed from triplesToInsert
			this.triplesToUpdate.put(key, 1);
		}


	}

	private void insertNewTriples() throws SQLException {
		PreparedStatement st = ConnectionHandler.getConnection().prepareStatement(INSERT_TRIPLE_WITH_HITS);

		for(Entry<String, Integer> entry : this.triplesToInsert.entrySet()) {
			String[] values = entry.getKey().split(",");
			String subject = values[0];
			String predicate = values[1];
			String object = values[2];

			st.setString(1, subject);
			st.setString(2, predicate);
			st.setString(3, object);

			st.setInt(4, entry.getValue());

			st.addBatch();

			// this triple becomes seen from unseen
			this.triplesToUpdate.put(entry.getKey(), 0);
		}
		st.executeBatch();
		ConnectionHandler.getConnection().commit();
	}

	private void updateAlreadySeenTriples() throws SQLException {
		PreparedStatement st = ConnectionHandler.getConnection().prepareStatement(INSERT_TRIPLE_WITH_HITS);

		for(Entry<String, Integer> entry : this.triplesToUpdate.entrySet()) {
			String[] values = entry.getKey().split(",");

			st.setInt(1, entry.getValue());

			st.setString(2, values[0]);
			st.setString(3, values[1]);
			st.setString(4, values[2]);

			st.addBatch();
			this.triplesToUpdate.put(entry.getKey(), 0); // "unload" the hits
		}

		st.executeBatch();
		ConnectionHandler.getConnection().commit();
	}

	/** Check if a triple is actually present in the triplestore, 
	 * or if it was obtained from the CONSTRUCT query */
	private boolean checkTriplePresenceInTriplestore(Statement st) {
		// builds the triple
		String subject = st.getSubject().toString();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().toString();

		// first, check in local RAM if the triple is present
		String key = subject + predicate + object;
		Boolean presence = this.presenceOfTriplesMap.get(key);

		if(presence == null) { // not in RAM, need to ask to the triplestore
			//prepare and execute the ASK query
			String query = String.format(this.SPARQL_ASK, subject, predicate, TripleStoreHandler.prepareObjectStringForQuery(object));
			try {
				BooleanQuery q = TripleStoreHandler.getRepositoryConnection().prepareBooleanQuery(query);
				boolean result = q.evaluate();// it can be tre (present) or false (absent)
				this.presenceOfTriplesMap.put(key, result); // we store the answer so next time we'll be faster
				return result;							
			} catch(MalformedQueryException e) {
				System.err.println(query);
				e.printStackTrace();
				System.exit(0);
			}
		}

		return presence;
	}
	// TODO-issimo a questo punto serve capire come si gestisce l'inserzione in cache/nell/RDB e nel named graph
	// dei nostri elementi, serve non sbagliate tra toString ed altre cose



	/** Checks if a triple is already present in the relational DB. 
	 * 
	 */
	private boolean checkTriplePresence(Statement st) throws SQLException {
		// the statement comes from a CONSTRUCT query
		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().toString();

		// check if already present

		/*
		// --------------------- added for efficiency -------------------------
		String key = subject+ "," + predicate + "," + object;
		// ask to the map containing the triples already found in the relational DB
		Integer v = this.triplesToUpdate.get(key);
		if(v != null) {
			return true; // the triple is already in the RDB
		}

		// if the answer if false, or there is no answer, maybe is a new triple
		v = this.triplesToInsert.get(key);
		if(v != null) {
			// the triple is not in the relational DB yet, but we have already seen it in this iteration, and we have it in this cache of ours
			return false;
		}
		// -------------------------------------------------------------------
		 * 
		 */

		// this is really the first time we see this triple, thus it is necessary to ask to the relational DB
		String check_query = String.format(this.CHECK_TRIPLE_PRESENCE, RDB.schema);
		PreparedStatement check_stmt = ConnectionHandler.getConnection().prepareStatement(check_query);
		check_stmt.setString(1, subject);
		check_stmt.setString(2, predicate);
		check_stmt.setString(3, object);

		ResultSet check_rs = check_stmt.executeQuery();
		if(check_rs.next())
			return true;
		else
			return false;


	}


	/** Deals with different cases and returns the right path where a csv file contains the values
	 * obtain through the ProduceValuesToPerformQueries class */
	@Deprecated
	private String chooseValuesPath(MyValues.QueryClass query_class, String b) {
		String baseDir = b;

		if(query_class == MyValues.QueryClass.ONE) {
			baseDir += "1/query-result.csv";//TODO serve controllare che tutti questi file siano stati correttamente aggiornati con la classe ProduceValuesToPerformQueries
		} else if(query_class == MyValues.QueryClass.FIVE) {
			baseDir += "5/query-result.csv";
		} else if(query_class == MyValues.QueryClass.SEVEN) {
			baseDir += "7/query-result.csv";
		} else if(query_class == MyValues.QueryClass.EIGHT) {
			baseDir += "8/query-result.csv";
		} else if(query_class == MyValues.QueryClass.TEN) {
			baseDir += "10/query-result.csv";
		} // TODO altre classi di query?


		return b;
	}


	@Deprecated
	private ArrayList<String[]> buildValuesForQueriesArray(MyValues.QueryClass class_, BufferedReader valuesReader) throws IOException {

		ArrayList<String[]> valuesList = new ArrayList<String[]>(); // list where we put the values to build the query
		String valuesLine = "";

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

		// I want to shuffle this list a little bit thus to ensure a sort or randomization 
		//I also do not want values that may share sub-graphs too close to each other
		Collections.shuffle(valuesList);

		return valuesList;
	}

	/** After the distribution of hits, use them to update the credit in each tuple by using */
	protected void assignCreditBasedOnNumberOFHits() {
		Connection cc = null;
		try {
			cc = ConnectionHandler.createConnection(RDB.produceJdbcString());
			cc.setAutoCommit(false);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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

	public void close() {
		try {
			ConnectionHandler.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.cacheHandler.close();
	}

	/** truncates the triplestore table in the relational db. Needed each time we re-start a query execution plan.
	 * */
	public void truncateRDBTriplestore() {
		// truncate the whole damn table, so we can fresh-start our experiments
		String sql = "TRUNCATE TABLE %s.triplestore";
		sql = String.format(sql, RDB.schema);
		try {
			ConnectionHandler.getConnection().prepareStatement(sql).execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// all ok
		System.out.println("truncated the triplestore");
	}





	public static void main(String[] args) throws SQLException {
		Experiment1 execution = new Experiment1();

		execution.truncateRDBTriplestore();

		execution.executeTheQueryPlan();

		execution.close();

	}

}
