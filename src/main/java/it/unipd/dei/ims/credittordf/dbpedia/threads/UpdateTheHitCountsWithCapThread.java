package it.unipd.dei.ims.credittordf.dbpedia.threads;

import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.CacheSupport;
import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.QueriesOnDbpediaWithCacheAndCap;
import it.unipd.dei.ims.data.ReturnBox;

import java.util.List;
import java.util.concurrent.Callable;

/** this thread computes the lineage of a query and adds it to the cache support,
 * keeping its elements updated with their hits.
 * */
public class UpdateTheHitCountsWithCapThread  implements Callable<ReturnBox>  {

    private QueriesOnDbpediaWithCacheAndCap instance;
    private String constructQuery;
    private CacheSupport cacheSupport;


    public UpdateTheHitCountsWithCapThread(QueriesOnDbpediaWithCacheAndCap i, String cs, CacheSupport cS) {
        this.instance = i;
        this.constructQuery = cs;
        this.cacheSupport = cS;
    }


    @Override
    public ReturnBox call() throws Exception {
        ReturnBox box = new ReturnBox();

        long start = System.nanoTime();
        // use the construct query to get the lineage
        List<String[]> lineage = this.instance.getTheLineageOfThisQueryWithoutMap(this.constructQuery);
        // insert the lineage in our cache support
//        this.cacheSupport.insertLineage(lineage);
        this.cacheSupport.insertLineageIntoHeap(lineage);


        long elapsed = System.nanoTime() - start;
        box.nanoTime = elapsed;

        return box;
    }
}
