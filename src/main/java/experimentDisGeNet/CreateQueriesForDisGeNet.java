package experimentDisGeNet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;

/**
 * 
 * properties to set:
 * <li> query.input.file: input file with the values of the query to use
 * <li> query.values.file (paths.properties): where to save the produced queries
 * <li> class (values.properties): to set the class we are dealing with 
 * <li> how_many_queries (values.properties): the number of queries that we want to print
 * 
 * 
 * */
public class CreateQueriesForDisGeNet {

	public CreateQueriesForDisGeNet() {
		MyValues.setup();
		MyPaths.setup();
	}


	/** 
	 * @param filePath path of the input file
	 * @param output_path where to write the queries
	 * @param filePath path of the file containing different values for the queries
	 * @throws IOException 
	 * */
	public void createQueriesForOneClass(String filePath, String output_path, int number_of_queries, MyValues.QueryClass query_class) throws IOException {
		// first, read the file containing the different queries
		Path in = Paths.get(filePath);
		List<String> queryList = new ArrayList<String>();

		try(BufferedReader reader = Files.newBufferedReader(in)) {
			// now let us read the lines
			reader.readLine(); // the first line is a header, we just read and discard it

			String line = "";

			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");

				// now, given the class of query, we prepare the queries
				String query = this.buildQueryValues(query_class, parts);
				queryList.add(query);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// shuffle a little bit the list
		Collections.shuffle(queryList);

		double stdv = Math.max(1, (double) queryList.size()/MyValues.standardDeviationRatio);
		NormalDistribution distribution = new NormalDistribution(queryList.size()/2, stdv);

		// prepare the writer
		FileWriter w = new FileWriter(output_path, true);
		BufferedWriter writer = new BufferedWriter(w); 
		PrintWriter pw = new PrintWriter(writer);

		// the header tells the class
		pw.println("epoch," + query_class);


		// now print the queries randomly
		for(int i = 0; i < number_of_queries; ++i) {
			// decide which query to write
			int randomNum =  (int) Math.floor(distribution.sample());
			// need to be sure to have a number that can be used
			while(randomNum < 0 || randomNum > queryList.size() - 1)
				randomNum = (int) Math.floor(distribution.sample());

			// get the values
			String q = queryList.get(randomNum);
			pw.println(q);
		}
		// flush and close
		pw.flush();
		pw.close();
		System.out.println("printed one class of queries");
	}

	private String buildQueryValues(MyValues.QueryClass query_class, String[] parts) {
		String toReturn = "";
		if(query_class == MyValues.QueryClass.DGNQUERY1) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY2) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY3) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY4) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY5) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY6) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY7) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY8) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY9) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY10) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY11) {
			toReturn = parts[0] + "," + query_class;
		} else if(query_class == MyValues.QueryClass.DGNQUERY12) {
			toReturn = parts[0] + "," + query_class;
		}

		return toReturn.replaceAll("\"", "");

	}

	public static void main(String[] args) throws IOException {
		CreateQueriesForDisGeNet execution = new CreateQueriesForDisGeNet();
		execution.createQueriesForOneClass(MyPaths.queryInputFile, MyPaths.queryValuesFile, MyValues.how_many_queries, MyValues.QUERYCLASS);
		
		System.out.println("done");
	}
}
