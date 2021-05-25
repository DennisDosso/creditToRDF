package it.unipd.dei.ims.credittordf.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import org.apache.commons.validator.routines.UrlValidator;

/** It seems that rdf4j requires a lot of time (too much) to import a database of 100M
 * triples. I tried to create this class to take and split a file in parts, so to
 * be easier to import it into a triplestore.
 * <p>
 * properties to set:
 * in path.properties
 * <li> rdf_files_directory: the directory where to take the files to split
 * <li>  fragmentsOutputDirectory
 * 
 * <p>
 * Command to execute:
 * <code>
 *
 * </code>
 * */
public class SplitDatasetFile {

	private static int fileCounter = 0;

	public SplitDatasetFile() {
		MyPaths.setup();
		MyValues.setup();
	}

	/** We suppose that all files are included in one directory. No subdirectories
	 *
	 * */
	public void splitMultipleFiles(String directoryPath, String outputDirectory) {
		// first, read all the files in the directory
		File folder = new File(directoryPath);
		for( File fileEntry : Objects.requireNonNull(folder.listFiles())) {
			if(fileEntry.isFile() && !fileEntry.getName().contains(".DS_Store")) {
				System.out.println("Splitting with file: " + fileEntry.getName());
				//split this file
				this.splitDatasetFile(fileEntry.getAbsolutePath(), outputDirectory);
			}
		}
	}

	/** Splits one database file and puts the splitted resulted files
	 * in one directory.
	 * 
	 * */
	public void splitDatasetFile(String path, String outputDirectory) {
		Path fileIn = Paths.get(path);
		String line = "";
		try(BufferedReader reader = Files.newBufferedReader(fileIn)) {

			int triplesCounter = 0;

			Path output;
			BufferedWriter writer = null;

			List<String> prefixes;
			prefixes = new ArrayList<String>();

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
//						.replaceAll("[\uFFF0-\uFFFF]", "?")
						.replaceAll("www:", "www.")
						.replaceAll("[\uE000-\uFFFF]", "?");


				// THIS FRAGMENT OF CODE
				// was used with external_links file. That file caused a lot of problems.
				// since it is not used in the queries, we removed it.


//				// now take the urls
//				String[] parts = line.split(" ");
//				// validate these URLs
//				String[] schemes = {"http","https"};
//				UrlValidator urlValidator = new UrlValidator(schemes);
//				String newLine = "";
//				for(int i = 0; i < 3; ++i) {
//					String url = parts[i].replaceAll("<|>", "");
//					// decode this url if
//
//					if(url.startsWith("http") || url.startsWith("ftp")) { // a url
//						// remove characters from this url that are not allowed
//						url = url.replaceAll("\\[", "_");
//						// make sure that people did not mess when writing this URL
////						url = java.net.URLEncoder.encode(url, StandardCharsets.UTF_8);
//						if(i < 2)
//							newLine += "<" + url + "> ";
//						else
//							newLine += "<" + url + "> .";
//					} else {
//						// a simple literal
//						if(i < 2)
//							newLine += url + " ";
//						else
//							newLine +=  url + " .";
//					}
//				}

//				line = newLine;

				if(triplesCounter % 250000 == 0) {// each 250000 lines, create a new file
					if(line.endsWith(";")) { // need to proceed until the next '.'
						writer.write(line);
						writer.newLine();
						continue;
					} 
					else if(line.endsWith(".") && writer != null) {
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
					output = Paths.get(outputDirectory + "/" + fileCounter + ".ttl");
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
			} // finished reading lines

			// before finishing with this file, make sure to correctly flush the writer
			if(writer != null) {
				writer.flush();
				writer.close();
			}


		} catch (IOException e) {
			System.out.println("we had a problem with this line I suppose: " + line);
			e.printStackTrace();
		}
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
		// directory with the input files
		String inputDirectory = MyPaths.rdf_files_directory;
		// directory where to write the fragments
		String output_directory = MyPaths.fragmentsOutputDirectory;
		
		// split the files
		execution.splitMultipleFiles(inputDirectory, output_directory);

		System.out.println("done");
	}
}
