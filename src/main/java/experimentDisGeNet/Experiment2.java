package experimentDisGeNet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.ReturnBox;

/** Here I test a little bit of unbounded queries
 * */
public class Experiment2 extends Experiment1 {

	public Experiment2() throws SQLException {
		super();
	}

	@Override
	public void executeTheQueryPlan() throws SQLException {
		int cacheHit = 0, cacheMiss = 0;

		List<Long> updateCacheTimes = new ArrayList<Long>(); // time to update cache, each epoch
		List<Long> updateNamedGraphTimes = new ArrayList<Long>();// time to update named graph, each epoch

		List<Long> updateRDBTimes = new ArrayList<Long>(); // time to update the support RDB


		List<Long> wholeDbTimes = new ArrayList<Long>();
		List<Long> namedDbTimes = new ArrayList<Long>();
		List<Long> cacheTimes = new ArrayList<Long>();


		// timer to decide when one epoch has passed and it is time to refresh the cache 
		int epochTimer = 0; 
		MyValues.QueryClass query_class = null;


		for(int i = 0; i < 100; ++i) {
			// we issue 100 queries

			// in this special experiment, we update every 5 queries
			if(  epochTimer % 5 == 0 && epochTimer != 0 ) { // new epoch - need to update some things
				// in this special case one epoch has passed or we changed query class

				try {

					ReturnBox box = new ReturnBox();
					if(MyValues.areWeDistributingCredit) {
						box = this.oneYearHasPassed(MyValues.coolDownStrategy);

						System.out.println("one epoch has passed, cache size: " + box.size + 
								" cache hits: " + cacheHit +
								" cache miss: " + cacheMiss);
					}

					// take note of the time required to update cache/named graph
					if(MyValues.areWeInterrogatingTheCache)
						updateCacheTimes.add(box.nanoTime);
					if(MyValues.areWeInterrogatingTheWholeNamedTripleStore)
						updateNamedGraphTimes.add(box.nanoTime);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			
			// decide the query to issue
			UniformIntegerDistribution dis = new UniformIntegerDistribution(1, 12);
			int query_id = dis.sample();
			query_class = this.decideQueryClass(query_id);
			System.out.println("performing now query " + query_class);

			// ----- credit distribution process ----- //
			// distribute the credit  - this may require some time due to the operations on the support RDB and the check for the LINEAGE
			if(MyValues.areWeDistributingCredit) {
				// this line seems strange, it is only here for compatibility with older code, do not bother 
				List<String[]> v = new ArrayList<String[]>();

				// this method has been optimized with a cache to reduce the number of construct queries to the database 
				long overheadTime = this.assignHitsWithOneQueryMoreEfficiently(query_class, v, 0);

				// take note of the time required to distribute the credit
				updateRDBTimes.add(overheadTime);
			}
			
			String[] values = null; // don't bother with this line, it is simply here to be able to use previously written code
			long tripleStoreTime = 0;

			// query the whole database
			if(MyValues.areWeInterrogatingTheWholeTripleStore) {
				ReturnBox box = this.queryTheTripleStore(query_class, false, values);
				wholeDbTimes.add(box.nanoTime);
				tripleStoreTime = box.nanoTime;
			}
			
			// query the named graph
			if(MyValues.areWeInterrogatingTheWholeNamedTripleStore) {
				ReturnBox box = this.queryTheTripleStore(query_class, true, values);
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
				System.out.println("performed the query in " + time);
			}

			// query the cache
			if(MyValues.areWeInterrogatingTheCache) {
				ReturnBox box = this.queryTheCache(query_class, values, this.repoConnection);
				Long time = box.nanoTime;
				if(!box.foundSomething) { 
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
				cacheTimes.add(time);
			}
			
			
			
			epochTimer++; // one line read, proceed
			
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
	
	
	private MyValues.QueryClass decideQueryClass(int query_id) {
		if(query_id == 1) {
			return MyValues.QueryClass.DGN1;
		} else if(query_id == 2) {
			return MyValues.QueryClass.DGN2;
		} else if(query_id == 3) {
			return MyValues.QueryClass.DGN3;
		} else if(query_id == 4) {
			return MyValues.QueryClass.DGN4;
		} else if(query_id == 5) {
			return MyValues.QueryClass.DGN5;
		} else if(query_id == 6) {
			return MyValues.QueryClass.DGN6;
		} else if(query_id == 7) {
			return MyValues.QueryClass.DGN7;
		} else if(query_id == 8) {
			return MyValues.QueryClass.DGN8;
		} else if(query_id == 9) {
			return MyValues.QueryClass.DGN9;
		} else if(query_id == 10) {
			return MyValues.QueryClass.DGN10;
		} else if(query_id == 11) {
			return MyValues.QueryClass.DGN11;
		} else if(query_id == 12) {
			return MyValues.QueryClass.DGN12;
		} 
		
		return MyValues.QueryClass.DGN1; 
	}
	
	public static void main(String[] args) {

		try {
			Experiment2 execution = new Experiment2();
			
			System.out.println("we are truncating...");
			execution.truncateRDBTriplestore();
			
			System.out.println("starting query plan...");
			execution.executeTheQueryPlan();

			execution.close();
			System.out.println("done");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
