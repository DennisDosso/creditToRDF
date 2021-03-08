package disgenetqueries;

public class DisGeNetQuery12 {

	public static String select = DisGeNetQuery1.prefixes +
			"SELECT * "
			+ "WHERE {"
			+ " %s a so:0001060 ; "
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal;"
			+ " sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "OPTIONAL { ?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal . }"
			+ "} "
			+ "LIMIT 20";
	
	
	public static String select_named = DisGeNetQuery1.prefixes +
			"SELECT * "
			+ "WHERE { GRAPH %here {"
			+ " %s a so:0001060 ; "
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal;"
			+ " sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "OPTIONAL { ?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal . }"
			+ "} }"
			+ "LIMIT 20";
	
	public static String construct = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ " %s a so:0001060 ; " // 1
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal;"
			+ " sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal ."
			+ "} "
			+ "WHERE {"
			+ " %s a so:0001060 ; " // 2
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "OPTIONAL { ?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal . }"
			+ "} "
			+ "LIMIT 200";
}
