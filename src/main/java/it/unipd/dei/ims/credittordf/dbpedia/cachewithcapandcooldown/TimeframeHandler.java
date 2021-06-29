package it.unipd.dei.ims.credittordf.dbpedia.cachewithcapandcooldown;

import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.TripleRecord;
import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.TripleRecordStrikeComparator;
import it.unipd.dei.ims.credittordf.utils.CustomURI;
import it.unipd.dei.ims.data.ReturnBox;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.*;

import static it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.CacheSupport.striplObjectFromDatatypes;
import static org.eclipse.rdf4j.model.util.Values.iri;

/** The child of {@link experiment2.EpochsHandler} and {@link it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.CacheSupport}
 * that allows us to have a cache that has a cap and also follows the time-based strategy*/
public class TimeframeHandler {

    /** The timeframes composing the cache*/
    private int timeframesNumber;

    /** the timeframes that constitute our cache. Each timeframe is a heap of triples,
     * constituting the cache built during that timeframe. */
    public List<PriorityQueue<TripleRecord>> timeframes;

    private PriorityQueue<TripleRecord> currentTimeframe;

    /** cap given to the overall cache*/
    private int cap;

    /** cap for one timeframe (the timeframes will have the same maximum size). */
    private int timeFrameCap;

    /** Map to keep track of our timeframes*/
    public Map<String, TripleRecord> tripleMap;

    public long currentTime;

    /**
     *
     * @param tFN the number of timeframes making the cache
     * @param c the cap of the whole cache*/
    public TimeframeHandler(int tFN, int c) {
        this.timeframesNumber = tFN;
        timeframes = new ArrayList<>();
        this.currentTimeframe = new PriorityQueue<>(new TripleRecordStrikeComparator());
        this.timeframes.add(currentTimeframe);
        this.cap = c;
        this.timeFrameCap = c / timeframesNumber;
        this.tripleMap = new HashMap<String, TripleRecord>();
        this.currentTime = 0;
    }

    public void startNewTimeframe() {
        PriorityQueue<TripleRecord> tf = new PriorityQueue<>(new TripleRecordStrikeComparator());
        this.currentTimeframe = tf;
        this.timeframes.add(tf);
        this.update();
        // clear the triple map
        tripleMap.clear();
    }

    public void update() {
        while(this.timeframes.size() > this.timeframesNumber) {
            this.timeframes.remove(0);
        }
    }

    /** Given a list of triples that are the lineage of a query output, add them
     * */
    public void insertLineage(List<String[]> lineage) {

        if(lineage.size() > this.timeFrameCap) // too big of a lineage for one query
            return;

        for(String[] t : lineage) {
            if(this.checkIfTripleIsAlreadyPresentInCurrentTimeframe(t))
                continue;
            else {
                // create the new triple and add it
                TripleRecord tr = new TripleRecord(t);
                tr.entryTime = this.currentTime;
                tr.strikes = 1;
                tr.impact = Math.log(2);

                // add it to the queue
                this.currentTimeframe.add(tr);
                this.tripleMap.put(tr.hash, tr);
            }
        }

        if(this.tripleMap.size() > this.timeFrameCap) {
            // remove least recently used triples
            int toRemove = this.tripleMap.size() - this.timeFrameCap;
            this.removeTheseManyTriplesFromQueue(toRemove);
        }

    }

    private boolean checkIfTripleIsAlreadyPresentInCurrentTimeframe(String[] t) {
        TripleRecord checkingT = new TripleRecord(t);
        // check if triple is alredy present in the map
        TripleRecord cT = this.tripleMap.get(checkingT.hash);
        if(cT == null)
            // this triple is not already present
            return false;
        else {
            // update the triple, since we used it once again
            cT.strikes++;
            cT.entryTime = this.currentTime;
            cT.impact = Math.log(1 + cT.strikes);

            // remove and re-insert it into the heap to keep it ordered
            boolean test = this.currentTimeframe.remove(cT);
            this.currentTimeframe.add(cT);
            return true;
        }
    }

    /** Removes a certain quantity of least striked triples from the current timeframe
     * */
    private void removeTheseManyTriplesFromQueue(int toRemove) {
        for(int i = 0; i < toRemove; ++i) {
            TripleRecord tr = this.currentTimeframe.poll();
            if(tr != null) {
                this.tripleMap.remove(tr.hash);
            }
        }
    }

