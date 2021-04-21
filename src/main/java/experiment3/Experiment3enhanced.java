package experiment3;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import experiment2.Experiment2;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;

/** Here I implement another cool-down strategy, based on the number of hits. 
 * */
public class Experiment3enhanced extends Experiment2 {

	private double previousCacheSize;
	private double originalCoolDown;

	public Experiment3enhanced() throws SQLException {
		super();
		previousCacheSize = 0;
		originalCoolDown = MyValues.coolDownFactor;
	}

	@Override
	public void executeTheQueryPlan() throws SQLException {
		// flag to keep thrack of the type of query we are answering right now
		MyValues.QueryClass query_class = null;

		int cacheHit = 0, cacheMiss = 0;

		// time used to perform the query
		List<Long> cacheTimes = new ArrayList<Long>();
		List<Long> updateCacheTimes = new ArrayList<Long>();
		List<Long> overheadTimes = new ArrayList<Long>();

		List<Long> updateRDBTimes = new ArrayList<Long>(); // time to update the support RDB

		// get the plan file and read it
		Path p = Paths.get(MyPaths.queryValuesFile);

		// timer to decide when one epoch has passed and it is time to refresh the cache 
		int epochTimer = 0; 

		try(BufferedReader reader = Files.newBufferedReader(p)) {
			String line = "";

			while((line = reader.readLine()) != null) {
				// for each query

				// take the values forming the query
				String[] values = line.split(",");
				if(values[0].equals("epoch")) {
					// do nothing, useless line
				} else {

					if(  epochTimer % MyValues.epochLength == 0 && epochTimer != 0 ) {
						// one epoch has passed, update the cache 
						if(MyValues.areWeInterrogatingTheCache) {


							long start = System.nanoTime();

							//update the cache!
							ReturnBox rb = this.oneEpochHasPassed();


							long elapsed = System.nanoTime() - start;
							updateCacheTimes.add(elapsed);

							System.out.println("one epoch has passed, cache size: " + rb.size + 
									" cache hits: " + cacheHit + 
									" cache miss: " + cacheMiss);
							this.previousCacheSize = rb.size;
						}
					}

					// one year has passed, let us cool-down a little here
					if(epochTimer % MyValues.yearLength == 0 && epochTimer != 0) {
						// update the values of credit in the database - this is the cool-down function
						long start = System.nanoTime();
						this.updateCredit();
						long elapsed = System.nanoTime() - start;
						overheadTimes.add(elapsed);

						System.out.println("One year has passed, " + 
								" cache hits: " + cacheHit + 
								" cache miss: " + cacheMiss);
					}

					query_class  = MyValues.convertToQueryClass(values[values.length - 1]);

					// distribute the credit  - this may require some time due to the operations on the support RDB and the check for the LINEAGE
					if(MyValues.areWeDistributingCredit) {
						// these two lineas are a little strange, but they are necessary because of how I built assignHitsWithOneQuery. 
						List<String[]> v = new ArrayList<String[]>();
						v.add(values);

						// this method has been optimized with a cache to reduce the number of construct queries to the database 
						long overheadTime = this.assignHitsWithOneQueryMoreEfficiently(query_class, v, 0);

						// take note of the time required to distribute the credit
						updateRDBTimes.add(overheadTime);
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

		this.printOneArrayOfTimes(updateRDBTimes, "updateRDBTime");// overhead due to maintaining the hits

		this.printOneArrayOfTimes(cacheTimes, "cache");
		this.printOneArrayOfTimes(updateCacheTimes, "update_cache");

		System.out.println("cache hits: " + cacheHit + ", cache miss: " + cacheMiss);

	}

	/** Set the hits to 0 in the relational support database*/
	protected void clearHits() {
		String sql = "UPDATE %s.triplestore set hits = 0;";
		sql = String.format(sql, RDB.schema);
		try {
			ConnectionHandler.createConnection(RDB.produceJdbcString());
			ConnectionHandler.getConnection().prepareStatement(sql).execute();
			ConnectionHandler.getConnection().commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/** In this case, we are also modifying on the fly the cool down factor,
	 * depending on how the cache grows*/
	protected void updateCredit() {
		String averageCredit = "SELECT avg(hits) from %s.triplestore where credit > 0;";
		averageCredit = String.format(averageCredit, RDB.schema);
		try {
			PreparedStatement stmt = ConnectionHandler.getConnection().prepareStatement(averageCredit);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			// get the average, turn it back to an int
			double avg = Math.floor(rs.getDouble(1));

			// everything that is above the average as number of hits is set to average
			String updt = "UPDATE %s.triplestore set hits = ? where hits > ?";
			updt = String.format(updt, RDB.schema);
			PreparedStatement pst = ConnectionHandler.getConnection().prepareStatement(updt);
			pst.setDouble(1, avg);
			pst.setDouble(2, avg);
			pst.execute();
			ConnectionHandler.getConnection().commit();

			// everything else is reduced by 1
			updt = "UPDATE %s.triplestore set hits = hits - ? WHERE hits < ? AND hits > ?";
			updt = String.format(updt, RDB.schema);
			pst = ConnectionHandler.getConnection().prepareStatement(updt);
			pst.setDouble(1, MyValues.coolDownFactor);
			pst.setDouble(2, avg - 0.1);
			double thresholdInHits = Math.exp(MyValues.creditThreshold) - 1;
			//			pst.setDouble(3, thresholdInHits);
			pst.setDouble(3, 0);
			pst.execute();
			ConnectionHandler.getConnection().commit();

			// now get the number of triples in the cache
			String howManyTriples = "SELECT count(hits) from %s.triplestore where hits > ?";
			howManyTriples = String.format(howManyTriples, RDB.schema);
			pst = ConnectionHandler.getConnection().prepareStatement(howManyTriples);
			pst.setDouble(1, MyValues.creditThreshold);
			rs = pst.executeQuery();
			rs.next();
			int hManyTriples = rs.getInt(1); // the number of triples that are now in the cache

			if(hManyTriples >= this.previousCacheSize) {
				// next time we will decrease more the number of hits
				MyValues.coolDownFactor ++;
				System.out.println("Updated the cool down factor to " + MyValues.coolDownFactor);
			} else {
				// restore to the original
				MyValues.coolDownFactor = this.originalCoolDown;
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected ReturnBox oneEpochHasPassed() throws SQLException {
		// first, update the credit based on the number of hits
		String updt = "UPDATE %s.triplestore set credit = ln(1 + hits) where hits >= 0;";
		updt = String.format(updt, RDB.schema);
		ConnectionHandler.getConnection().prepareStatement(updt).executeUpdate();
		ConnectionHandler.getConnection().commit();

		// now update the cache
		ReturnBox rb = null;
		rb = this.cacheHandler.updateCacheUsingThreshold(MyValues.creditThreshold);
		return rb;
	}

	public static void main(String[] args) {
		try {
			Experiment3enhanced execution = new Experiment3enhanced();
			execution.truncateRDBTriplestore();

			execution.executeTheQueryPlan();

			execution.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
