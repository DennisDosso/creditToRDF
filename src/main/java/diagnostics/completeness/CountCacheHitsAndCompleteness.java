package diagnostics.completeness;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** The average times are kept in ns
 * */
public class CountCacheHitsAndCompleteness {

    // skip the first tot queries, where we do not have the use of a cache
    // BSBM: 20, SWDF: 100, DBpedia: 1000
    public static int skip = 1000;

    /** size of a block*/
    public static int blockSize = 1000;

    private static String outPath = "/Users/dennisdosso/Documents/databases/Dbpedia/statistiche/cache_cap_50k/block_statistics.txt";
    
    public static void countCacheHitsAndCompleteness (String cacheFile, String wholeDbFile) {
        // open the whole db file
        Path wDB = Paths.get(wholeDbFile);
        Path cF = Paths.get(cacheFile);

        Map<Integer, CompletenessHolder> dbMap = new HashMap<Integer, CompletenessHolder>();
        Map<Integer, CompletenessHolder> cacheMap = new HashMap<Integer, CompletenessHolder>();

        // read the info about the database
        try(BufferedReader dbReader = Files.newBufferedReader(wDB)) {
            String line = "";
            while((line = dbReader.readLine()) != null) {
                String[] parts = line.split(",");
                CompletenessHolder holder = new CompletenessHolder();
                try{
                    holder.queryNo = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    // found some line that is not a number
                    continue;
                }
                boolean valid = true;
                try{
                    holder.time = (Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    // timeout or error - jump query
                    holder.time = 0;
                    holder.resultSetSize = 0;
                    valid = false;
                }

                if(valid)
                    holder.resultSetSize = Integer.parseInt(parts[2]);

                dbMap.put(Integer.parseInt(parts[0]), holder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(dbMap.size());

        // populate the other query
        try(BufferedReader dbReader = Files.newBufferedReader(cF)) {
            String line = "";
            // first line are headers
            dbReader.readLine();
            while((line = dbReader.readLine()) != null) {
                String[] parts = line.split(",");
                CompletenessHolder holder = new CompletenessHolder();
                holder.queryNo = Integer.parseInt(parts[0]);
                try{
                    holder.time =  Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    // query went to timeout or error
                    continue;
                } catch( IndexOutOfBoundsException e2) {
                    continue;
                }

                if(parts[2].equals("hit"))
                    holder.hit = true;
                else if (parts[2].equals("miss"))
                    holder.hit = false;

                holder.resultSetSize = Integer.parseInt(parts[3]);
                if(holder.resultSetSize == 0)
                    continue;

                cacheMap.put(holder.queryNo, holder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(cacheMap.size());

        // compute our statistics!
        computeTotalNumberOfCacheHits(dbMap, cacheMap);

        computeTotalNumberOfCacheHitsWithBlocksOf(blockSize, dbMap, cacheMap);
    }

    private static void computeTotalNumberOfCacheHitsWithBlocksOf(int blockSize,
                                                                  Map<Integer, CompletenessHolder> dbMap,
                                                                  Map<Integer, CompletenessHolder> cacheMap) {
        Path outP = Paths.get(outPath);
        try(BufferedWriter writer = Files.newBufferedWriter(outP)) {
            int times = dbMap.size() / blockSize;
            for(int j = 1; j <= times; ++j) { // starting from 1 because the first block does not have a cache
                computeTotalNumberOfCacheHitsWithLimitAndOffset(blockSize,
                        j*blockSize,
                        dbMap,
                        cacheMap,
                        writer);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void computeTotalNumberOfCacheHitsWithLimitAndOffset(int limit, int offset,
                                                                 Map<Integer, CompletenessHolder> dbMap,
                                                                 Map<Integer, CompletenessHolder> cacheMap,
                                                                        BufferedWriter writer) throws IOException {
        int block = offset / limit;

        int cacheHits = 0;
        int cacheMiss = 0;

        // the average between the # of result set size from the cache and the number of cache hits
        double overallCompleteness = 0;

        long dbTime = 0;
        long cacheTime = 0;

        for(int i = offset; i < offset+limit; ++i) {
            CompletenessHolder cacheHolder = cacheMap.get(i);


            CompletenessHolder dbHolder = dbMap.get(i);
            if(dbHolder == null) {
                continue;
            }

            if(cacheHolder!= null && cacheHolder.hit){
                cacheHits ++;
                int wholeResultSet = dbHolder.resultSetSize;
                int cacheResultSet = cacheHolder.resultSetSize;

                dbTime += dbHolder.time;
                if(dbTime == 0)
                    continue;

                double singleCompleteness = (double) cacheResultSet / wholeResultSet;
                if(singleCompleteness > 1)
                    singleCompleteness = 1;
                overallCompleteness += singleCompleteness;


                cacheTime += cacheHolder.time;
            } else if (cacheHolder != null && !cacheHolder.hit) {
                if(dbHolder.resultSetSize != 0)
                    cacheMiss++;
                else {
                     // do nothing, we do not count this query (empty result set)
                    }
            } else if(cacheHolder == null) {
                if(dbHolder.resultSetSize !=0)
                    cacheMiss++;
            }

        }

        if(cacheHits != 0) {
            overallCompleteness = overallCompleteness / cacheHits;
            dbTime = dbTime / cacheHits;
            cacheTime /= cacheHits;
        }


        double hitRation = 0;
        if(cacheHits > 0)
                hitRation = ((double) cacheHits / (cacheHits + cacheMiss));

        if(overallCompleteness > 1)
            overallCompleteness = 1;

        writer.write("\n=========="); writer.newLine();
        writer.write("Block: " + block); writer.newLine();
        writer.write("=========="); writer.newLine();
        writer.write("cache hit ratio: " + hitRation); writer.newLine();
        writer.write("cache hits: " + cacheHits); writer.newLine();
        writer.write("cache miss: " + cacheMiss); writer.newLine();
        writer.write("total number of queries " + (cacheHits + cacheMiss)); writer.newLine();
        writer.write("Cache average time: " + cacheTime); writer.newLine();
        writer.write("DB average time " + dbTime); writer.newLine();
        writer.write("Average completeness " + overallCompleteness); writer.newLine();
        writer.write("==========\n"); writer.newLine();

//        System.out.println("\n==========");
//        System.out.println("Block: " + block);
//        System.out.println("==========");
//        System.out.println("cache hit ratio: " + hitRation);
//        System.out.println("cache hits: " + cacheHits);
//        System.out.println("cache miss: " + cacheMiss);
//        System.out.println("total number of queries " + (cacheHits + cacheMiss));
//        System.out.println("Cache average time: " + cacheTime);
//        System.out.println("DB average time " + dbTime);
//        System.out.println("Average completeness " + overallCompleteness);
//        System.out.println("==========\n");

    }


    private static void computeTotalNumberOfCacheHits(Map<Integer, CompletenessHolder> dbMap,
                                               Map<Integer, CompletenessHolder> cacheMap) {

        int cacheHits = 0;
        int cacheMiss = 0;
        double overallCompleteness = 0;

        long dbTime = 0;
        long cacheTime = 0;

        for(int i = skip; i < dbMap.size(); ++i) {
            CompletenessHolder cacheHolder = cacheMap.get(i);

            CompletenessHolder dbHolder = dbMap.get(i);
            if(dbHolder == null) {
                continue;
            }


            if(cacheHolder!= null && cacheHolder.hit){
                cacheHits ++;
                int wholeResultSet = dbHolder.resultSetSize;
                int cacheResultSet = cacheHolder.resultSetSize;

                dbTime += dbHolder.time;
                if(dbTime == 0)
                    continue;

                double singleCompleteness = (double) cacheResultSet / wholeResultSet;

                if(singleCompleteness > 1) // it may be the case, with our construct, to return some more results if no limit was specified
                    singleCompleteness = 1;
                overallCompleteness += singleCompleteness;


                cacheTime += cacheHolder.time;
            } else if (cacheHolder != null && !cacheHolder.hit) {
                if(dbHolder.resultSetSize != 0)
                    cacheMiss++;
                    // else do nothing, we do not count this query (empty result set)

            } else if(cacheHolder == null) {
                if(dbHolder.resultSetSize !=0)
                    cacheMiss++;
            }
        }

        overallCompleteness = overallCompleteness / cacheHits;
        if(cacheHits != 0) {
            cacheTime /= cacheHits;
            dbTime = dbTime / cacheHits;
        }
        else {
            dbTime = 0;
            cacheTime = 0;
        }


        double hitRation = 0;
        if(cacheHits > 0)
            hitRation = ((double) cacheHits / (cacheHits + cacheMiss));

        System.out.println("cache hit ratio: " + hitRation);
        System.out.println("cache hits: " + cacheHits);
        System.out.println("cache miss: " + cacheMiss);
        System.out.println("total number of queries " + (cacheHits + cacheMiss));
        System.out.println("Cache average time: " + cacheTime);
        System.out.println("DB average time " + dbTime);
        System.out.println("Average completeness " + overallCompleteness);
    }


    public static void main(String[] args) {

        // put here the paths of the two input files, the data of time on the whole DB and on the cache
        String wholeDbTimes = "/Users/dennisdosso/Documents/databases/Dbpedia/results/whole_db/whole_db_times.csv";
        String cacheTimes = "/Users/dennisdosso/Documents/databases/Dbpedia/results/cache_cap_50k/cache_times.csv";

        countCacheHitsAndCompleteness(cacheTimes, wholeDbTimes);
    }

}
