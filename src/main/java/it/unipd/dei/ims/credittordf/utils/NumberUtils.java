package it.unipd.dei.ims.credittordf.utils;

import java.util.regex.Pattern;

/** Contains methods useful with strings and RDF in general
 * (the truth is I did not know where else to put these methods)
 * */
public class NumberUtils {

	/** code produly copied from stackoverlof at https://stackoverflow.com/questions/237159/whats-the-best-way-to-check-if-a-string-represents-an-integer-in-java
	 * used to understand if a string is an integer without raising those nasty exceptions. */
	public static boolean isInteger(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
	
	/** checks if the given string is a date, following the format
	 * required by BSBM, i.e. YYYY-MM-DDT00:00:00*/
	public static boolean isBSBMDate(String supposedDate) {
		String regex = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d";
		
		return Pattern.matches(regex, supposedDate);
	}

}
