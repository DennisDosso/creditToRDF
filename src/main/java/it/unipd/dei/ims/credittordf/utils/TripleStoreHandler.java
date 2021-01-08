package it.unipd.dei.ims.credittordf.utils;

import java.io.File;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

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
		repository = new SailRepository(new NativeStore(dataDir));
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
		File dataDir = new File(path);
		repository = new SailRepository(new NativeStore(dataDir));
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
	

}
