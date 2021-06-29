package it.unipd.dei.ims.credittordf.dbpedia.cachewithcapandcooldown;

import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.QueriesOnDbpediaWithCacheAndCap;
import it.unipd.dei.ims.credittordf.dbpedia.threads.UpdateHitsCountOnTimeframesThread;
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

public class QueriesWithCacheAndCapAndCooldown extends QueriesOnDbpediaWithCacheAndCap {


    private TimeframeHandler timeframeHandler;


    public QueriesWithCacheAndCapAndCooldown() throws SQLException {
        super();
        this.timeframeHandler = new TimeframeHandler(MyValues.howManyEpochs, MyValues.cap);
    }


    public void executeQueriesWithCacheAndCapAndCooldown() {
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
                // update hit count for this query
                this.updateHitsCount(updateRDBWriter);

                // when needed, update the cache
                if(queryCounter % MyValues.epochLength == 0 && queryCounter!=0) {
                    System.out.println("done " + queryCounter + " queries, now updating the cache...");

                    this.updateTheCache(queryCounter, updateCacheWriter);

                    writer.flush();
                    updateRDBWriter.flush();
                    updateCacheWriter.flush();
                }
                if(queryCounter % MyValues.yearLength == 0 && queryCounter != 0) {
                    this.timeframeHandler.startNewTimeframe();
                    System.out.println("One timeframe finished, another one started. Number of timeframes: " +
                            this.timeframeHandler.timeframes.size());
                }
                queryCounter++;


            } // end of queries
            //close readers
            constructReader.close();
            queryDataReader.close();
            // close writers
            writer.flush(); writer.close();
            updateRDBWriter.flush(); updateRDBWriter.close();
            updateCacheWriter.flush(); updateCacheWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void updateHitsCount(BufferedWriter writer) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        // update the current timeframe with strikes/ new triples
        try {
            future = executor.submit(new UpdateHitsCountOnTimeframesThread(this, this.constructQuery, this.timeframeHandler));
            ReturnBox result = future.get(MyValues.construct_query_timeout, TimeUnit.MILLISECONDS);
            writer.write(queryNo + "," + result.nanoTime  + "," + result.size);
            writer.newLine();
        } catch (InterruptedException | ExecutionException e) {
            try {
                writer.write(queryNo + "," + "error,0");
                writer.newLine();
            } catch (IOException ioException) { }
        } catch (TimeoutException e) {
            try {
                writer.write(queryNo + "," + "timeout,0");
                writer.newLine();
            } catch (IOException ioException) { }
        } catch (IOException e) {
            // this exception would have already be caught prior to this piece of code
        }
    }

    public void updateTheCache(int queryNo, BufferedWriter writer) {
        ReturnBox rb = this.timeframeHandler.buildCache(this.cacheHandler.cacheConnection, MyValues.creditThreshold);
        try {
            writer.write(queryNo + "," + rb.nanoTime + "," + rb.size);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        QueriesWithCacheAndCapAndCooldown execution = null;
        try {
            execution = new QueriesWithCacheAndCapAndCooldown();

            execution.truncateRDBTriplestore();

            execution.executeQueriesWithCacheAndCapAndCooldown();

            execution.close();
            System.out.println("done");
            System.exit(0);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
