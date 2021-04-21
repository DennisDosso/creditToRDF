package experiment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;

import experiment1.Experiment1;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;


/** In this second experiment we implement a sort of cool-down function, based on time. 
 * Only the x last epoch of distributed credit are kept in cache.
 * <p>
 * This strategy is impact-factor like. When we compute the impact factor of a paper, we consider 
 * the citations conied in the last two years. In the same way, we consider
 * only the last epochs in the computation of the distributed credit. 
 * <p>
 * We call this strategy TIME-AWARE.
 * <p>
 * The strategy NON-TIME-AWARE is simply the one of {@link Experiment1}, where you simply interrogate the cache
 * and use the same threshold used here.
 * 
 * <br>
 * <pre>
 * nohup java -cp creditToRdf-1.0.jar:lib/* experiment2/Experiment2NG > 
 * </pre>
 * */

public class Experiment2NG extends Experiment1 {

	private String SELECT_CREDITED_TRIPLES = "select t.subject , t.predicate , t.\"object\" \n" + 
			"from %s.triplestore t \n" + 
			"where credit > ?";

	private EpochsHandler epochsHandler;

	/** a set containing the triples that are currently present in the named graph, 
	 * written inside in a csv format.
	 * */
	private Set<String> namedTriples;


	protected String SPARQL_DELETE_FROM_NAMED_GRAPH = "DELETE WHERE { GRAPH <%s> {<%s> <%s> %s}}; "
			+ "INSERT DATA {"
			+ " { "
			+ "<%s> <%s> %s }}";


	public Experiment2NG() throws SQLException {
		super();
		epochsHandler = new EpochsHandler(MyValues.howManyEpochs);
		namedTriples = new HashSet<String>();
	}


	/**
	 * 
	 * Because executeOrder66 was already taken.
	 * @throws SQLException 
	 * 
	 * */
	@Override
	public void executeTheQueryPlan() throws SQLException {
		// flag to keep thrack of the type of query we are answering right now
		MyValues.QueryClass query_class = null;

		int cacheHit = 0, cacheMiss = 0;

		// time used to perform the query
		List<Long> cacheTimes = new ArrayList<Long>();
		List<Long> dbTimes = new ArrayList<Long>();
		List<Long> overheadTimes = new ArrayList<Long>();

		List<Long> namedDbTimes = new ArrayList<Long>();

		List<Long> updateNamedGraphTimes = new ArrayList<Long>();// time to update named graph, each epoch

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
					// do nothing, useless line
				} else {

					if(epochTimer % MyValues.yearLength == 0 && epochTimer != 0) {
						//start a new epoch
						this.epochsHandler.startNewEpoch();
						// update the set of epochs 
						this.updateEpochsHandler();
						System.out.println("One year has passed, epochs size: " + (epochsHandler.getEpochs().size() ) + 
								" cache hits: " + cacheHit + 
								" cache miss: " + cacheMiss);
					}

					if(  epochTimer % MyValues.epochLength == 0 && epochTimer != 0 ) { 

						// update of the cache - in this case the named graph
						// update the credit in the support database using the epochs
						this.assignCreditToDatabase();

						// update the named graph
						ReturnBox rb =  this.updateNamedGraphUsingThreshold();

						updateNamedGraphTimes.add(rb.nanoTime);

						System.out.println("one epoch has passed, cache size: " + rb.size + 
								" cache hits: " + cacheHit + 
								" cache miss: " + cacheMiss);

					}

					// add the current query to the current epoch
					this.epochsHandler.addQueryToCurrentEpoch(values);

					//get the class of this query
					query_class = MyValues.convertToQueryClass(values[values.length - 1]);

					ReturnBox box = null;
					long tripleStoreTime = 0;
					if(MyValues.areWeInterrogatingTheWholeTripleStore) {
						// query the whole database
						box = this.queryTheTripleStore(query_class, false, values);
						// and save the time
						dbTimes.add(box.nanoTime);
					}

					// query the named graph
					if(MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
						box = this.queryTheTripleStore(query_class, true, values);
						long time = box.nanoTime;
						if(!box.foundSomething) { // cache miss
							if(tripleStoreTime!=0) {
								time += tripleStoreTime;
							} else {
								box = this.queryTheTripleStore(query_class, false, values);
								time += box.nanoTime;
							}
							cacheMiss ++;
						} else {
							// cache hit
							cacheHit ++;
						}
						namedDbTimes.add(time);
					}

					if(MyValues.areWeInterrogatingTheCache) {
						// query the cache and the DB, if necessary
						box = this.queryTheCache(query_class, values, this.repoConnection);


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

					epochTimer++;
				}
			} // end of the queries
			if(MyValues.areWeInterrogatingTheCache) {
				this.printOneArrayOfTimes(cacheTimes, "cache");
				this.printOneArrayOfTimes(overheadTimes, "update_epochs");
			}

