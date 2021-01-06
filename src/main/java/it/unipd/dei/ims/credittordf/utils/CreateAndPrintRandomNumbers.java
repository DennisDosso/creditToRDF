package it.unipd.dei.ims.credittordf.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.distribution.NormalDistribution;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;

/** Methods to create and print random numbers, so that
 * I can simulate a rate of utilization of queries */
public class CreateAndPrintRandomNumbers {

	public static void printRandomNumbers(String outputFile, int howManyNumbers, int maxNumberOfTimes) {
		Path p = Paths.get(outputFile);


		try(BufferedWriter writer = Files.newBufferedWriter(p, StandardCharsets.UTF_8)){
			for(int i = 0; i < howManyNumbers; ++i) {
				int randomNum = ThreadLocalRandom.current().nextInt(0, maxNumberOfTimes + 1);
				writer.write(randomNum + "");
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void printNumbersNormalDistribution(String outputFile, int howManyNumbers, int maxNumberOfTimes) {
		NormalDistribution distribution = new NormalDistribution(50, 18);
		Path p = Paths.get(outputFile);
		try(BufferedWriter writer = Files.newBufferedWriter(p, StandardCharsets.UTF_8)){
			for(int i = 0; i < howManyNumbers; ++i) {
				int randomNum =  (int) Math.floor(distribution.sample());
				if(randomNum < 0)
					randomNum = 0;
				writer.write(randomNum + "");
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public static void main(String[] args) {

//		CreateAndPrintRandomNumbers.printRandomNumbers(MyPaths.times_file_path, MyValues.how_many_queries, MyValues.max_times_per_one_query);
		CreateAndPrintRandomNumbers.printNumbersNormalDistribution(MyPaths.times_file_path_normal_distribution, MyValues.how_many_queries, MyValues.max_times_per_one_query);
		System.out.println("done");
	}
}
