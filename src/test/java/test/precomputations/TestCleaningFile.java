package test.precomputations;

import it.unipd.dei.ims.data.MyPaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestCleaningFile {

    public static void splitDatasetFile(String inputString) {
        Path fileIn = Paths.get(inputString);
        try(BufferedReader reader = Files.newBufferedReader(fileIn)) {
            String line = "";
            String outputDirectory = "/Users/anonymous/Documents/databases/Dbpedia/output_fragments/";

            int fileCounter = 0;
            int triplesCounter = 0;

            Path output;
            BufferedWriter writer = null;

            List<String> prefixes = new ArrayList<String>();

            while((line = reader.readLine()) != null) {

                // take care of the prefixes
                if(line.contains("@prefix")) {
                    prefixes.add(line);
                    continue;
                }

                // in case of a comment
                if(line.startsWith("#")) {
                    continue;
                }

                // we need to clean the lines from unexpected characters
                // in general, we replace which is not printable - this is a little tranchant, but how to do otherwise?
                line = line.replaceAll("\\p{So}+", "?")
//						.replaceAll("\\p{C}", "?") // this creates a malformed input exception. I was not able to understand why
                        .replaceAll("\uF8FF", "?")
                        .replaceAll("\uE347", "?")
                        .replaceAll("\uE1E1", "?")
                        .replaceAll("\\*", "")
                        .replaceAll("\uF051", "?")
                        .replaceAll("\uF269", "?")
                        .replaceAll("\uE057", "?")
                        .replaceAll("[\uE000-\uF8FF]", "?");


                if(triplesCounter % 250000 == 0) {// each 100000 lines, create a new file
                    if(line.endsWith(";")) { // need to proceed until the next '.'
                        writer.write(line);
                        writer.newLine();
                        continue;
                    }
                    else if(line.endsWith(".") && writer!= null) {
                        //write the last line
                        writer.write(line);
                        line = null;
                    }

                    // if it exists, flush and close the previous writer
                    if(writer != null) {
                        writer.flush();
                        writer.close();
                    }

                    System.out.println("starting fragment number " + fileCounter);
                    // open a new Buffered Writer
                    output = Paths.get(outputDirectory + fileCounter + ".ttl");
                    fileCounter++;
                    writer = Files.newBufferedWriter(output);
                    //print the prefixes
                    for(String prefix : prefixes) {
                        writer.write(prefix);
                        writer.newLine();
                    }
                }

                if(line != null) {
                    writer.write(line);
                    writer.newLine();
                }
                triplesCounter++;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String inputFile = "/Users/anonymous/Documents/databases/Dbpedia/fragments/630.ttl";
        TestCleaningFile.splitDatasetFile(inputFile);
    }
}
