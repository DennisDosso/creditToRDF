package it.unipd.dei.ims.credittordf.dbpedia.cachewithcap;

import java.util.Comparator;

/** Compares the entry times. The one with smaller entry time is considered smaller
 * */
public class TripleRecordComparator implements Comparator<TripleRecord> {

    @Override
    public int compare(TripleRecord o1, TripleRecord o2) {
        try{
            if(o1.entryTime < o2.entryTime)
                return -1;
            else if (o1.entryTime > o2.entryTime)
                return  1;
            else
                return 0;
        } catch (Exception e) {
            System.out.println("Error here, bucko");
            return 1;
        }
    }
}
