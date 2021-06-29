package it.unipd.dei.ims.credittordf.dbpedia.cachewithcap;

import it.unipd.dei.ims.credittordf.dbpedia.QueriesOnDbpediaWithCache;
import it.unipd.dei.ims.credittordf.dbpedia.threads.UpdateTheHitCountsThread;
import it.unipd.dei.ims.credittordf.dbpedia.threads.UpdateTheHitCountsWithCapThread;
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

public class QueriesOnDbpediaWithCacheAndCap extends QueriesOnDbpediaWithCache {

    private CacheSupport cacheSupport;

    /**
     * Sets up the values.properties, rdb.properties, paths.properties and the connection to the RDB
     *
     * @throws SQLException when there are problems
     */
    public QueriesOnDbpediaWithCacheAndCap() throws SQLException {
        super();
        this.cacheSupport = new CacheSupport(MyValues.cap);
    }


    public void executeQueriesWithCacheAndCap() {
        // paths from where we read queries and other information
        Path selectIn = Paths.get(MyPaths.query_select_file);
        Path constructIn = Paths.get(MyPaths.query_construct_file);
        Path queryDataIn = Paths.get(MyPaths.query_data_file);
        // paths where we write things
        Path resultsPath = Paths.get(MyPaths.cacheTimes);
        Path updateRDBPath = Paths.get(MyPaths.overheadTimes);
        Path updateCacheTimes = Paths.get(MyPaths.updateCacheTimes);

        int queryCounter = 0;

        try(BufferedReader selectReader = Files.newBufferedReader(selectIn)) {
            BufferedReader constructReader = Files.newBufferedReader(constructIn);
            BufferedReader queryDataReader = Files.newBufferedReader(queryDataIn);

            BufferedWriter writer = Files.newBufferedWriter(resultsPath);
            writer.write("query#,time (ns),hit/miss,result set size");
            writer.newLine();

            BufferedWriter updateRDBWriter = Files.newBufferedWriter(updateRDBPath);
            updateRDBWriter.write("query#,time,lineage size");
            updateRDBWriter.newLine();

            BufferedWriter updateCacheWriter = Files.newBufferedWriter(updateCacheTimes);
            updateCacheWriter.write("query#,time,cache size");
            updateCacheWriter.newLine();


            //read query by query
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

                // run query on the cache
                this.runQueryWithCache(writer);

                // update the number of hits through this query
                this.updateHitsCount(updateRDBWriter);

                // talk to me about your progression and update the cache
                if(queryCounter % MyValues.epochLength == 0 && queryCounter!=0) {
                    System.out.println("done " + queryCounter + " queries, now updating the cache...");

                    this.updateTheCache(queryCounter, updateCacheWriter);

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
            System.err.println("Unable to write");
            e.printStackTrace();
        }

    }

    private void updateHitsCount(BufferedWriter writer) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        // update the strikes
        try {
            future = executor.submit(new UpdateTheHitCountsWithCapThread(this, this.constructQuery, this.cacheSupport));
            ReturnBox result = future.get(MyValues.construct_query_timeout, TimeUnit.MILLISECONDS);
            writer.write(queryNo + "," + result.nanoTime  + "," + result.size);
            writer.newLine();
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
    }

    private void updateTheCache(int queryNo, BufferedWriter writer) {
//        ReturnBox rb = this.cacheSupport.buildCache(this.cacheHandler.cacheConnection, MyValues.creditThreshold);
        ReturnBox rb = this.cacheSupport.buildCacheFromQueue(this.cacheHandler.cacheConnection, MyValues.creditThreshold);
        try {
            writer.write(queryNo + "," + rb.nanoTime + "," + rb.size);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        QueriesOnDbpediaWithCacheAndCap execution = null;
        try {
            execution = new QueriesOnDbpediaWithCacheAndCap();

            execution.truncateRDBTriplestore();
            execution.executeQueriesWithCacheAndCap();

            execution.close();
            System.out.println("done");
            System.exit(0);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



}
