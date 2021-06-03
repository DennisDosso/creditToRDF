package it.unipd.dei.ims.credittordf.dbpedia.cachewithcap;

/** represents one triple, with support information
 * */
public class TripleRecord {

    /** an array containiong three elements: subhect, predicate, and object*/
    public String[] triple;

    /** a progressive integer describing the moment in which this triple entered/was used*/
    public long entryTime;

    /** strikes of this triple*/
    public int strikes;

    /** impact of this triple*/
    public double impact;

    public TripleRecord() {
        triple = new String[3];
        entryTime = 0;
        strikes = 0;
        impact = 0;
    }



}
