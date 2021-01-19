package it.unipd.dei.ims.data;

public class RDBQueries {

	/** The %s needs to be substituted with a schema 
	 * */
	public static String insert_triple = "INSERT INTO %s.triplestore(\n" + 
			"	subject, predicate, object, credit)\n" + 
			"	VALUES (?, ?, ?, ?);";
	
	public static String insert_triple_with_hits = "INSERT INTO %s.triplestore(\n" + 
			"	subject, predicate, object)\n" + 
			"	VALUES (?, ?, ?);";
}
