package it.unipd.dei.ims.credittordf.dbpedia.threads;

import it.unipd.dei.ims.credittordf.dbpedia.QueriesOnDbpediaWithCache;
import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;
import org.eclipse.rdf4j.query.Update;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class UpdateTheHitCountsThread implements Callable<ReturnBox>  {

    private QueriesOnDbpediaWithCache instance;
    private String constructQuery;

    public UpdateTheHitCountsThread(QueriesOnDbpediaWithCache i, String cs) {
        this.instance = i;
        this.constructQuery = cs;
    }

    /** The method builds the lineage of this query, if possible. It then uses it to update the
     * underlying relational database.
     * */
    @Override
    public ReturnBox call() throws Exception {
        ReturnBox box = new ReturnBox();

        // prepare the SQL query to update the RDB
        // we have two statements: one to update a triple already in the DB, or one to add a new triple
        String q = String.format(this.instance.updateHits, RDB.schema);
        String insert_q = String.format(this.instance.INSERT_TRIPLE, RDB.schema);

        long startTime = System.nanoTime();

        // now use the construct query to get the lineage
        List<String[]> lineage = this.instance.getTheLineageOfThisQueryWithoutMap(this.constructQuery);
        if(lineage != null && lineage.size() > 0) {
            box.foundSomething = true;
            box.size = lineage.size();
        }
        else
            box.foundSomething = false;

        try {

            PreparedStatement update_stmt = ConnectionHandler.getConnection().prepareStatement(q);
            PreparedStatement insert_stmt = ConnectionHandler.getConnection().prepareStatement(insert_q);
            // update the RDB using the lineage
            for(String[] triple: lineage) {
                // for each triple in the lineage
                if(this.instance.checkTriplePresence(triple)) { // if it is already in the RDB
                    update_stmt.setInt(1, 1);
                    update_stmt.setString(2, triple[0]);
                    update_stmt.setString(3, triple[1]);
                    update_stmt.setString(4, triple[2]);
                    //this little trick here above is due to the fact that we need objects that are not too big to be properly indexed

                    update_stmt.addBatch();
                } else {
                    // we need to insert it into the RDB
                    insert_stmt.setString(1, triple[0]);
                    insert_stmt.setString(2, triple[1]);
                    insert_stmt.setString(3, triple[2]);

                    insert_stmt.executeUpdate();
                    ConnectionHandler.getConnection().commit();
                }
            } // covered the whole lineage

            update_stmt.executeBatch();
            ConnectionHandler.getConnection().commit();
        } catch (SQLException throwables) {
            try{
                System.err.println("Error with SQL query to update hits. The CONSTRUCT query was " + this.constructQuery.substring(0, 25) + "...\n");
            } catch(Exception e) {
                System.err.println("Error with SQL query to update hits.");
            }
//            throwables.printStackTrace();
        }
        long totalTime = System.nanoTime() - startTime;
        box.nanoTime = totalTime;

        return box;
    }
}
