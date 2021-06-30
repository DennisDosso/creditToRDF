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
	
	/**
	 * 
	 * 
	 * @param mainDir directory in input containing the necessary files in subdirectories
	 * @param output_path where to save the mixed query plan
	 * */
	public void createAMixedQueryPlan(String mainDir, String output_path, int number_of_queries) throws IOException {
		// DisGeNet has 12 classes, so I hardcoded this value. I am a bad bad man
		int how_many_classes_are_there = 12;
		// each class will be equally represented
		int times_one_class_is_built = (int) number_of_queries / how_many_classes_are_there;
		ArrayList<String> mainQueryList = new ArrayList<String>();
		
		for(int i = 0; i < how_many_classes_are_there; ++i) {
			// open the file with the queries
			Path in = Paths.get(mainDir + "/" + (i+1) + "/query_values.csv");
			
			ArrayList<String> queryList = new ArrayList<String>();
			MyValues.QueryClass query_class = null;
			
			if(i == 0) query_class = MyValues.QueryClass.DGNQUERY1;
			else if (i == 1) query_class = MyValues.QueryClass.DGNQUERY2;
			else if (i == 2) query_class = MyValues.QueryClass.DGNQUERY3;
			else if (i == 3) query_class = MyValues.QueryClass.DGNQUERY4;
			else if (i == 4) query_class = MyValues.QueryClass.DGNQUERY5;
			else if (i == 5) query_class = MyValues.QueryClass.DGNQUERY6;
			else if (i == 6) query_class = MyValues.QueryClass.DGNQUERY7;
			else if (i == 7) query_class = MyValues.QueryClass.DGNQUERY8;
			else if (i == 8) query_class = MyValues.QueryClass.DGNQUERY9;
			else if (i == 9) query_class = MyValues.QueryClass.DGNQUERY10;
			else if (i == 10) query_class = MyValues.QueryClass.DGNQUERY11;
			else if (i == 11) query_class = MyValues.QueryClass.DGNQUERY12;
			
			
			// read the values building this query
			try(BufferedReader reader = Files.newBufferedReader(in)) {
				// now let us read the lines
				reader.readLine(); // the first line is a header, we just read and discard it

				String line = "";

				while((line = reader.readLine()) != null) {
					String[] parts = line.split(",");

					// now, given the class of query, we prepare the queries
					String query = this.buildQueryValues(query_class, parts);
					queryList.add(query);
				} // read all the queries of a class
				Collections.shuffle(queryList);
				
				// add a part of the queryList to the set of all queries considered
				int share = Math.min(times_one_class_is_built, queryList.size());
				mainQueryList.addAll(queryList.subList(0, share));
			}
		} // end of the iteration over all classes
		
		System.out.println("finished asking queries, now printing them");
		// everyday I'm shufflin'
		Collections.shuffle(mainQueryList);
		

		// get a normal distribution to choose the queries to print
		NormalDistribution distribution = new NormalDistribution(mainQueryList.size()/2, mainQueryList.size()/MyValues.standardDeviationRatio);

		// open the writer in append
		FileWriter w = new FileWriter(output_path, true);
		BufferedWriter writer = new BufferedWriter(w); 
		PrintWriter pw = new PrintWriter(writer);

		// tell that one year has passed
		pw.println("epoch, variable");

		// now write the single values
		for(int i = 0; i < number_of_queries; ++i) {
			// decide which query to write
			int randomNum =  (int) Math.floor(distribution.sample());
			// need to be sure to have a number that can be used
			while(randomNum < 0 || randomNum > mainQueryList.size() - 1)
				randomNum = (int) Math.floor(distribution.sample());

			// get the values
			String q = mainQueryList.get(randomNum);
			pw.println(q);
				
		}
		// flush and close
		pw.flush();
		pw.close();
		
	}

	public static void main(String[] args) throws IOException {
		CreateQueriesForDisGeNet execution = new CreateQueriesForDisGeNet();
//		execution.createQueriesForOneClass(MyPaths.queryInputFile, MyPaths.queryValuesFile, MyValues.how_many_queries, MyValues.QUERYCLASS);
		
		String inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/1/query_values.csv";
		String output = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/query_plan_uniform_alpha_18.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY1);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/2/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY2);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/3/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY3);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/4/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY4);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/5/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY5);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/6/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY6);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/7/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY7);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/8/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY8);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/9/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY9);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/10/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY10);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/11/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY11);
		inputPath = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/12/query_values.csv";
		execution.createQueriesForOneClass(inputPath, output, MyValues.how_many_queries, MyValues.QueryClass.DGNQUERY12);
		
		
		String mainDir = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet";
		output = "/Users/anonymous/Documents/CreditToRDF/query_plans/DisGeNet/query_plan_mixed_alpha_18.csv";
		execution.createAMixedQueryPlan(mainDir, output, 2000);
		
		System.out.println("done");
	}
}
