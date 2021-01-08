package it.unipd.dei.ims.utils.test;

import it.unipd.dei.ims.credittordf.utils.NumberUtils;

public class TestRegex {

	public static void main(String[] args) {
		String date = "proprio no";
		date = "2007-09-02T00:00:00";
		System.out.println(NumberUtils.isBSBMDate(date));
	}

}
