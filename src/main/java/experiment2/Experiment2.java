package experiment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.repository.RepositoryConnection;

import experiment1.Experiment1;
import it.unipd.dei.ims.credittordf.utils.CacheHandler;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;


/** In this second experiment we implement a sort of cooldown function, based on time. 
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
 * */

public class Experiment2 extends Experiment1 {

	private EpochsHandler epochsHandler;
	
	
	public Experiment2() throws SQLException {
		super();
		epochsHandler = new EpochsHandler(MyValues.howManyEpochs);
		
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
					if(  epochTimer % MyValues.epochLength == 0 && epochTimer != 0 ) { 
						
						if(MyValues.areWeInterrogatingTheCache) {
							long start = System.nanoTime();
							// an epoch has passed - update the number of epochs that we are keeping track of (one exists, one enters)
							this.updateEpochsHandler();
							
							this.assignCreditToDatabase();
							
							//update the cache!
							ReturnBox rb = this.cacheHandler.updateCacheUsingThreshold(MyValues.creditThreshold);
							
							// also, start a new epoch
							this.epochsHandler.startNewEpoch();
							
							long elapsed = System.nanoTime() - start;
							overheadTimes.add(elapsed);
							
							System.out.println("one epoch has passed, cache size: " + rb.size + 
									" cache hits: " + cacheHit + 
									" cache miss: " + cacheMiss);
						}
					}
					
					// add the query to the epoch handlers
					this.epochsHandler.addQueryToCurrentEpoch(values);
					
					//get the class of this query
					query_class = MyValues.convertToQueryClass(values[values.length - 1]);

					ReturnBox box = null;
					if(MyValues.areWeInterrogatingTheWholeTripleStore) {
						// query the whole database
						box = this.queryTheTripleStore(query_class, false, values);
						// and save the time
						dbTimes.add(box.nanoTime);
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
			
			
			
			System.out.println("cache hits: " + cacheHit + ", cache miss: " + cacheMiss);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void updateEpochsHandler() {
		this.epochsHandler.update();
	}
	
	/** Using the queries in the timespan, updates the credit in the database.
	 * NB: the epochs should already have been updated
	 * 
	 * @throws SQLException */
	private void assignCreditToDatabase() throws SQLException {
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Experiment2 execution = new Experiment2();
			
			execution.executeTheQueryPlan();
			
			execution.close();
		} catch (SQLException e) {
			e.printStackTrace();
			e.getNextException().printStackTrace();
		} 
		
	}

}