    public ReturnBox buildCache(RepositoryConnection cache, int threshold) {
        ReturnBox box = new ReturnBox();
        // clear the cache
        cache.clear();
        long n = System.currentTimeMillis();
        UrlValidator urlValidator = new UrlValidator();
        ModelBuilder builder = new ModelBuilder();
        ValueFactory vf = SimpleValueFactory.getInstance();

        for (PriorityQueue<TripleRecord> timeframe : this.timeframes) {
            for (TripleRecord tr : timeframe) {
                this.addTripleRecordToCache(tr, threshold, builder, urlValidator, vf);
            }
        }
        Model graph = builder.build();
        cache.add(graph);
        box.nanoTime  = System.currentTimeMillis() - n;
        box.size = graph.size();
        return box;
    }

    private void addTripleRecordToCache(TripleRecord tr,
                                        int threshold,
                                        ModelBuilder builder,
                                        UrlValidator urlValidator,
                                        ValueFactory vf) {
        String sub = tr.triple[0];
        String pred = tr.triple[1];
        String obj = tr.triple[2];

        if(tr.impact < threshold)
            return; // this triple does not go in the cache

        if(!urlValidator.isValid(sub))
            sub = "http://dbpedia.org/node/" + sub;

        if(!urlValidator.isValid(pred))
            sub = "http://dbpedia.org/property/" + pred;





        // insert this triple in the cache
        if(urlValidator.isValid(obj)) { // it is (probably) a IRI
            try{
                IRI o = iri(obj);
                builder.subject(sub).add(pred, o);
            } catch(IllegalArgumentException iae) {
                System.err.println("Raised illegal argument exception with triple "
                        + sub + " " + pred + " " + obj);
            } catch (ModelException e) {
                System.err.println("Raised model exception exception with triple "
                        + sub + " " + pred + " " + obj);
            }
        } else {// it is (probably) a literal
            String[] parts = striplObjectFromDatatypes(obj);
            // depending on its type, we insert the literal
            if(parts == null) {
                try{
                    // the object is a broken IRI since we were unable to find a datatype
                    builder.subject(sub).add(pred, obj);
                } catch (ModelException e) {
                    // we do not do anything, this triple is simply lost
                    System.err.println("Raised model exception with triple "
                            + sub + " " + pred + " " + obj);
                }
            } else {
                // some for of literal
                try{
                    if(parts[2].equals("@"))
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], parts[1]));

                    if(parts[2].equals("integer"))
                        builder.subject(sub).add(pred, Integer.parseInt(parts[0]));
                    if(parts[2].equals("double"))
                        builder.subject(sub).add(pred, Double.parseDouble(parts[0]));
                    if(parts[2].equals("float"))
                        builder.subject(sub).add(pred, Float.parseFloat(parts[0]));
                    if(parts[2].equals("date"))
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.DATE));
                    if(parts[2].equals("dateTime"))
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.DATETIME));
                    if(parts[2].equals("nonNegativeInteger"))
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.NON_NEGATIVE_INTEGER));
                    if(parts[2].equals("gYear"))
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.GYEAR));
                    if(parts[2].equals("gMonthDay"))
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.GMONTHDAY));
                    if(parts[2].equals("custom")) {
                        CustomURI u = new CustomURI(parts[3], parts[4]);
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], u));
                    }
                    if(parts[2].equals("XMLSchema")) {
                        CustomURI u = new CustomURI(parts[3], parts[4]);
                        builder.subject(sub).add(pred, vf.createLiteral(parts[0], u));
                    }
                    if(parts[2].equals("unknown")) {
                        System.out.println("[WARNING] this triple has a special datatype, thus is added " +
                                "as simple literal: " +
                                sub + " " + pred + " " + obj);
                        builder.subject(sub).add(pred, obj.replaceAll("\"", ""));
                    }
                } catch(ModelException e) {
                    System.err.println("error in inserting literal " + obj + " inserting it as general literal");
                    try{
                        builder.subject(sub).add(pred, obj.replaceAll("\"", ""));
                    } catch (Exception e2) {
                        System.err.println("Truly impossible to import " + sub + " " + pred + " " + obj);
                        e.printStackTrace();
                        e2.printStackTrace();
                    }
                }
            }
        }
    }



}
