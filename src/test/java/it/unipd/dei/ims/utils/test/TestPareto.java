package it.unipd.dei.ims.utils.test;

import org.apache.commons.math3.distribution.ParetoDistribution;

public class TestPareto {

	public static void main(String[] args) {
		ParetoDistribution distribution = new ParetoDistribution(1, 4);
		
		for(int i = 0; i < 100; ++i) {
			int randomNum =  (int) Math.floor(distribution.sample());
			System.out.println(randomNum);
		}
	}
}
