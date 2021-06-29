package it.unipd.dei.ims.credittordf.dbpedia.threads;

import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.CacheSupport;
import it.unipd.dei.ims.credittordf.synthetic.cachewithcap.QueryWithCacheAndCap;
import it.unipd.dei.ims.data.ReturnBox;

import java.util.List;
import java.util.concurrent.Callable;

public class UpdateTheHitCountsWithCapSyntheticThread implements Callable<ReturnBox>  {

    private QueryWithCacheAndCap instance;
    private String constructQuery;
    private CacheSupport cacheSupport;

    public UpdateTheHitCountsWithCapSyntheticThread(QueryWithCacheAndCap i, String cs, CacheSupport cS) {
        this.instance = i;
        this.constructQuery = cs;
        this.cacheSupport = cS;
    }

    @Override
    public ReturnBox call() throws Exception {
        ReturnBox box = new ReturnBox();

        long start = System.nanoTime();

        // compute the lineage
        List<String[]> lineage = this.instance.getTheLineageOfThisQuery(this.constructQuery);
        // insert the lineage in the cache support
        this.cacheSupport.insertLineage(lineage);

        long elapsed = System.nanoTime() - start;
        box.nanoTime = elapsed;
        box.size = lineage.size();

        return box;
    }
}
