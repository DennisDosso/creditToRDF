package it.unipd.dei.ims.credittordf.dbpedia.cachewithcap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public String hash;

    public TripleRecord() {
        triple = new String[3];
        entryTime = 0;
        strikes = 0;
        impact = 0;
    }

    public TripleRecord(String[] t) {
        this.triple = new String[3];
        this.triple[0] = t[0];
        this.triple[1] = t[1];
        this.triple[2] = t[2];

        this.setHash();

        entryTime = 0;
        strikes = 0;
        impact = 0;
    }


    public void setHash() {
        String wholeTriple = this.triple[0] + this.triple[1] + this.triple[2];
        MessageDigest mDigest;

        try {
            mDigest = MessageDigest.getInstance("SHA-256");
            mDigest.update(wholeTriple.getBytes());
            this.hash = new String(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }



}
