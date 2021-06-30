package diagnostics.completeness;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CheckCacheEfficiency {

    public static void checkCacheEfficiency(String inPath) {
        Path p = Paths.get(inPath);
        try(BufferedReader reader = Files.newBufferedReader(p)) {
            String line = "";

            String block = "";
            long cacheTime = 0;
            long dbTime = 0;

            int step = 0;

            while((line = reader.readLine()) != null) {
                if(line.contains("Block")){
                    String[] parts = line.split(" ");
                    block = parts[1];
                    step = 1;
                }

                if(line.contains("Cache average time:")) {
                    String[] parts = line.split(" ");
                    cacheTime = Long.parseLong(parts[3]);
                    step = 2;
                }

                if(line.contains("DB average time")) {
                    String[] parts = line.split(" ");
                    dbTime = Long.parseLong(parts[3]);
                    step = 3;
                }


                if(step==3 && cacheTime > dbTime) {
                    System.out.print(" Block " + block + " has a slower cache");
                    System.out.println(" Cache: " + (double)cacheTime/1000000 + " DB: " + (double)dbTime/1000000);
                    step = 0;
                } else if(step==3 && cacheTime < dbTime) {
                    System.out.print(" Block " + block + " has a faster cache");
                    System.out.println(" Cache: " + (double)cacheTime/1000000 + " DB: " + (double)dbTime/1000000);
                    step = 0;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String path = "/Users/anonymous/Documents/databases/Dbpedia/results/statistiche/block_statistics.txt";
        checkCacheEfficiency(path);
    }
}
