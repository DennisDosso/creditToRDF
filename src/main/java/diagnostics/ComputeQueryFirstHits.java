package diagnostics;

import org.mapdb.Atomic;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** This is a little class to compute some statistics on SWDF.
 * De facto it is a Java script.
 * We look for the cache hits happening on the first time we meet a query
 * */
public class ComputeQueryFirstHits {


    private static Object HashMap;

    public static void main(String[] args) {
        String select_queries_path = "/Users/dennisdosso/Documents/databases/SW Dog Food/queries/select_queries.txt";
        String queries_path = "/Users/dennisdosso/Documents/databases/SW Dog Food/results/times/cache_times_cap_10%/cache_times.csv";
        String whole_db_path = "/Users/dennisdosso/Documents/databases/SW Dog Food/results/times/whole_db/whole_db_times.csv";

        // keeps the id of a query and the hash of its string formulation
        Map<Integer, String> index = new HashMap<Integer, String>();

        // keeps the hash of the queries and the ids of the queries where it appears
        Map<String, List<Integer>> reverseIndex = new HashMap<>();

        Map<Integer, Integer> resultSetCounter = new HashMap<>();

        // keeps the track of the fact if this hash query hashed been already answered from the cache or not
        Map<String, Boolean> alreadyAnsweredMap = new HashMap<>();

        int skip = 100;

        Path p = Paths.get(select_queries_path);
        try(BufferedReader reader = Files.newBufferedReader(p)) {
            String line = "";
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
                    // this is a query
                    // produce the hash
                    MessageDigest mDigest;
                    try {
                        mDigest = MessageDigest.getInstance("SHA-256");
                        mDigest.update(line.getBytes());
                        String queryHash = new String(mDigest.digest());
                        // now insert this hash in the map
                        index.put(queryNo, queryHash);

                        // also, prepare the reverse index
                        List<Integer> l = reverseIndex.get(queryHash);
                        if(l == null) {
                            // create new entry
                            l = new ArrayList<>();
                            l.add(queryNo);
                            reverseIndex.put(queryHash, l);
                        } else {
                            // update current entry
                            l.add(queryNo);
                        }

                        // also, prepare the answered/not answered map
                        alreadyAnsweredMap.put(queryHash, false);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } // we finished reading the select queries

        // now compute our things using the file with the results from the cache
        p = Paths.get(queries_path);
        int freshHits = 0;
        int hitCount = 0;
        int answeredMoreThanOnce = 0;
        try(BufferedReader reader = Files.newBufferedReader(p)) {
            String line = "";
            // first line is of headers
            reader.readLine();
            while((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int queryNo = Integer.parseInt(parts[0]);
                String hitOrMiss = parts[2];
                if(hitOrMiss.equals("hit")) {
                    // found a query hit - get the query hash
                    String queryHash = index.get(queryNo);
                    hitCount++;
                    // check if the query has already been discovered
                    boolean answered = alreadyAnsweredMap.get(queryHash);
                    if(answered) {
                        // do nothing
                        answeredMoreThanOnce ++;
                    } else {
                        // found our ``fresh'' hit
                        alreadyAnsweredMap.put(queryHash, true);
                        freshHits ++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        p = Paths.get(whole_db_path);
        int queriesMoreThanOnce = 0;
        int uniqueQueries = 0;
        int validQueriesCounter = 0;
        int errorQueries = 0;
        int timeoutQueries = 0;
        int zeroCounter = 0;

        int queriesAnsweredExactlyOnce = 0;

        try(BufferedReader reader = Files.newBufferedReader(p)) {
            String line = "";
            Set<String> querySet = new HashSet<>();

            // read the file
            while((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int queryNo = Integer.parseInt(parts[0]);
                if(parts[1].equals("error"))
                {
                    errorQueries++;
                    continue;
                }

                if( parts[1].equals("timeout")){
                    timeoutQueries++;
                    continue;
                }

                int resultSetSize = Integer.parseInt(parts[2]);

                if(resultSetSize > 0) {
                    validQueriesCounter++;
                    resultSetCounter.put(queryNo, resultSetSize);
                    String queryHash = index.get(queryNo);
                    if(querySet.contains(queryHash)) {
                        queriesMoreThanOnce ++;
                    } else {
                        uniqueQueries++;
                        querySet.add(queryHash);
                    }

                    // know if this query has been answered only once
                    List<Integer> l = reverseIndex.get(queryHash);
                    if(l.size() == 1) {
                        queriesAnsweredExactlyOnce++;
                    }
                }

                if(resultSetSize==0)
                    zeroCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Unique queries with a result set >0: " + uniqueQueries);
        System.out.println("Queries with a result set >0 answered more than once: " + queriesMoreThanOnce);
        System.out.println("Overall valid queries: " + validQueriesCounter);
        System.out.println("Queries in error: " + errorQueries);
        System.out.println("Queries in timeout: " + timeoutQueries);
        System.out.println("Queries with empty result set: " + zeroCounter);


        System.out.println("Query hits for the first time: " + freshHits);
        System.out.println("Total query hits: " + hitCount);
        System.out.println("Ration between fresh hits and total hits: " + (double) (freshHits * 100)/hitCount);
        System.out.println("Queries answered more than once: " + answeredMoreThanOnce);
        System.out.println("Number of distinct queries: " + alreadyAnsweredMap.size());
        System.out.println("cache hit ratio: " + (double) freshHits / alreadyAnsweredMap.size());

    }
}
