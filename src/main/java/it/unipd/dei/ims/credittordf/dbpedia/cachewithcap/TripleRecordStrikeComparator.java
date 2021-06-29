package it.unipd.dei.ims.credittordf.dbpedia.cachewithcap;

import java.util.Comparator;

/** Compares the strike count. The one with smaller strike count is considered smaller
 * */
public class TripleRecordStrikeComparator implements Comparator<TripleRecord> {

    @Override
    public int compare(TripleRecord o1, TripleRecord o2) {
        try{
            if(o1.strikes < o2.strikes)
                return -1;
            else if (o1.strikes > o2.strikes)
                return  1;
            else
                return 0;
        } catch (Exception e) {
            System.out.println("Error comparing, bucko");
            return 1;
        }
    }
}
