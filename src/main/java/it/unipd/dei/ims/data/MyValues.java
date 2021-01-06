package it.unipd.dei.ims.data;

import java.util.Map;

import it.unipd.dei.ims.credit.distribution.BSBMCreditDistributor;
import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;

public class MyValues {

	/** number of different queries for each class*/
	public static int how_many_queries = 10000;
	
	/** Maximum number of times a query has been performed*/
	public static int max_times_per_one_query = 300;
	
	/** Class of queries we are using. Change between ONE, TWO, THREE etc. */
	public static BSBMCreditDistributor.QueryClass QUERYCLASS = BSBMCreditDistributor.QueryClass.ONE;
	
	public MyValues() {
		Map<String, String> map = PropertiesUtils.getPropertyMap("properties/values.properties");
		
		String class_ = map.get("class");
		if(class_.equals("1"))
			QUERYCLASS = BSBMCreditDistributor.QueryClass.ONE;
		
		how_many_queries = Integer.parseInt(map.get("how_many_queries"));
		max_times_per_one_query = Integer.parseInt(map.get("max_times_per_one_query"));
	}
}
