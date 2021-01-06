package it.unipd.dei.ims.database;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.NumberUtils;
import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.RDB;

/** Given a relational database where the column triplestore presents some credit, 
 * extract the triples that have credit bigger than a certain thresholds, and builds a 
 * new triple store
 * 
 * Step 4 - once the credit is distributed, extract the triples
 * */
public class ExtractTriplesFromRDBAndBuildTripleStore {

	private Connection connection;
	private RepositoryConnection repo;
	

	/** Query to select triples in the relational database that have credit bigger than 0 
	 * */
	private String SELECT_CREDITED_TRIPLES = "select t.subject , t.predicate , t.\"object\" \n" + 
			"from triplestore t \n" + 
			"where credit > ?";
	
	
	/** Prepares the connection to the relational databases and the connection to the reduced triple-store. 
	 * 
	 * Used property files:
	 * <p>
	 * <ul>
	 * <li> main.properties - database connection </li>
	 * <li> opath.properties - reduced.index.path for the path of the directory where to save the triple store</li>
	 * </ul>
	 * */
	public ExtractTriplesFromRDBAndBuildTripleStore() {
		try {
			// builds connection to relational database database
			Map<String, String> pMap = PropertiesUtils.getPropertyMap("properties/rdb.properties");
			RDB.setup();
			String jdbcString = RDB.produceJdbcString();
			this.connection = ConnectionHandler.createConnection(jdbcString);
			
			// open connection to triplestore
			pMap = PropertiesUtils.getSinglePropertyFileMap("properties/paths.properties");
			String reduced_index_path = pMap.get("reduced.index.path");
			this.repo = TripleStoreHandler.openRepositoryAndConnection(reduced_index_path);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(RepositoryException e2) {
			e2.printStackTrace();
		}
	}
	
	public void close() {
		TripleStoreHandler.closeRepositoryAndConnextion();

		try {
			ConnectionHandler.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**Given the connection to the relational database of the object invoking this method,
	 * and the path of the triple store also associated to the instance calling this method,
	 * extracts triples from the RDB database with credit above a certain threshold, and uses them to 
	 * build the reduced triple-store.
	 * 
	 * @param threshold minimun quantity of credit a triple needs to have (it is a > relationship, value excluded).
	 * If you put 0 as value, you get all the triples that have at least something as value.  
	 * */
	public void buildTripleStoreFromCreditedTriples(double threshold) {
		try {
			// asks to the relational db all the triples with a certain quantity of credit
			PreparedStatement stmt = this.connection.prepareStatement(this.SELECT_CREDITED_TRIPLES);
			stmt.setDouble(1, threshold);
			ResultSet r = stmt.executeQuery();
			
			// prepare an rdf model in-memory
			ModelBuilder builder = new ModelBuilder().setNamespace("q1", "http://bsbm.query1.it/");
			Model model;
			int counter = 0;
			
			while (r.next()) {
				// take all the triples
				String sub = r.getString(1);
				String pred = r.getString(2);
				String obj = r.getString(3);
				
				UrlValidator urlValidator = new UrlValidator();
				if(urlValidator.isValid(obj)) { // in case the object is a resource
					IRI o = iri(obj);					
					builder.subject(sub).add(pred, o);
				} else { // in case it is a literal
					if(NumberUtils.isInteger(obj)) {
						//in case the value is an integer literal
						builder.subject(sub).add(pred, Integer.parseInt(obj));
					} else {
						// in case it is a standard string
						builder.subject(sub).add(pred, obj);						
					} 
				}
				
				
				counter++;
				if(counter % 100 == 0) {
					model = builder.build();
					this.repo.add(model);
					this.repo.commit();
					
					//clear the builder
					builder = new ModelBuilder().setNamespace("q1", "http://bsbm.query1.it/");
				}
			}
			// deal with the triples
			model = builder.build();
			this.repo.add(model);
			
			this.repo.commit();
			
		} catch (SQLException e) {
			System.out.println("Error producing the select query");
			e.printStackTrace();
		}
		
	}
	
	
	/** Test main */
	public static void main(String[] args) {
		ExtractTriplesFromRDBAndBuildTripleStore execution = new ExtractTriplesFromRDBAndBuildTripleStore();
		execution.buildTripleStoreFromCreditedTriples(0);
		
		execution.close();
	}

}
