package it.unipd.dei.ims.credittordf.utils;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.File;

import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import it.unipd.dei.ims.data.MyValues;

public class TripleStoreHandler {

	/** Triple store repository */
	private static Repository repository = null;
	private static RepositoryConnection connection = null;
	private static boolean initialized = false;
	
	
	/** 
	 * 
	 * @param path path of the repository*/
	public static void initRepository(String path) {
		File dataDir = new File(path);
		repository = new SailRepository(new NativeStore(dataDir, MyValues.indexString));
		repository.init();
		initialized = true;
	}
	
	public static void initConnection() {
		if(initialized) 
			connection = repository.getConnection();
	}
	
	
	public static void shutDownRepository() {
		if(repository != null) {
			repository.shutDown();
		}
	}
	
	public static void closeRepositoryConnection() {
		connection.close();
	}
	
	public static Repository getRepository() {
		return repository;
	}
	
	public static RepositoryConnection getRepositoryConnection() {
		return connection;
	}
	
	public static RepositoryConnection openRepositoryAndConnection(String path) {
		if(connection!=null)
			return connection;
		
		MyValues.setup();
		File dataDir = new File(path);
		repository = new SailRepository(new NativeStore(dataDir, MyValues.indexString));
		repository.init();
		connection = repository.getConnection();
		initialized = true;
		return connection;
	}
	
	public static void closeRepositoryAndConnextion() {
		if(connection!= null && connection.isOpen())
			connection.close();
		if(repository != null)
			repository.shutDown();
	}
	
	
	/** Given a string, it deals with its nature to prepare it to be inserted in a triple store. 
	 * i.e. ir recognizes if it is a URL, ora a literal, and its type
	 * */
	public static String stringAdapter(String obj) {
		
		ValueFactory vf = SimpleValueFactory.getInstance();
		
		UrlValidator urlValidator = new UrlValidator();
		if(urlValidator.isValid(obj)) { // in case the object is a resource
//			IRI o = iri(obj);
			return "<" + obj + ">";
		} else { // in case it is a literal
			if(NumberUtils.isInteger(obj)) {
				//in this case, it is an integer
				obj = vf.createLiteral(obj, XSD.INT).toString();
			} else if(NumberUtils.isBSBMDate(obj)) {
				// in this case, it is a data
				obj = vf.createLiteral(obj, XSD.DATETIME).toString();
			} else {
				// in case it is a standard string, add it with an english tag
//				obj = vf.createLiteral(obj, "en").toString();
				obj = vf.createLiteral(obj).toString();
			} 
		}
		
		return obj;
	}
	
	/** Given an object string, obtained from a .toString() method, that may for example
	 * be in the form "something"@en if string Litera, "114"^^xsd:int if integer, 
	 * or a url, checks if it is a url. If it is a URL, it returns it surrounded by <...>,
	 * ready to be used for example in a SPARQL query. Otherwise, it returns it as it is*/
	public static String prepareObjectStringForQuery(String obj) {
		UrlValidator urlValidator = new UrlValidator();
		if(urlValidator.isValid(obj)) { // in case the object is a resource
			return "<" + obj + ">";
		}
		else
			return obj;
	}
	
	public static String stripObjectFromDatatypes(String obj) {
		String ret = "";
		// a literal with a language tag
		if(obj.contains("@")) {
			return obj.split("@")[0].replace("\"", "");
		}// an integer
		else if(obj.contains("^^")) {
			return obj.split("\\^\\^")[0].replace("\"", "");
		}
		
		// a literal without language tag
		return obj.replace("\"", "");
	}
	

}
