package diagnostics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/** This class computes X/Y,
 * where
 * X: number of queries seen for the first time with a hit
 * Y: number of queries seen for the first time
 *
 * */
public class QueriesFirstHitsVsFirstTimeQueries {

    public static void main(String[] args) {
        String select_queries_path = "/Users/dennisdosso/Documents/databases/SW Dog Food/queries/select_queries.txt";
        String queries_path = "/Users/dennisdosso/Documents/databases/SW Dog Food/results/times/cache_times_no_cap/cache_times.csv";
        String whole_db_path = "/Users/dennisdosso/Documents/databases/SW Dog Food/results/times/whole_db/whole_db_times.csv";

        int skip = 100;

        // a set to keep track of the found queries
        Set<String> querySet = new HashSet<>();

        int numberOfDistinctNewQueries = 0;
        // hits on the queries
        int hits = 0;
        double completeness = 0;

        int counter = 0;

        // read the queries
        Path p = Paths.get(select_queries_path);
        Path np = Paths.get(queries_path);
        Path wp = Paths.get(whole_db_path);
        try(BufferedReader reader = Files.newBufferedReader(p)) {
            BufferedReader cacheReader = Files.newBufferedReader(np);
            cacheReader.readLine(); // first line is made of headers

            BufferedReader wholeDbReader = Files.newBufferedReader(wp);

            String line = "";
            String cacheLine = "";
            String wholeDbLine = "";

            int queryNo = -1;
            while((line = reader.readLine()) != null) {
                if(line.startsWith("#")){
                    // get the id of the query
                    String[] parts = line.split(" ");
                    try{
                        queryNo = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e2) {
                        System.err.println("error with line " + line);
                    }

                } else {
                    // this is the line with a query
                    cacheLine = cacheReader.readLine();
                    wholeDbLine = wholeDbReader.readLine();
                    counter++;

//                    if(line.contains("OFFSET"))
//                        continue;

                    String[] wholeDbParts = wholeDbLine.split(",");
                    if(wholeDbParts.length < 3)
                        // error or timeout
                        continue;
                    if(wholeDbParts[2].equals("0")) {
                        // do not count queries with result set equal to 0
                        continue;
                    }

                    // produce the hash
                    MessageDigest mDigest;
                    mDigest = MessageDigest.getInstance("SHA-256");
                    mDigest.update(line.getBytes());
                    String queryHash = new String(mDigest.digest());


                    // now look in the set if this query was already found previously
                    if(querySet.contains(queryHash)) {
                        // this query was already found before, we do not need it
                        continue;
                    } else {
                        querySet.add(queryHash);
                        if(counter < skip) {
                            // do not count this query in the counting of hit/misses, move on
                            continue;
                        }

                        // first time
                        numberOfDistinctNewQueries++;
                        String[] cacheParts = cacheLine.split(",");
                        // get if this is a hit or a miss
                        String result = cacheParts[2];
                        if(result.equals("hit")) {
                            hits++;
                            int cacheCompl = Integer.parseInt(cacheParts[3]);
                            int wholeDbCompl = Integer.parseInt(wholeDbParts[2]);
                            double ratio = (double) cacheCompl / wholeDbCompl;
                            if(ratio > 1)
                                ratio = 1;
                            completeness += ratio;
                        }
                    }
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        System.out.println("number of distinct new queries: " + numberOfDistinctNewQueries);
        System.out.println("number of hits: " + hits);
        System.out.println("ratio: " + (double) hits / numberOfDistinctNewQueries);
        System.out.println("completeness ratio on the hits: " + (double) completeness / hits);
    }
}
