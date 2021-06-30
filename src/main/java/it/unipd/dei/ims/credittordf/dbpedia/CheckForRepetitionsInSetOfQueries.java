package it.unipd.dei.ims.credittordf.dbpedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class CheckForRepetitionsInSetOfQueries {

    public static void checkForRepeatedQueries(String inputFile) throws NoSuchAlgorithmException {
        // object to perfor hash
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        Map<String, Integer> map = new HashMap<String, Integer>();

        //read the file
        Path inputPath = Paths.get(inputFile);

        // process the file
        try(BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String line = "";
            while((line = reader.readLine()) != null) {
                if(line.startsWith("#")) {
                    // header with the number of the query
                    String queryNumber = line.split(" ")[2];
                } else {
                    // we have a query - create an hash
                    messageDigest.update(line.getBytes());
                    String hashedString = new String(messageDigest.digest());
                    Integer hit = map.get(hashedString);
                    if(hit != null) { // query already found
                        map.put(hashedString, hit + 1);
                    } else { // first time we see this query
                        map.put(hashedString, 1);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("error in trying to read file:\n "  + inputFile + "\n\nHere some stack trace:\n");
            e.printStackTrace();
        }

        int repeatedQueries = 0;
        for(Map.Entry<String, Integer> e : map.entrySet()) {

            if(e.getValue() > 1) {
                System.out.println(e.getValue());
                repeatedQueries++;
            }
        }
        System.out.println("Number of unique queries: " + map.size() +
                "\nnumber of repeated queries: " + repeatedQueries);
    }

    public static void main(String[] args) {
//        String inputFile =
//                "/Users/anonymous/Documents/databases/Dbpedia/Dbpedia_queries/processed_queries/query_dump.txt";

        String inputFile =
                "/Users/anonymous/Documents/databases/SW Dog Food/queries/select_queries.txt";
        try {
            checkForRepeatedQueries(inputFile);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
