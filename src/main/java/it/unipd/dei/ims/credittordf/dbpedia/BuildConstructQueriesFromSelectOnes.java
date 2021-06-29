package it.unipd.dei.ims.credittordf.dbpedia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Given the file built with {@link ConvertDumpToListOfQueries}, this class
 * creates a new file with the equivalent set of CONSTRUCT queries.
 * */
public class BuildConstructQueriesFromSelectOnes {

    public static void convertQueriesToConstruct(String inputFile, String outputFile) {
        Path inputPath = Paths.get(inputFile);
        Path outputPath = Paths.get(outputFile);
        try(BufferedReader reader = Files.newBufferedReader(inputPath)) {
            BufferedWriter writer = Files.newBufferedWriter(outputPath);

            //read lines from the input file
            String line = "";
            String queryNumber = "";
            int queryCount = 0;

            while((line = reader.readLine()) != null) {
                if(line.startsWith("#")) {
                    // get the query number
                    queryNumber = line.split(" ")[2];
                } else {
                    queryCount ++;
                    // dealing with a query
                    String constructQuery = "";

                    // first, get the prefixes
                    String[] parts = line.split("SELECT|select");
                    String prefixes = "";
                    if(parts.length>1) {
                        //prefixes may be absent, only if we have 2 elements it is ok to have them
                        prefixes = parts[0];
                        constructQuery += prefixes;
                    }

                    //extrapolate the whole pattern inside the WHERE clause
                    String regex = "(WHERE|where)( |\t)*\\{(.+)}";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(line);
                    String pattern;
                    if(m.find()){
                        pattern = m.group(3);
                    } else {
                        // maybe this is a nice little query without a WHERE keyword
                        // identify the pattern
                        regex = "(SELECT|select)[^\\{]*\\{(.*)}";
                        p = Pattern.compile(regex);
                        m = p.matcher(line);
                        if(m.find()) {
                            pattern = m.group(2);
                        } else {
                            // this is a query with some strange way of doing things that I do not know
                            System.err.println("Error with query number " + queryNumber +
                            "\nwhich is: " + line);
                            continue;
                        }
                    }

                    // extrapolate the pattern before OPTIONAL and filter
                    regex = "(.+?)(?=OPTIONAL|optional|FILTER|filter)";
                    p = Pattern.compile(regex);
                    m = p.matcher(pattern);
                    String mainBody;
                    if(m.find()){
                        mainBody = m.group(1);
                    } else {
                        mainBody = pattern;
                    }
                    mainBody = mainBody.trim();
                    if(!mainBody.endsWith(".")) {
                        mainBody += ".";
                    }

                    // We count how many '.' we have in the mainBody. This gives us an indication of how many triples we have
                    long count = mainBody.chars().filter(ch -> ch == '.').count();

                    //extrapolate the pattern of OPTIONAL
                    regex = "(OPTIONAL|optional) ?\\{([^}]*)}";
                    p = Pattern.compile(regex);
                    m = p.matcher(pattern);
                    String optionalPattern = "";
                    while(m.find()) {
                        String optPattern = m.group(2);
                        optionalPattern += " " + optPattern;
                        count++;
                    }

                    // find the offset if present
                    regex = "(OFFSET|offset)( *)([0-9]+)";
                    p = Pattern.compile(regex);
                    m = p.matcher(line);
                    String offset = "";
                    if(m.find()) {
                        offset = m.group(3).trim();
                    }

                    //last, find the limit if present
                    regex = "(LIMIT|limit)(.*)";
                    p = Pattern.compile(regex);
                    m = p.matcher(line);
                    int limit = -1;
                    if(m.find()) {
                        try{
                            limit = Integer.parseInt(m.group(2).trim());
                        } catch ( NumberFormatException e) {

                        }
                    }

                    // now build the CONSTRUCT query
                    constructQuery += " CONSTRUCT { " + mainBody + " " + optionalPattern +
                            " } WHERE { " + pattern + "} ";
                    if(!offset.equals("")) {
                        constructQuery += "OFFSET " + offset;
                    }

                    if(limit != -1) {
                        constructQuery += " LIMIT " + limit*count;
                    }

                    writer.write("# QUERYNO " + queryNumber);
                    writer.newLine();
                    writer.write(constructQuery);
                    writer.newLine();

                }// end processing of one query
            } // done all queries
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String inputPath =
                "/Users/dennisdosso/Documents/databases/SW Dog Food/queries/select_queries.txt";
        String outputPath =
                "/Users/dennisdosso/Documents/databases/SW Dog Food/queries/construct_queries.txt";
        convertQueriesToConstruct(inputPath, outputPath);
    }

}
