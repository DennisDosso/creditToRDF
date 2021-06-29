package it.unipd.dei.ims.credittordf.synthetic.cachewithcap;

import experiment1.Experiment1;
import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.CacheSupport;
import it.unipd.dei.ims.credittordf.dbpedia.threads.QueryTheCacheSyntheticThread;
import it.unipd.dei.ims.credittordf.dbpedia.threads.RunQueryOnWholeDBSyntheticThread;
import it.unipd.dei.ims.credittordf.dbpedia.threads.UpdateTheHitCountsWithCapSyntheticThread;
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

/** Implements the cache-algorithm with credit distribution
 * also using a maximum cap to the dimension of the cache (budget)
 * */
public class QueryWithCacheAndCap extends Experiment1 {

    /** object used to control the size and update our cache
     * */
    private CacheSupport cacheSupport;


    /**
     * Sets up the values.properties, rdb.properties, paths.properties and the connection to the RDB
     *
     * @throws SQLException
     */
    public QueryWithCacheAndCap() throws SQLException {
        super();
        this.cacheSupport = new CacheSupport(MyValues.cap);
    }


    public void executeTheQueryPlanWithCacheAndCap() {
        // get the plan file and read it
        Path p = Paths.get(MyPaths.queryValuesFile);

        Path overheadP = Paths.get(MyPaths.overheadTimes);
        Path cacheP = Paths.get(MyPaths.cacheTimes);
        Path updateCacheTimes = Paths.get(MyPaths.updateCacheTimes);

        MyValues.QueryClass query_class = null;

        // timer to decide when one epoch has passed and it is time to refresh the cache
        int epochTimer = 0;
        int queryCounter = 0;

        try(BufferedReader reader = Files.newBufferedReader(p)) {

            BufferedWriter overheadWriter = Files.newBufferedWriter(overheadP);
            overheadWriter.write("queryNo,lineage/update time(ns),lineage size");
            overheadWriter.newLine();

            BufferedWriter cacheWriter = Files.newBufferedWriter(cacheP);
            cacheWriter.write("queryNo,time(ns),hit/miss,result size");
            cacheWriter.newLine();

            BufferedWriter updateCacheWriter = Files.newBufferedWriter(updateCacheTimes);
            updateCacheWriter.write("query#,time,cache size");
            updateCacheWriter.newLine();

            String line = "";
            while((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if(values[0].equals("epoch")) {
                    try { //change of query class, do nothing
                        System.out.println("Change of class: " + values[1]);
                    } catch(Exception e) { }
                } else {
                    // decide which query this is gonna be
                    query_class  = MyValues.convertToQueryClass(values[values.length - 1]);

                    // compute the query
                    this.runQueryWithCache(query_class, values, cacheWriter, queryCounter);

                    // distribute credit
                    this.assignHits(query_class, values, overheadWriter, queryCounter);

                    if(  epochTimer % MyValues.epochLength == 0 && epochTimer != 0 ) {
                        // update the cache
                        System.out.println("done " + queryCounter + " queries, now updating the cache...");
                        this.updateThecache(updateCacheWriter, queryCounter);

                        // flush the writers so we will see things
                        cacheWriter.flush(); overheadWriter.flush(); updateCacheWriter.flush();
                    }

                    queryCounter++;
                    epochTimer++;
                }
            }// end of queries

            // close writers
            cacheWriter.flush(); cacheWriter.close();
            overheadWriter.flush(); overheadWriter.close();
            updateCacheWriter.flush(); updateCacheWriter.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateThecache(BufferedWriter writer, int queryNo) {
        ReturnBox r = this.cacheSupport.buildCache(this.cacheHandler.cacheConnection, MyValues.creditThreshold);
        try {
            writer.write(queryNo + "," + r.nanoTime + "," + r.size);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected ReturnBox runQueryWithCache(MyValues.QueryClass query_class, String[] values, BufferedWriter writer, int queryNo) {
        // create the select query
        String selectQuery = this.decideTheQuery(query_class, false, values);
        ReturnBox box = new ReturnBox();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        future = executor.submit(new QueryTheCacheSyntheticThread(this, selectQuery));
        try {
            ReturnBox result = future.get(MyValues.select_query_timeout, TimeUnit.MILLISECONDS);
            if(result.foundSomething) { // cache hit
                //we have the solution and we write it
                writer.write(queryNo + "," + result.nanoTime + ",hit," + result.resultSetSize);
                writer.newLine();
            } else {
                // if we have a miss, ask the whole database
                this.runQueryOnWholeDBMiss(result.nanoTime, queryNo, writer, selectQuery);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            future.cancel(true);
            try {
                writer.write(queryNo + "," + "timeout,0");
                writer.newLine();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return box;
    }

    protected void runQueryOnWholeDBMiss(long cacheTime, int queryNo, BufferedWriter writer, String selectQuery) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        future = executor.submit(new RunQueryOnWholeDBSyntheticThread(this, selectQuery));
        try {
            ReturnBox result = future.get(MyValues.select_query_timeout, TimeUnit.MILLISECONDS);
            //we have the solution and we write it
            writer.write(queryNo + "," + (result.nanoTime + cacheTime) + ",miss," + result.resultSetSize);
            writer.newLine();
        } catch (InterruptedException e) {
            System.err.println("Interrupted error with query number " + queryNo);
            //e.printStackTrace();
        } catch (ExecutionException e) {
            System.err.println("Execution exception error with query number " + queryNo);
            try {
                writer.write(queryNo + "," + "error");
                writer.newLine();
            } catch (IOException ioException) {
                //ioException.printStackTrace();
            }
            //e.printStackTrace();
        } catch (TimeoutException e) {
            //we ran out of time - stop the thread and write that the query went to timeout
            future.cancel(true);
            try {
                writer.write(queryNo + "," + "timeout");
                writer.newLine();
            } catch (IOException ioException) {
                System.out.println("Timeout with query number " + queryNo);
                ioException.printStackTrace();
            }
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /** Computes the lineage of a query and distributes the hits in the in-memory cacheSupport object.
     * It also writes on the support file the time required to update */
    protected ReturnBox assignHits(MyValues.QueryClass query_class, String[] values, BufferedWriter writer, int queryNo) {
        // build the query from the values
        String constructQuery = this.buildConstructQuery(query_class, values);

        // prepare the thread that computes lineage
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        // update the strikes number on the RDB
        try {
            future = executor.submit(new UpdateTheHitCountsWithCapSyntheticThread(this, constructQuery, this.cacheSupport));
            ReturnBox result = future.get(MyValues.construct_query_timeout, TimeUnit.MILLISECONDS);
            writer.write(queryNo + "," + result.nanoTime  + result.size);
            writer.newLine();
            return result;
        } catch (InterruptedException | ExecutionException e) {
            try {
                writer.write(queryNo + "," + "error,0");
                writer.newLine();
            } catch (IOException ioException) { }
//            e.printStackTrace();
        } catch (TimeoutException e) {
            try {
                writer.write(queryNo + "," + "timeout,0");
                writer.newLine();
            } catch (IOException ioException) { }
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return null;
    }



    public static void main(String[] args) throws SQLException {
        QueryWithCacheAndCap execution = new QueryWithCacheAndCap();

        execution.truncateRDBTriplestore();

        execution.executeTheQueryPlanWithCacheAndCap();

        execution.close();
        System.out.println("done");
        System.exit(0);
    }
}
