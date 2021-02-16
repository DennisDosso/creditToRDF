package experiment1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;

/** It seems that rdf4j requires a lot of time (too much) to import a database of 100M
 * triples. I tried to create this class to take and split a file in parts, so to
 * be easier to import it into a triplestore.
 * <p>
 * properties to set:
 * in path.properties
 * <li> text_rdf_file
 * <li>  fragmentsOutputDirectory
 * 
 * <p>
 * Phase -2
 * */
public class SplitDatasetFile {
	
	public SplitDatasetFile() {
		MyPaths.setup();
		MyValues.setup();
	}
	
	public void splitDatasetFile() {
		Path fileIn = Paths.get(MyPaths.text_rdf_file);
		try(BufferedReader reader = Files.newBufferedReader(fileIn)) {
			String line = "";
			String outputDirectory = MyPaths.fragmentsOutputDirectory;
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
				
				
				if(triplesCounter % 250000 == 0) {// each 100000 lines, create a new file
					if(line.endsWith(";")) { // need to proceed until the next '.'
						writer.write(line);
						writer.newLine();
						continue;
					} 
					else if(line.endsWith(".")) {
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
		SplitDatasetFile execution = new SplitDatasetFile();
		execution.splitDatasetFile();
		
		System.out.println("done");
	}
}
