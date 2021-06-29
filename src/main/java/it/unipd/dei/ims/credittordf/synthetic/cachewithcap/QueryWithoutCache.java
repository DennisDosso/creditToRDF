package it.unipd.dei.ims.credittordf.synthetic.cachewithcap;

import experiment1.Experiment1;
import it.unipd.dei.ims.credittordf.dbpedia.threads.RunQueryOnWholeDBSyntheticThread;
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

/** Queries the database to get the size of the result sets of
 * our queries
 *
 * properties to set:
 *
 * (path.properties)
 * <li>query.values.file: where to take the queries</li>
 * <li>whole.db.times</li>
 *
 * */
public class QueryWithoutCache extends Experiment1 {


    /**
     * Sets up the values.properties, rdb.properties, paths.properties and the connection to the RDB
     *
     * @throws SQLException
     */
    public QueryWithoutCache() throws SQLException {
        super();
    }

    public void executeTheQueryPlanWithoutCache() {
        Path p = Paths.get(MyPaths.queryValuesFile);

        Path timeP = Paths.get(MyPaths.wholeDbTimes);

        MyValues.QueryClass query_class = null;

        int queryNo = 0;

        try(BufferedReader reader = Files.newBufferedReader(p)) {
            BufferedWriter writer = Files.newBufferedWriter(timeP);
            writer.write("queryNo,time(ns),result size");
            writer.newLine();

            String line = "";
            while((line = reader.readLine()) != null) { // we read the queries
                // decide which query this is gonna be
                String[] values = line.split(",");
                query_class  = MyValues.convertToQueryClass(values[values.length - 1]);
                String selectQuery = this.decideTheQuery(query_class, false, values);

                this.runQueryOnWholeDB(writer, queryNo, selectQuery);

                if(queryNo % MyValues.epochLength == 0) {
                    System.out.println("Asked " + queryNo + " queries");
                    writer.flush();
                }
                queryNo++;



            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runQueryOnWholeDB(BufferedWriter writer, int queryNo, String selectQuery) {
        ReturnBox box = new ReturnBox();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ReturnBox> future = null;

        future = executor.submit(new RunQueryOnWholeDBSyntheticThread(this, selectQuery));

        try {
            ReturnBox result = future.get(MyValues.select_query_timeout, TimeUnit.MILLISECONDS);
            writer.write(queryNo + "," + result.nanoTime  + "," + result.resultSetSize);
            writer.newLine();
        } catch (InterruptedException e) {
            System.err.println("Interrupted error with query number " + queryNo);
            try {
                writer.write(queryNo + "," + "error,0");
                writer.newLine();
            } catch (IOException ioException) {
                //ioException.printStackTrace();
            }
            //e.printStackTrace();
        } catch (ExecutionException e) {
            System.err.println("Execution exception error with query number " + queryNo);
            try {
                writer.write(queryNo + "," + "error,0");
                writer.newLine();
            } catch (IOException ioException) {
                //ioException.printStackTrace();
            }
        } catch (TimeoutException e) {
            //we ran out of time - stop the thread and write that the query went to timeout
            future.cancel(true);
            try {
                writer.write(queryNo + "," + "timeout,0");
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


    public static void main(String[] args) throws SQLException {
        QueryWithoutCache execution = new QueryWithoutCache();

        execution.truncateRDBTriplestore();

        execution.executeTheQueryPlanWithoutCache();

        execution.close();
        System.out.println("done");
        System.exit(0);
    }
}
