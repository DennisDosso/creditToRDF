package it.unipd.dei.ims.credittordf.utils;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;

/** A class to handle the cache. 
 * it contains the cahce itself, and takes care to upload it. 
 * */
public class CacheHandler {

	/** in-memory graph */
	public Model graph;
	
	private Repository cache;
	
	/** in-memory repository connection to the RDF graph*/
	public RepositoryConnection cacheConnection; 
	
	private String SELECT_CREDITED_TRIPLES = "select t.subject , t.predicate , t.\"object\" \n" + 
			"from %s.triplestore t \n" + 
			"where credit > ?";
	
	public CacheHandler() {
		// build the in-memory database
		this.cache = new SailRepository(new MemoryStore());
		cache.init();
		// and get its repository connection
		this.cacheConnection = cache.getConnection();
	}
	
	public void close() {
		this.cacheConnection.close();
		this.cache.shutDown();
	}
	
	/** Given a credit threshold, it uses the relational database to update the cache with the triples.
	 * <p>
	 * Each time this method is called, the cache is cleared and built from scratch.
	 * */
	public ReturnBox updateCacheUsingThreshold(double threshold) {
		ReturnBox box = new ReturnBox();
		
		// clear the whole cache
		this.cacheConnection.clear();
		
		// take the time required to update the cache
		long n = System.currentTimeMillis();
		try {
			// asks to the relational db all the triples with a certain quantity of credit
			String qu = String.format(this.SELECT_CREDITED_TRIPLES, RDB.schema);
			// use the support relational database
			PreparedStatement stmt = ConnectionHandler.getConnection().prepareStatement(qu);
			stmt.setDouble(1, threshold);
			ResultSet r = stmt.executeQuery();

			// prepare an rdf model in-memory
			ModelBuilder builder = new ModelBuilder().setNamespace("n", MyValues.namedGraph);
			ValueFactory vf = SimpleValueFactory.getInstance();

			while (r.next()) {
				// for each triple, we insert it in the cache
				String sub = r.getString(1);
				String pred = r.getString(2);
				String obj = r.getString(3);

				UrlValidator urlValidator = new UrlValidator();
				if(urlValidator.isValid(obj)) { // in case the object is a resource
					IRI o = iri(obj);					
					builder.subject(sub).add(pred, o);
				} else { // in case it is a literal
					// we first need to understand what type of object this is. Once understood that,
					// we can insert it
					String insertingObj = TripleStoreHandler.stripObjectFromDatatypes(obj);
					if(NumberUtils.isInteger(insertingObj)) {
						//in this case, it is an integer
						builder.subject(sub).add(pred, Integer.parseInt(insertingObj));
					} else if(NumberUtils.isBSBMDate(insertingObj)) {
						// in this case, it is a data
						builder.subject(sub).add(pred, vf.createLiteral(insertingObj, XSD.DATETIME));
					} else {
						// in case it is a standard string
						if(obj.contains("@")) {
							// insert it with the language tag
							builder.subject(sub).add(pred, vf.createLiteral(insertingObj, obj.split("@")[1]));								
						} else {
							// it is a literal without tag
							builder.subject(sub).add(pred, insertingObj);
						}
					} 
				}


			}
			// create a graph
			this.graph = builder.build();
			
			
			// add this graph to our cache
			this.cacheConnection.add(graph);
			
			long elapsed = System.currentTimeMillis() - n;
			box.nanoTime  = elapsed;
			box.size = this.graph.size();
			
			// close the jdbc statement
			r.close();

			
		} catch (SQLException e) {
			System.out.println("Error producing the select query");
			e.printStackTrace();
		}
		return box;
		
	}
}
