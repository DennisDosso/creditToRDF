package it.unipd.dei.ims.data;

public class RDBQueries {

	public static String insert_triple = "INSERT INTO public.triplestore(\n" + 
			"	subject, predicate, object, credit)\n" + 
			"	VALUES (?, ?, ?, ?);";
}
