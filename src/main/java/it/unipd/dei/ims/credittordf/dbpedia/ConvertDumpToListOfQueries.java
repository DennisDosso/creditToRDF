package it.unipd.dei.ims.credittordf.dbpedia;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * To read and parse a dump of queries and create a directory with all these files.
 * The dump file is the one downloaded from the website <a>https://aksw.github.io/LSQ/</a>,
 * in particular I wrote this cose to deal with
 * the Datadump from <a>https://drive.google.com/u/0/uc?id=0B1tUDhWNTjO-ejZId2JmRTdVaG8&export=download</a>
 * the DBpedia dump of queries. This is a dump of queries, not of DBpedia data, as we originally thought.
 * */
public class ConvertDumpToListOfQueries {

    public static void convertIntoManyFiles(String inputFile, String outputDir) {
        // first, read the file
        Path p = Paths.get(inputFile);
        int queryCounter = 0;
        String query = "";
        int parseErrors = 0, emptyResults = 0;

        try(BufferedReader reader = Files.newBufferedReader(p)) {
            String line = "";
            while((line = reader.readLine()) != null) {
                if(line.contains("sp:text")) {

                    // read the next line and let's check the result size
                    String nextLine = reader.readLine();
                    String[] parts = nextLine.split(";");
                    if(!parts[0].split(" ")[1].contains("lsqv:resultSize")) {
                        // found a query with bad format
                        parseErrors++;
                        continue;
                    }

                    // check the result size of this query. If 0, we do not bother to print it
                    String resultSize = parts[0].split(" ")[2];
                    try{
                        int resSize = Integer.parseInt(resultSize);
                        if (resSize == 0) {
                            // found a query with no results
                            emptyResults++;
                            continue;
                        }
                    } catch(NumberFormatException e) {
                        System.err.println("Found a query with a strange format, here the incriminated part:\n");
                        System.err.println(line + "\n" + nextLine);
                        System.err.println("\nIgnoring this query");
                        continue;
                    }

                    // read the query - and process it
                    query = line.replaceAll("\"", "")
                            .replaceAll("sp:text", "")
                    .replaceAll("FROM <http://dbpedia.org>", "") // I do not want the named graph
                    .trim();

                    query = query.substring(0, query.length()-1); // remove the last ';'
                    // clean the query from characters that we do not want (decode it from url encoding)
                    query = decodeString(query, queryCounter);

                    // print the query
                    try {
                        // file where we write the query
                        String filename = outputDir + "/" + (queryCounter++) + ".txt";
                        FileWriter myWriter = new FileWriter(filename);
                        myWriter.write(query);
                        myWriter.close();

                        // talk to me
                        if(queryCounter % 1000 == 0) {
                            System.out.println("written " + queryCounter + " queries");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } // finished the file
        } catch (IOException e) {
            System.err.println("Query file " + inputFile + " to read not found");
            e.printStackTrace();
        }

        System.out.println("ended the extraction of queries. \n" +
                "Number of queries with empty result set: " + emptyResults +
                "\nnumber of malformed queries: " + parseErrors);
    }

    /** Takes one dump of queries and converts it into one file
     *
     * @param inputFile the dump file downloaded from <a>https://aksw.github.io/LSQ/</a>
     * * */
    public static void convertIntoOneFile(String inputFile, String outputFile) {
        /* first, read the file */
        Path p = Paths.get(inputFile);
        int queryCounter = 0;
        String query = "";
        int parseErrors = 0, emptyResults = 0, queriesWeCannotDealWith = 0;

        try(BufferedReader reader = Files.newBufferedReader(p)) {
            String line = "";
            String filename = outputFile;
            FileWriter myWriter = new FileWriter(filename);

            while((line = reader.readLine()) != null) {
                if(line.contains("sp:text")) {// this is a query
                    // read the next line and let's check the result size
                    String nextLine = reader.readLine();
                    String[] parts = nextLine.split(";");
                    if(!parts[0].split(" ")[1].contains("lsqv:resultSize")) {
                        // found a query with bad format, it does not contain the result size but some other information
                        parseErrors++;
                        continue;
                    }
                    // check the result size of this query. If 0, we do not bother to deal with it
                    String resultSize = parts[0].split(" ")[2];
                    try{
                        int resSize = Integer.parseInt(resultSize);
                        if (resSize == 0) {
                            // found a query with no results
                            emptyResults++;
                            continue;
                        }
                    } catch(NumberFormatException e) {
                        System.err.println("Found a query with a strange format, here the incriminated part:\n");
                        System.err.println(line + "\n" + nextLine);
                        System.err.println("\nIgnoring this query");
                        continue;
                    }

                    //check the presence of keywords we do not want
                    if(line.contains("UNION ") || line.contains("union ")
                            || line.contains("ASK ") || line.contains("ask ")
                            || line.contains("DESCRIBE ") || line.contains("describe ")
                    || line.contains("CONSTRUCT") || line.contains("construct")) {
                        queriesWeCannotDealWith ++;
                        continue;
                    }

                    // read the query - and clean it a little bit
                    query = line.replaceAll("\"", "") // remove the " at beginning and ending of query
                            .replaceAll("sp:text", "") // remove the sp:text piece
                            .replaceAll("FROM <http://dbpedia.org>", "") // I do not want to use the named graph
                            .trim();

                    query = query.substring(0, query.length()-1); // remove the last ';'
                    // decode the query
                    query = decodeString(query, queryCounter);
                    // print the query in an output file (I decided to print one query per file)
                    try {
                        // open file where we will write the query
                        myWriter.write("# QUERYNO " + queryCounter + "\n");
                        myWriter.write(query + "\n");

                        queryCounter++;

                        // talk to me
                        if(queryCounter % 5000 == 0) {
                            System.out.println("written " + queryCounter + " queries");
                            myWriter.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } // end of the process of dealing with a query
            } // finished the file
            myWriter.flush();
            myWriter.close();//flush and close
        } catch (IOException e) {
            System.err.println("Query file " + inputFile + " to read not found");
            e.printStackTrace();
        }

        System.out.println("ended the extraction of queries. \n" +
                "Number of queries with empty result set: " + emptyResults +
                "\nnumber of malformed queries: " + parseErrors +
                "\nnumber of non-BGP queries " + queriesWeCannotDealWith +
                "\nqueries we can work with: " + queryCounter);
    }

    /** The method java.net.URLDecoded.decode seems to only work on URLs, and it breaks on
     * SPARQL queries. So I created my own decoder, hoping for the best.
     * This decoder first takes the urls used in a query, and decodes them.
     * */
    public static String decodeString(String s, int queryCounter) {
//        Pattern p = Pattern.compile("<[^>]*>");
        Pattern p = Pattern.compile("<http[^>]*>");
        Matcher m = p.matcher(s);
        while(m.find()) {
            String url = m.group(0).replaceAll("<|>", "");
            String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
            try {
            s = s.replaceAll(url, decodedUrl);
            } catch (java.util.regex.PatternSyntaxException e) {
                System.err.println("error in pattern of query number: " + (queryCounter));
                System.err.println("the incriminated url: " + url);
                System.err.println("the decoded url: " + decodedUrl);
            } catch (java.lang.IllegalArgumentException e1) {
                System.err.println("error in pattern of query number: " + (queryCounter));
                System.err.println("the incriminated url: " + url);
                System.err.println("the decoded url: " + decodedUrl);
            } catch (java.lang.IndexOutOfBoundsException e2) {
                System.err.println("error in pattern of query number: " + (queryCounter));
                System.err.println("the incriminated url: " + url);
                System.err.println("the decoded url: " + decodedUrl);
            }
        }

        s = s.replaceAll("%2C", ",");
        return s;
    }

    /** test main
     *
     * */
    public static void main(String[] args) {
        String inputFile = "/Users/dennisdosso/Documents/databases/Dbpedia/Dbpedia_queries/lsq_dump/LSQ-DBpedia351.ttl";
        String outputFile = "/Users/dennisdosso/Documents/databases/Dbpedia/Dbpedia_queries/query_files/query_dump.txt";

        convertIntoOneFile(inputFile, outputFile);
    }
}