			if(MyValues.areWeInterrogatingTheWholeTripleStore)
				this.printOneArrayOfTimes(dbTimes, "whole");

			if(MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
				this.printOneArrayOfTimes(namedDbTimes, "named");
				this.printOneArrayOfTimes(updateNamedGraphTimes, "update_named"); // overhead due to updating the named graph at each epoch
			}



			System.out.println("cache hits: " + cacheHit + ", cache miss: " + cacheMiss);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void updateEpochsHandler() {
		this.epochsHandler.update();
	}

	protected ReturnBox updateNamedGraphUsingThreshold() {
		ReturnBox box = new ReturnBox();
		// take the time required to update the cache
		long start = System.nanoTime();

		Set<String> newTriples = new HashSet<String>();

		// take all the triples that need to be in the named graph
		String qu = String.format(this.SELECT_CREDITED_TRIPLES, RDB.schema);
		PreparedStatement stmt;
		try {
			stmt = ConnectionHandler.getConnection().prepareStatement(qu);
			stmt.setDouble(1, MyValues.creditThreshold);
			ResultSet r = stmt.executeQuery();

			// insert all the useful elements in the new map of necessary triples
			while (r.next()) {
				// get the necessary elements that build the triple
				String sub = r.getString(1);
				String pred = r.getString(2);
				String obj = r.getString(3);
				obj = TripleStoreHandler.prepareObjectStringForQuery(obj);

				newTriples.add(sub + "\t" + pred + "\t" + obj); 
			}

			// get the triples to add to the database
			Set<String> stringsToAdd = Sets.difference(newTriples, this.namedTriples);

			// now, for each triple to add
			for(String toAdd : stringsToAdd) {
				String[] parts = toAdd.split("\t");
				String sub = parts[0];
				String pred = parts[1];
				String obj = parts[2];

				// format the query
				String updateQuery = String.format(this.SPARQL_UPDATE, sub, pred, obj, MyValues.namedGraph, sub, pred, obj);

				// perform the update
				Update q = TripleStoreHandler.getRepositoryConnection().prepareUpdate(QueryLanguage.SPARQL, updateQuery);
				q.execute();
			}

			TripleStoreHandler.getRepositoryConnection().commit();
			
			// get the triples to remove
			Set<String> triplesToDelete = Sets.difference(this.namedTriples, newTriples);

			for(String toAdd : triplesToDelete) {
				String[] parts = toAdd.split("\t");
				String sub = parts[0];
				String pred = parts[1];
				String obj = parts[2];

				// format the query
				String updateQuery = String.format(this.SPARQL_DELETE_FROM_NAMED_GRAPH, MyValues.namedGraph, sub, pred, obj, sub, pred, obj);
				// perform the update
				Update q = TripleStoreHandler.getRepositoryConnection().prepareUpdate(QueryLanguage.SPARQL, updateQuery);
				q.execute();
			}

			TripleStoreHandler.getRepositoryConnection().commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		long elapsed = System.nanoTime() - start;
		box.nanoTime = elapsed;
		box.size = newTriples.size();
		this.namedTriples = newTriples;
		return box;
	}

	/** Using the queries in the timespan, updates the credit in the database.
	 * NB: the epochs should already have been updated
	 * 
	 * @throws SQLException */
	protected void assignCreditToDatabase() throws SQLException {
		// first, we need to clean the database, if we want then to update it
		this.cleanDatabase();

		// use all the queries in the available epochs 
		for(List<String[]> epoch : this.epochsHandler.getEpochs()) {
			// for each epoch
			for(String[] query : epoch) {
				// for each query in the epoch
				MyValues.QueryClass query_class = MyValues.convertToQueryClass(query[query.length - 1]);
				List<String[]> v = new ArrayList<String[]>();
				v.add(query);
				// add the hits to the database based on the query
				//				this.assignHitsWithOneQuery(query_class, v, 0);
				this.assignHitsWithOneQueryMoreEfficiently(query_class, v, 0);
			}
		}
		// now that we have distributed the credit, update the credit in the database
		this.assignCreditBasedOnNumberOFHits();
	}

	/** Used to clean the credit column, putting everything to 0*/
	protected void cleanDatabase() {
		String sql = "UPDATE %s.triplestore SET credit = 0, hits = 0;";
		sql = String.format(sql, RDB.schema);
		try {
			ConnectionHandler.createConnection(RDB.produceJdbcString());
			ConnectionHandler.getConnection().prepareStatement(sql).execute();
			ConnectionHandler.getConnection().commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Experiment2NG execution = new Experiment2NG();
			
			execution.truncateRDBTriplestore();

			execution.executeTheQueryPlan();

			execution.close();
		} catch (SQLException e) {
			e.printStackTrace();
			e.getNextException().printStackTrace();
		} 

	}

}
