package diagnostics.completeness;

/** Little boject to contain the information about the queries, such as execution time,
 * result set, hit or miss.
 *
 * */
public class CompletenessHolder {

    public int queryNo;

    public long time;

    public boolean hit;

    public int resultSetSize;

    /**
     * Set a time expressed as ns in the time field as ms
     * */
    public void setTimeAsMs(long ns) {
        this.time = ns / 1000000;
    }
}
