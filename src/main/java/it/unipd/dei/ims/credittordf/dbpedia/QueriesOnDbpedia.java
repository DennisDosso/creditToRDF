package it.unipd.dei.ims.credittordf.dbpedia;

import experiment1.Experiment1;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.ReturnBox;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class QueriesOnDbpedia extends Experiment1 {

    private int cacheHit = 0, cacheMiss = 0;
    /** To save time required to update the cache*/
    private List<Long> updateCacheTimes;
    /** To save time required to update the RDB*/
    private List<Long> updateRDBTimes;

    private List<String> wholeDbTimes;
    private List<Long> namedDbTimes;
    private List<Long> cacheTimes;

    /** to count how many queries we already did*/
    private int queryCounter;

    public int unansweredSelectQueries;

    public String getSelectQuery() {
        return selectQuery;
    }

    public String selectQuery = "", constructQuery = "";



    protected class RunQueryOnWholeDBThread implements Callable<ReturnBox> {

        private QueriesOnDbpedia instance;

        /** The thread takes the instance of the class that called it, in this way I can pass
         * information to the thread itself
         * */
        public RunQueryOnWholeDBThread(QueriesOnDbpedia i) {
            this.instance = i;
        }

        @Override
        public ReturnBox call() throws Exception {
            // perform the query
            ReturnBox box = new ReturnBox();
            // prepare to execute the query and take the time
            long query_start_time = System.nanoTime();
            int resultSetSize = 0;
            // EXECUTE!
            try{
                TupleQuery tupleQuery = TripleStoreHandler.getRepositoryConnection().prepareTupleQuery(this.instance.selectQuery);
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    if (result.hasNext()) {// potentially time consuming operation
                        box.foundSomething = true;
                        while (result.hasNext()) {
                            result.next();
                            // we count the result set size
                            resultSetSize++;
                        }
                    } else {
                        box.foundSomething = false;
                    }
                }
            } catch (org.eclipse.rdf4j.query.MalformedQueryException e) {

            }

            box.resultSetSize = 0;
            box.resultSetSize = resultSetSize;
            long required_time = System.nanoTime() - query_start_time;
            box.nanoTime = required_time;
            return box;
        }
    }



    /**
     * Sets up the values.properties, rdb.properties, paths.properties and the connection to the RDB
     *
     * @throws SQLException when there are problems
     */
    public QueriesOnDbpedia() throws SQLException {

        super();

        updateCacheTimes = new ArrayList<Long>();
        updateRDBTimes = new ArrayList<Long>();
        wholeDbTimes = new ArrayList<String>();
        namedDbTimes = new ArrayList<Long>();
        cacheTimes = new ArrayList<Long>();

        queryCounter = 0;
    }

    /** It executes the entirety of the select queries on the whole DBpedia database
     *
     * */
    public void executeQueriesOnWholeDb() {
        // first, we need the SELECT and CONSTRUCT queries
        Path selectIn = Paths.get(MyPaths.query_select_file);
        int queryCounter = 0;

        // read the queries from these files
        try(BufferedReader reader = Files.newBufferedReader(selectIn)) {
            // prepare writer where to write the results
            Path resultsPath = Paths.get(MyPaths.wholeDbTimes);
            BufferedWriter writer = Files.newBufferedWriter(resultsPath);

            String selectLine = "", selectQuery = "";
            String queryNo = "";

            while((selectLine = reader.readLine()) != null) {
                if(selectLine.startsWith("#")) {
                    // found number of query, read it and go query line
                    queryNo = selectLine.split(" ")[2];
                    continue;
                }

                this.selectQuery = selectLine;

                // run query on the whole database
                this.runQueryOnWholeDB(selectQuery, queryNo, writer);
                if(queryCounter % 1000 == 0) {
                    writer.flush();
                    System.out.println("done " + queryCounter + " queries");
                }
                queryCounter++;
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // print the results
        // todo controlla che questo funzioni correttamente
//        this.printOneArrayOfTimes(wholeDbTimes, "whole");
    }


    /** Takes care to perform one select query to the whole database.
     * It has a timeout defined in values.properties/select_query_timeout
     *
     * */
    private void runQueryOnWholeDB(String selectQuery, String queryNo, BufferedWriter writer) {
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
            writer.write(queryNo + "," + result.nanoTime + "," + result.resultSetSize);
            writer.newLine();
            wholeDbTimes.add(queryNo + "," + result.nanoTime);
        } catch (InterruptedException e) {
            System.err.println("error with query number " + queryNo);
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.err.println("error with query number " + queryNo);
            try {
            writer.write(queryNo + "," + "error");
            writer.newLine();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (TimeoutException e) {
            //we ran out of time - stop the thread and write that the query went to timeout
            future.cancel(true);
            try {
                writer.write(queryNo + "," + "timeout");
                writer.newLine();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            unansweredSelectQueries++;
        } catch (IOException e) {
            System.err.println("Unable to write results");
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        QueriesOnDbpedia execution = null;
        try {
            execution = new QueriesOnDbpedia();
            execution.truncateRDBTriplestore();
            execution.executeQueriesOnWholeDb();
            execution.close();

            System.out.println("done");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
