package it.unipd.dei.ims.data;

import java.util.Map;

import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;

public class MyPaths {

	public static String index_path = "/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/bsbm100k";
//	public static String index_path = "/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/bsbm100kreduced";
	
	public static String reduced_index_path = "/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/bsbm100kreduced";
	
	/** path of the file with the values for the query*/
	public static String values_path = "/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/random values for queries/1/random_values_for_query_1.csv";
	
	
	/** the times file described how many times each query is executed
	 * */
	public static String times_file_path = "/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/random values for queries/1/times_uniform.txt";
	public static String times_file_path_normal_distribution = "/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/random values for queries/1/times_normal.txt";
	
	/** Path for an index that you are querying
	 * */
	public static String querying_index = "/Users/dennisdosso/MEGAsync/Ricerca/progetti_di_ricerca/CreditToRDF/bsbm100k";
	
	public MyPaths() {
		Map<String, String> map = PropertiesUtils.getPropertyMap("properties/paths.properties");
		
		index_path = map.get("index_path");
		reduced_index_path = map.get("reduced.index.path");
		values_path = map.get("values_path");
		times_file_path = map.get("times_file_path");
		times_file_path_normal_distribution = map.get("times_file_path_normal_distribution");
		querying_index = map.get("querying.index");
	}
}
