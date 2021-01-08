package it.unipd.dei.ims.data;

import java.util.Map;

import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;

/** Contains values to connect to a relational database*/
public class RDB {

//	jdbc.connection.string=jdbc:postgresql://localhost:5432/KS?user=postgres&password=Ulisse92
	public static String host = "localhost";
	public static String port = "5432";
	public static String user = "postgres";
	public static String database = "bsbm100k";
	public static String password = "Ulisse92";
	/** schema of the database that we are using (so you can switch between different triple stores and dimensions of the table). Default at public*/
	public static String schema = "public";
	
	public static String produceJdbcString() {
		return "jdbc:postgresql://" + RDB.host + ":" + RDB.port + "/" + RDB.database + "?user=" + RDB.user + "&password=" + RDB.password; 
	}
	
	/** Prepares the values using the file rdb.properties
	 * */
	public static void setup() {
		Map<String, String> map = PropertiesUtils.getPropertyMap("properties/rdb.properties");
		host = map.get("host");
		port = map.get("port");
		user= map.get("user");
		database = map.get("database");
		password = map.get("password");
		schema = map.get("schema");
	}
}
