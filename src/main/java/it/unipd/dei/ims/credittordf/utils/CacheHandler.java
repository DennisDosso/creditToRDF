package it.unipd.dei.ims.credittordf.utils;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.unipd.dei.ims.credittordf.dbpedia.cachewithcap.CacheSupport;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;
import it.unipd.dei.ims.data.ReturnBox;


/** A class to handle the cache. 
 * it contains the cache itself, and takes care to upload it.
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
			UrlValidator urlValidator = new UrlValidator();

			while (r.next()) {
				// for each triple, we insert it in the cache
				String sub = r.getString(1);
				String pred = r.getString(2);
				String obj = r.getString(3);

				if(!urlValidator.isValid(sub))
					sub = "http://dbpedia.org/node/" + sub;

				if(!urlValidator.isValid(pred))
					sub = "http://dbpedia.org/property/" + pred;

				// now adding triples to the cache
				if(urlValidator.isValid(obj)) {
					try{
						IRI o = iri(obj);
						builder.subject(sub).add(pred, o);
					} catch(IllegalArgumentException iae) {
						System.err.println("Raised illegal argument exception with triple "
								+ sub + " " + pred + " " + obj);
					} catch (ModelException e) {
						System.err.println("Raised model exception with triple "
								+ sub + " " + pred + " " + obj);
					}
				} else {
					// the could be a literal, or a "broken" IRI, one of those of Dbpedia
					//first let's try to see what we got
					String[] parts = CacheSupport.striplObjectFromDatatypes(obj);
					if(parts == null) {
						try{
							// the object is a broken IRI since we were unable to find a datatype
							builder.subject(sub).add(pred, obj);
						} catch (ModelException e) {
							// we do not do anything, this triple is simply lost
							System.err.println("Raised model exception with triple "
									+ sub + " " + pred + " " + obj);
						}
					} else {
						// it is some form of literal
						try{
							if(parts[2].equals("@"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], parts[1]));

							if(parts[2].equals("integer"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.INTEGER));
							if(parts[2].equals("double"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.DOUBLE));
							if(parts[2].equals("float"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.FLOAT));
							if(parts[2].equals("date"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.DATE));
							if(parts[2].equals("dateTime"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.DATETIME));
							if(parts[2].equals("nonNegativeInteger"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.NON_NEGATIVE_INTEGER));
							if(parts[2].equals("gYear"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.GYEAR));
							if(parts[2].equals("gMonthDay"))
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], XSD.GMONTHDAY));
							if(parts[2].equals("custom")) {// custom datatype from dbpedia
								CustomURI u = new CustomURI(parts[3], parts[4]);
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], u));
							}
							if(parts[2].equals("XMLSchema")) {
								CustomURI u = new CustomURI(parts[3], parts[4]);
								builder.subject(sub).add(pred, vf.createLiteral(parts[0], u));
							}
							if(parts[2].equals("unknown")) {
								System.out.println("[WARNING] this triple has a special datatype, thus is added " +
										"as simple literal: " +
										sub + " " + pred + " " + obj);
								builder.subject(sub).add(pred, obj.replaceAll("\"", ""));
							}
						} catch(ModelException e) {
							System.err.println("error in inserting literal " + obj + " inserting it as general literal");
							try{
								builder.subject(sub).add(pred, obj.replaceAll("\"", ""));
							} catch (Exception e2) {
								System.err.println("Truly impossible to import " + sub + " " + pred + " " + obj);
								e.printStackTrace();
								e2.printStackTrace();
							}
						}
					}

				}
			}// end while
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

	public Model getGraph() {
		return graph;
	}

	public void setGraph(Model graph) {
		this.graph = graph;
	}

	public Repository getCache() {
		return cache;
	}

	public void setCache(Repository cache) {
		this.cache = cache;
	}

	public RepositoryConnection getCacheConnection() {
		return cacheConnection;
	}

	public void setCacheConnection(RepositoryConnection cacheConnection) {
		this.cacheConnection = cacheConnection;
	}
}
