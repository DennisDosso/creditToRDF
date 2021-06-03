package it.unipd.dei.ims.credittordf.dbpedia;

import it.unipd.dei.ims.credittordf.dbpedia.threads.QueryTheCacheThread;
import it.unipd.dei.ims.credittordf.dbpedia.threads.UpdateTheHitCountsThread;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.ReturnBox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.*;

/** First version of code where I simply use the cache. There is no use of a maximum cap to the number
 * of triples, and no cooldown strategy.
 * */
public class QueriesOnDbpediaWithCache extends QueriesOnDbpedia {

    // strings to save our select/construct query, the time of the query, the number of the query
    public String selectQuery = "", constructQuery = "", dataLine = "", queryNo = "";

    public int cacheHit, cacheMiss;

    /**
     * Sets up the values.properties, rdb.properties, paths.properties and the connection to the RDB
     *
     * @throws SQLException when there are problems
     */
    public QueriesOnDbpediaWithCache() throws SQLException {
        super();
        cacheHit = 0;
        cacheMiss = 0;
    }

    public void executeQueriesWithCache() {
        // read files with our queries: select, construct, and output of the query
        Path selectIn = Paths.get(MyPaths.query_select_file);
        Path constructIn = Paths.get(MyPaths.query_construct_file);
        // this is the output of QueriesOnDbpedia.java. Used to know the queries that go to timeout or exception,
        // so we can avoid to lose time with them
        Path queryDataIn = Paths.get(MyPaths.query_data_file);

        // where we write our results
        Path resultsPath = Paths.get(MyPaths.cacheTimes);
        // time to update the relational database
        Path updateRDBPath = Paths.get(MyPaths.overheadTimes);

        Path updateCacheTimes = Paths.get(MyPaths.updateCacheTimes);

        int queryCounter = 0;

        // read one query at the time
        try(BufferedReader selectReader = Files.newBufferedReader(selectIn)) {
            BufferedReader constructReader = Files.newBufferedReader(constructIn);
            BufferedReader queryDataReader = Files.newBufferedReader(queryDataIn);

            // writer to record our results
            BufferedWriter writer = Files.newBufferedWriter(resultsPath);
            writer.write("query#,time (ns),hit/miss,result set size");
            writer.newLine();

            BufferedWriter updateRDBWriter = Files.newBufferedWriter(updateRDBPath);
            updateRDBWriter.write("query#,time,lineage size");
            updateRDBWriter.newLine();

            BufferedWriter updateCacheWriter = Files.newBufferedWriter(updateCacheTimes);
            updateCacheWriter.write("query#,time,cache size");
            updateCacheWriter.newLine();

            while((this.selectQuery = selectReader.readLine()) != null) {
                this.constructQuery = constructReader.readLine();

                if(this.selectQuery.startsWith("#")) {
                    // found number of query, read it and go query line
                    this.queryNo = this.selectQuery.split(" ")[2];
                    continue;
                }

                dataLine = queryDataReader.readLine();

                String[] dataParts = dataLine.split(",");
                String queryResult = dataParts[1];
                if(queryResult.equals("timeout")) {
                    // this query produced a timeout or was badly written => we pass to the next query
                    writer.write(queryNo + ",timeout,miss,0");
                    writer.newLine();
                    continue;
                }
                if(queryResult.equals("error")) {
                    // this query produced a timeout or was badly written => we pass to the next query
                    writer.write(queryNo + ",error,miss,0");
                    writer.newLine();
                    continue;
                }

                // execute this query on the cache
                this.runQueryWithCache(writer);
                // update the number of hits on the support relational database
                this.updateHitCounts(updateRDBWriter);

                // talk to me about your progression and update the cache
                if(queryCounter % MyValues.epochLength == 0 && queryCounter!=0) {
                    System.out.println("done " + queryCounter + " queries, now updating the cache...");

                    this.updateTheCache(updateCacheWriter);
                    writer.flush();
                    updateRDBWriter.flush();
                    updateCacheWriter.flush();
                }
                queryCounter++;
            }
            //close readers
            constructReader.close();
            queryDataReader.close();
            // close writers
            writer.flush(); writer.close();
            updateRDBWriter.flush(); updateRDBWriter.close();
            updateCacheWriter.flush(); updateCacheWriter.close();

        } catch (IOException e) {
            System.err.println("unable to read file");
            e.printStackTrace();
        }
    }

    private void updateTheCache(@org.jetbrains.annotations.NotNull BufferedWriter writer) {
        this.assignCreditBasedOnNumberOFHits();
        ReturnBox rb = this.cacheHandler.updateCacheUsingThreshold(MyValues.creditThreshold);
        // print the time necessary to update the cache and its size
        try {
            writer.write(queryNo + "," + rb.nanoTime + "," + rb.size);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error printing the time and size of the cache");
//            e.printStackTrace();
        }
    }

