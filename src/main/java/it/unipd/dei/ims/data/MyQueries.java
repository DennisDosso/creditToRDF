package it.unipd.dei.ims.data;

import java.util.Map;

import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;

public class MyQueries {

	public static String tupleQuery;
	
	public static void setup() {
		Map<String, String> map = PropertiesUtils.getPropertyMap("properties/queries.properties");
		
		tupleQuery = map.get("tuple.query");
		
		
	}
}
