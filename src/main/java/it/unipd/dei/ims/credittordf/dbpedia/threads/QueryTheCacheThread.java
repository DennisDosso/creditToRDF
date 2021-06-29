package it.unipd.dei.ims.credittordf.dbpedia.threads;

import it.unipd.dei.ims.credittordf.dbpedia.QueriesOnDbpedia;
import it.unipd.dei.ims.credittordf.dbpedia.QueriesOnDbpediaWithCache;
import it.unipd.dei.ims.data.ReturnBox;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.util.concurrent.Callable;

/** This thread interrogates the cache and returns the necessary time
 * */
public class QueryTheCacheThread implements Callable<ReturnBox> {

    private QueriesOnDbpedia instance;
    private String selectQuery;

    public QueryTheCacheThread(QueriesOnDbpediaWithCache i, String sQ) {
        this.instance = i;
        this.selectQuery = sQ;
    }

    @Override
    public ReturnBox call()  {
        ReturnBox box = new ReturnBox();
        // prepare to execute the query and take the time
        long query_start_time = System.nanoTime();
        int resultSetSize = 0;

        // interrogate the cache - it was already opened in the constructor of the instance
        try{
            TupleQuery tupleQuery = instance.getRepoConnection().prepareTupleQuery(this.selectQuery);
            try(TupleQueryResult result = tupleQuery.evaluate()) {
                // check for presence of a result
                if(result.hasNext()) {
                    box.foundSomething = true;
                    long size = instance.getRepoConnection().size();
                    while(result.hasNext()) {
                        result.next();
                        resultSetSize++;
                    }
                } else {
                    box.foundSomething = false;
                }
            }
        } catch(Exception e1) {
            try{
                // todo maybe see what can be modified here
//                System.err.println("An error when submitting this select query to the cache: " + this.selectQuery.substring(0, 25) + "...");
            } catch (StringIndexOutOfBoundsException e2) {
//                System.err.println("An error when submitting this select query to the cache: " + this.selectQuery);
            }
            box.foundSomething = false;
        }

        box.resultSetSize = 0;
        box.resultSetSize = resultSetSize;
        long required_time = System.nanoTime() - query_start_time;
        box.nanoTime = required_time;

        return box;
    }
}
