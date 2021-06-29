package it.unipd.dei.ims.credittordf.dbpedia.cachewithcap;

import it.unipd.dei.ims.credittordf.utils.CustomURI;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.ReturnBox;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.graph.Triple;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.*;

import static org.eclipse.rdf4j.model.util.Values.iri;

/** Used to work with an upper limit to the cache
 * */
public class CacheSupport {

    /** should always be equal to the sum of the available lineages */
    public int cacheSize;

    public int cap;

    /** List containing all our triples. These are the triples that go in our cache*/
    public List<TripleRecord> triples;

    public PriorityQueue<TripleRecord> tripleQueue;

    public Map<String, TripleRecord> tripleMap;

    /** Keeps track of the triples to add/remove to the next cache update*/
    public List<TripleRecord> addendum, removendum;

    public long currentTime;

    /**
     * @param c the cap that we want to impose to our cache
     * */
    public CacheSupport(int c) {
        cacheSize = 0;
        triples = new ArrayList<>();
        this.addendum = new ArrayList<>();
        this.removendum = new ArrayList<>();
        cap = c;
        currentTime = 0;
        this.tripleQueue = new PriorityQueue<>(new TripleRecordComparator());
        this.tripleMap = new HashMap<String, TripleRecord>();
    }

    /**Inserts a new lineage in the cache support.
     * */
    public void insertLineage(List<String[]> lineage) {
        // check if it is too big of a lineage (like a SELECT * {?s ?p ?o})
        if(lineage.size() > cap)
            return;

        // insert the new triples
        for(String[] t : lineage) {
            // check if it is not already present
            boolean alreadyPresent = this.checkIfTripleIsAlreadyPresent(t);
            if(alreadyPresent) {
                continue;
            } else {
                // need to insert this new triple
                TripleRecord tr = new TripleRecord();
                tr.triple = t;
                tr.entryTime = this.currentTime;
                tr.strikes = 1;
                tr.impact = Math.log(2);
                this.triples.add(tr);
            }
        }

        // free space if necessary
        if(this.triples.size() > this.cap) {
            // we need to remove this many triples from the cache
            int toRemove = this.triples.size() - cap;
            this.removeTheseManyTriples(toRemove);
        }

        this.currentTime++;
    }

    /** A different, hopefully more efficient, way to keep our cache updated*/
    public void insertLineageIntoHeap(List<String[]> lineage) {
        if(lineage.size() > cap)
            return;

        for(String[] t : lineage) {
            if(this.checkIfTripleIsAlreadyPresentInQueue(t)) {
                continue;
            } else {
                // create the triple and add it
                TripleRecord tr = new TripleRecord(t);
                tr.entryTime = this.currentTime;
                tr.strikes = 1;
                tr.impact = Math.log(2);

                // add it to the queue
                this.tripleQueue.add(tr);
                this.tripleMap.put(tr.hash, tr);
                this.addendum.add(tr);
            }
        }

        if(this.tripleMap.size() > this.cap) {
            int toRemove = this.tripleMap.size() - cap;
            this.removeTheseManyTriplesFromQueue(toRemove);
        }

    }

    private void removeTheseManyTriplesFromQueue(int toRemove) {
        for(int i = 0; i < toRemove; ++i) {
            TripleRecord tr = this.tripleQueue.poll();
            if(tr != null) {
                this.tripleMap.remove(tr.hash);
                this.removendum.add(tr);
            }
        }
    }

    /** Implements the removal of triples using a Least Recently Used (LRU) approach
     * */
    private void removeTheseManyTriples(int toRemove) {
        // first, we order our triple
        try{
            Collections.sort(this.triples, new TripleRecordComparator());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i= 0; i < toRemove; ++i) {
            this.triples.remove(0);
        }
    }


