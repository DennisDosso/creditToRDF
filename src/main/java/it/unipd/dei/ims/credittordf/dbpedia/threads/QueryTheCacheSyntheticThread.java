package it.unipd.dei.ims.credittordf.dbpedia.threads;

import it.unipd.dei.ims.credittordf.dbpedia.QueriesOnDbpedia;
import it.unipd.dei.ims.credittordf.dbpedia.QueriesOnDbpediaWithCache;
import it.unipd.dei.ims.credittordf.synthetic.cachewithcap.QueryWithCacheAndCap;
import it.unipd.dei.ims.data.ReturnBox;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public class QueryTheCacheSyntheticThread implements Callable<ReturnBox>  {

    private QueryWithCacheAndCap instance;
    private String selectQuery;

    public QueryTheCacheSyntheticThread(QueryWithCacheAndCap i, String sQ) {
        this.instance = i;
        this.selectQuery = sQ;
    }

    @Override
    public ReturnBox call() throws Exception {
        ReturnBox box = new ReturnBox();
        // prepare to execute the query and take the time
        long query_start_time = System.nanoTime();
        int resultSetSize = 0;

        // check if we already know that the query can be answered
        MessageDigest mDigest;
        String queryHash = null;

        try {
            mDigest = MessageDigest.getInstance("SHA-256");
            mDigest.update(selectQuery.getBytes());
            queryHash = new String(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(this.instance.queryCanBeAnsweredMap.get(queryHash) != null &&
                !this.instance.queryCanBeAnsweredMap.get(queryHash)) {
            // the query cannot be answered
            long time = System.nanoTime() - query_start_time;
            box.foundSomething = true; // in any case, this is a hit
            box.nanoTime = time;
            return box;
        }

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
            System.err.println("An error when submitting this select query to the cache: " + this.selectQuery.substring(0, 10) + "...");
            box.foundSomething = false;
        }

        box.resultSetSize = 0;
        box.resultSetSize = resultSetSize;
        long required_time = System.nanoTime() - query_start_time;
        box.nanoTime = required_time;

        return box;
    }
}
