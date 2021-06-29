package it.unipd.dei.ims.credittordf.dbpedia.threads;

import experiment1.Experiment1;
import it.unipd.dei.ims.credittordf.dbpedia.QueriesOnDbpedia;
import it.unipd.dei.ims.credittordf.synthetic.cachewithcap.QueryWithCacheAndCap;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.ReturnBox;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.util.concurrent.Callable;

public class RunQueryOnWholeDBSyntheticThread implements Callable<ReturnBox>  {

    private Experiment1 instance;
    private String selectQuery;


    public RunQueryOnWholeDBSyntheticThread(Experiment1 i, String selectQ) {
        this.instance = i;
        this.selectQuery = selectQ;
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
            TupleQuery tupleQuery = TripleStoreHandler.getRepositoryConnection().prepareTupleQuery(this.selectQuery);
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