    private boolean checkIfTripleIsAlreadyPresentInQueue(String[] t) {
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
            boolean test = this.tripleQueue.remove(cT);
            this.tripleQueue.add(cT);
            return true;
        }

    }

    /** Check if it is present and also update it*/
    private boolean checkIfTripleIsAlreadyPresent(String[] t){
        for(TripleRecord tr: this.triples) {
            String[] tR = tr.triple;
            if(tR[0].equals(t[0]) &&
                    tR[1].equals(t[1]) &&
                    tR[2].equals(t[2])) {
                //update strikes
                tr.strikes ++;
                //update entry time
                tr.entryTime = this.currentTime;
                // update impact
                tr.impact = Math.log(1 + tr.strikes);
                return true;
            }
        }
        return false;
    }

    /** Uses the data in our CacheSupport to refresh the cache
     * */
    public ReturnBox buildCache(RepositoryConnection cache, int threshold) {
        ReturnBox box = new ReturnBox();
        // clear the cache
        cache.clear();

        // take the time required to update the cache
        long n = System.currentTimeMillis();
        // we do not need to update the impact of the triples since it is already updated here

        UrlValidator urlValidator = new UrlValidator();
        ModelBuilder builder = new ModelBuilder();
        ValueFactory vf = SimpleValueFactory.getInstance();

        for(TripleRecord tr : this.triples) {
            String sub = tr.triple[0];
            String pred = tr.triple[1];
            String obj = tr.triple[2];

            if(tr.impact < threshold)
                continue; // this triple does not go in the cache

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
                        continue;
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
        } // done with all our triples

        // add this graph to our cache
        Model graph = builder.build();
        cache.add(graph);
        long elapsed = System.currentTimeMillis() - n;
        box.nanoTime  = elapsed;
        box.size = graph.size();
        return box;

    }

    /** Uses the data in our CacheSupport to refresh the cache.
     * */
    public ReturnBox buildCacheFromQueue(RepositoryConnection cache, int threshold) {
        ReturnBox box = new ReturnBox();

        // clear the cache
        cache.clear();
        this.addendum.clear();
        this.removendum.clear();

        // take the time required to update the cache
        long n = System.currentTimeMillis();
        // we do not need to update the impact of the triples since it is already updated here

        UrlValidator urlValidator = new UrlValidator();
        ModelBuilder builder = new ModelBuilder();
        ValueFactory vf = SimpleValueFactory.getInstance();

        Iterator<Map.Entry<String, TripleRecord>> i;
        i = this.tripleMap.entrySet().iterator();
        try{

            while(i.hasNext()) {
                /* for(Map.Entry<String, TripleRecord> eTr : this.tripleMap.entrySet()) { */
                Map.Entry<String, TripleRecord> eTr = i.next();
                TripleRecord tr = eTr.getValue();

                String sub = tr.triple[0];
                String pred = tr.triple[1];
                String obj = tr.triple[2];

                if(tr.impact < threshold)
                    continue; // this triple does not go in the cache

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
                            continue;
                        }
                    } else {
                        // some for of literal
                        try{
                            if(parts[2].equals("@"))
                                builder.subject(sub).add(pred, vf.createLiteral(parts[0], parts[1]));

                            if(parts[2].equals("integer"))
                                try{
                                    builder.subject(sub).add(pred, Integer.parseInt(parts[0]));
                                } catch(NumberFormatException e2) {
                                    try{
                                        builder.subject(sub).add(pred, Float.parseFloat(parts[0]));
                                    } catch (Exception e3) {
                                        builder.subject(sub).add(pred, obj.replaceAll("\"", ""));
                                    }
                                }
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
            } // done with all our triples
        } catch (ConcurrentModificationException eh) {

        }

        // add this graph to our cache
        Model graph = builder.build();
        cache.add(graph);
        box.nanoTime  = System.currentTimeMillis() - n;
        box.size = graph.size();
        return box;

    }

    /** to add things in an rdf4j Model, we need to divide the elements*/
    public static String[] striplObjectFromDatatypes(String obj) {
        if(obj.contains("@")) {
            String content = obj.split("@")[0].replace("\"", "");
            String language = obj.split("@")[1];
            if(language.equals(""))
                language = "en";
            return new String[] {content, language, "@"};
        } else if(obj.contains("^^")) {
            // a general type of datatype
            String content = obj.split("\\^\\^")[0].replace("\"", "");
            String datatype = obj.split("\\^\\^")[1].replace("\"", "");
            // we deal with a certain set of datatype. Maybe you'll need to deal with other datatypes here
            if(datatype.contains("integer"))
                return new String[] {content, datatype, "integer"};
            else if(datatype.contains("double"))
                return new String[] {content, datatype, "double"};
            else if(datatype.contains("float"))
                return new String[] {content, datatype, "float"};
            else if(datatype.contains("date"))
                return new String[] {content, datatype, "date"};
            else if(datatype.contains("dateTime"))
                return new String[] {content, datatype, "dateTime"};
            else if(datatype.contains("nonNegativeInteger"))
                return new String[] {content, datatype, "nonNegativeInteger"};
            else if(datatype.contains("gYear"))
                return new String[] {content, datatype, "gYear"};
            else if(datatype.contains("gMonthDay"))
                return new String[] {content, datatype, "gMonthDay"};
            else if(datatype.contains("dbpedia.org/datatype")) {
                // custom datatype of dbPedia
                String[] parts = datatype.split("/");
                String value = parts[parts.length-1];
                return new String[] {content, datatype, "custom", "http://dbpedia.org/datatype/", value};
            } else if(datatype.contains("XMLSchema")){
                String[] parts = datatype.split("#");
                String value = parts[parts.length-1];
                return new String[] {content, datatype, "XMLSchema", parts[0], value};
            } else if(datatype.contains("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/")) {
                String[] parts = datatype.split("/");
                String value = parts[parts.length-1];
                return new String[] {content, datatype, "custom", "http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/", value};
            }
            else {
                System.err.println("[WARNING!!!] a new datatype found in DBpedia," + datatype);
                return new String[] {content, datatype, "unknown"};
            }

        }
        return null;
    }


}
