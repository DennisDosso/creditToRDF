package it.unipd.dei.ims.credittordf.dbpedia.threads;

import it.unipd.dei.ims.credittordf.dbpedia.cachewithcapandcooldown.QueriesWithCacheAndCapAndCooldown;
import it.unipd.dei.ims.credittordf.dbpedia.cachewithcapandcooldown.TimeframeHandler;
import it.unipd.dei.ims.data.ReturnBox;

import java.util.List;
import java.util.concurrent.Callable;

public class UpdateHitsCountOnTimeframesThread implements Callable<ReturnBox>  {

    private QueriesWithCacheAndCapAndCooldown instance;
    private String constructQuery;
    private TimeframeHandler timeframeHandler;

    public  UpdateHitsCountOnTimeframesThread(QueriesWithCacheAndCapAndCooldown i , String cs, TimeframeHandler tfH) {
        this.instance = i;
        this.constructQuery = cs;
        this.timeframeHandler = tfH;
    }


    @Override
    public ReturnBox call() throws Exception {
        ReturnBox box = new ReturnBox();

        long start = System.nanoTime();
        List<String[]> lineage = this.instance.getTheLineageOfThisQuery(this.constructQuery);
        this.timeframeHandler.insertLineage(lineage);
        long elapsed = System.nanoTime() - start;
        box.nanoTime = elapsed;
        return box;
    }
}
