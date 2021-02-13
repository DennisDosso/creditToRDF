package experiment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import experiment1.Experiment1;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;


/** In this second experiment we implement a sort of cooldown function, based on time. 
 * Only the x last epoch of distributed credit are kept in cache.
 * <p>
 * This strategy is impact-factor like. When we compute the impact factor of a paper, we consider 
 * the citations conied in the last two years. In the same way, we consider
 * only the last epochs in the computation of the distributed credit. 
 * <p>
 * We call this strategy TIME-AWARE.
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

		// time used to perform the query
		List<Long> interrogationTimes = new ArrayList<Long>();

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
						// an epoch has passed
						// TODO fai quello che devi fare in base al cambiamento di un'epoca
						this.updateEpochsHandler();
						this.assignCreditToDatabase();
						//update the cache!
					}
				}
				
				//get the class of this query
				query_class = this.assignClass(values[values.length - 1]);
				// update hits on the database
				// distribute the credit  - this may require some time due to the operations on the support RDB and the check for the LINEAGE
				if(MyValues.areWeDistributingCredit) {
					// these two lineas are a little strange, but they are necessary because of how I built assignHitsWithOneQuery. 
					List<String[]> v = new ArrayList<String[]>();
					v.add(values);

					long overheadTime = this.assignHitsWithOneQuery(query_class, v, 0);

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void updateEpochsHandler() {
		this.epochsHandler.update();
	}
	
	/** Using the queries in the timespan, updates the credit in the database
	 * 
	 * @throws SQLException */
	private void assignCreditToDatabase() throws SQLException {
		// first, we need to clean the database, if we want then to update it
		this.cleanDatabase();
		
		
		for(List<String[]> epoch : this.epochsHandler.getEpochs()) {
			// for each epoch
			for(String[] query : epoch) {
				// for each query in the epoch
				MyValues.QueryClass query_class = this.assignClass(query[query.length - 1]);
				List<String[]> v = new ArrayList<String[]>();
				v.add(query);
				this.assignHitsWithOneQuery(query_class, v, 0);
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

	}

}