    /** Asking the cache to answer my query. If we have a cache hit we save the time and the size of the result set.
     * Otherwise we send the request to the whole db*/
    protected void runQueryWithCache(BufferedWriter writer) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        try {
            // ask the cache about this query with a timeout
            future = executor.submit(new QueryTheCacheThread(this, this.selectQuery));
            ReturnBox result = future.get(MyValues.select_query_timeout, TimeUnit.MILLISECONDS);
            if(result.foundSomething) { // cache hit
//                System.out.println("[DEBUG] this query has answered: " + queryNo + "\n" + this.selectQuery
//                + "\ncache Size: ");
                //we have the solution and we write it
                writer.write(queryNo + "," + result.nanoTime + ",hit," + result.resultSetSize);
                writer.newLine();
                cacheHit++;
            } else {
                // if we have a miss, ask the whole database
                this.runQueryOnWholeDB(result.nanoTime, this.queryNo, writer);
                cacheMiss++;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Interrupted or execution exception interrogating the cache with query number " + queryNo + "\n" +
                    "i.e. query " + this.selectQuery.substring(0, 15) + "...");
            try {
                writer.write(queryNo + "," + "error,0");
                writer.newLine();
            } catch (IOException ioException) {
//                ioException.printStackTrace();
            }
            e.printStackTrace();
        } catch (TimeoutException e) {
            // first, stop the thread interrogating the cache
            future.cancel(true);
            try {
                writer.write(queryNo + "," + "timeout");
                writer.newLine();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            this.unansweredSelectQueries++;
        } catch (IOException e) {
            System.err.println("error when writing the result file");
            e.printStackTrace();
        }
    }

    /** USes an external thread with a timeout to update the hit counts on the relational database and
     * write the result.
     * */
    private void updateHitCounts(BufferedWriter writer) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        try {
            // update the strikes number on the RDB
            future = executor.submit(new UpdateTheHitCountsThread(this, this.constructQuery));
            ReturnBox result = future.get(MyValues.construct_query_timeout, TimeUnit.MILLISECONDS);
            writer.write(queryNo + "," + result.nanoTime  + result.size);
            writer.newLine();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Interrupted or Execution exception when computing lineage of query number " + queryNo);
            try {
                writer.write(queryNo + "," + "error,0");
                writer.newLine();
            } catch (IOException ioException) {
//                ioException.printStackTrace();
            }
            if(e instanceof  org.eclipse.rdf4j.query.MalformedQueryException)
            {
                System.out.println("Malformed query");
            }
//            e.printStackTrace(); // no need to write the query
        } catch (TimeoutException e) {
            // first, stop the thread computing the lineage and updating the database
            future.cancel(true);
            try {
                writer.write(queryNo + "," + "timeout,0");
                writer.newLine();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("error with writing the result file for the hits count");
            e.printStackTrace();
        }
    }

    //todo serve contare la size del result set

    /**Method invoked when we have a cache miss
     *
     * @param cacheTime since this method is invoked after a cache miss, we need to summ
     * the time required to ask the cache to the time required to ask to the whole database*/
    private void runQueryOnWholeDB(long cacheTime, String queryNo, BufferedWriter writer) {
        // use a thread to run the query
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // object to get the results of the thread
        Future<ReturnBox> future = null;

        // create an instance of the thread. Send the instance of the whole class to pass data
        // set the timeout for the execution
        try {
            future = executor.submit(new RunQueryOnWholeDBThread(this));
            ReturnBox result = future.get(MyValues.select_query_timeout, TimeUnit.MILLISECONDS);
            //we have the solution and we write it
            writer.write(queryNo + "," + (result.nanoTime + cacheTime) + ",miss," + result.resultSetSize);
            writer.newLine();
        } catch (InterruptedException e) {
            System.err.println("Interrupted error with query number " + queryNo);
//            e.printStackTrace();
        } catch (ExecutionException e) {
            System.err.println("Execution exception error with query number " + queryNo);
            try {
                writer.write(queryNo + "," + "error");
                writer.newLine();
            } catch (IOException ioException) {
//                ioException.printStackTrace();
            }
        } catch (TimeoutException e) {
            //we ran out of time - stop the thread and write that the query went to timeout
            future.cancel(true);
            try {
                writer.write(queryNo + "," + "timeout");
                writer.newLine();
            } catch (IOException ioException) {
                System.out.println("Timeour with query number " + queryNo);
                ioException.printStackTrace();
            }
            unansweredSelectQueries++;
        } catch (IOException e) {
            System.err.println("Unable to write results");
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        QueriesOnDbpediaWithCache execution;
        try {
            execution = new QueriesOnDbpediaWithCache();

            // clean the support DB
            execution.truncateRDBTriplestore();

            // execute the queries
            execution.executeQueriesWithCache();
            execution.close();

            System.out.println("done");


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
