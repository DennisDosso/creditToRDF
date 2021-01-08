package it.unipd.dei.ims.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.NumberUtils;
import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.MyValues;
import it.unipd.dei.ims.data.RDB;

/** It is thought to extract from a relational databse all the triples with 
 * a certain quantity of credit, and name them in the correlated triple store
 * <p>
 * Step 4 - alternative to building a sub-set of triples.
 * */
public class ExtractTriplesFromRDBAndNameThemInATriplestore {

	private Connection connection;
	private RepositoryConnection repo;
	
	/** Query to select triples in the relational database that have credit bigger than 0 
	 * */
	private String SELECT_CREDITED_TRIPLES = "select t.subject , t.predicate , t.\"object\" \n" + 
			"from %s.triplestore t \n" + 
			"where credit > ?";
	
	/** Updates a triple by changing its named graph. 
	 * */
	private String SPARQL_UPDATE = "DELETE WHERE { <%s> <%s> %s}; "
			+ "INSERT DATA {"
				+ "GRAPH <%s> {"
				+ "<%s> <%s> %s }}";
	/* note this: even if we are not correctly deleting a triple because the object may be wrong (e.g. in the 
	 * case of long texts that were cropped in the RDB, we still are introducing a new triple in the new named graph.
	 * In this way we are always able to correctly answer to the SPARQL query, limiting ourselves with adding a limiting
	 * amount of triples. To solve this, you should use a general quantity of text in your RDB)*/
	

	/** builder. Creates a connetion to a relational database and to a triple store.
	 * Properties in rdb.properties and paths.properties
	 * */
	public ExtractTriplesFromRDBAndNameThemInATriplestore(){
		try {
			// builds connection to relational database database
			Map<String, String> pMap = PropertiesUtils.getPropertyMap("properties/rdb.properties");
			RDB.setup();
			String jdbcString = RDB.produceJdbcString();
			this.connection = ConnectionHandler.createConnection(jdbcString);

			// open connection to triplestore
			pMap = PropertiesUtils.getSinglePropertyFileMap("properties/paths.properties");
			String reduced_index_path = pMap.get("renaming.triple.store");
			this.repo = TripleStoreHandler.openRepositoryAndConnection(reduced_index_path);
			
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(RepositoryException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * 
	 * @param namedGraph the IRI identifying the named graph that will receive the triples with credit
	 * */
	public void extractTriplesFroRDFAndNameThemInTripleStore(int threshold, String namedGraph) throws SQLException {
		// asks to the relational db all the triples with a certain quantity of credit
		String qu = String.format(this.SELECT_CREDITED_TRIPLES, RDB.schema);
		PreparedStatement stmt = this.connection.prepareStatement(qu);
		stmt.setDouble(1, threshold);
		ResultSet r = stmt.executeQuery();
		
		int counter = 0;


		while (r.next()) {
			// get the values from the RDB. Prepare a new triple so we can rename it in the triple store
			String sub = r.getString(1);
			String pred = r.getString(2);
			String obj = r.getString(3);
			
			// the object can be many things. We need to format it properly
			UrlValidator urlValidator = new UrlValidator();
			if(urlValidator.isValid(obj)) { // the object is a URL
				obj = "<" + obj + ">";
			} else { // in case it is a literal
				if(NumberUtils.isInteger(obj)) { // check if it is a number
					obj = "\"" + obj + "\"^^xsd:integer";
				} else if(NumberUtils.isBSBMDate(obj)) {
					// in this case, it is a date
					obj = "\"" + obj + "\"^^xsd:date";
				} else {
					// in case it is a standard string
					obj = "\"" + obj + "\"@en";						
				} 
			}
			
			
			// format the query
			String updateQuery = String.format(this.SPARQL_UPDATE, sub, pred, obj, namedGraph, sub, pred, obj);
			
			// perform the update
			Update q = this.repo.prepareUpdate(QueryLanguage.SPARQL, updateQuery);
			q.execute();
			counter++;
			if(counter % 100 == 0) {
				System.out.println(counter + " triples updated");
				repo.commit();
			}
		}
		repo.commit();
		
	}

	public void close() {
		TripleStoreHandler.closeRepositoryAndConnextion();

		try {
			ConnectionHandler.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws SQLException {
		ExtractTriplesFromRDBAndNameThemInATriplestore execution = new ExtractTriplesFromRDBAndNameThemInATriplestore();
		
		MyValues.setup();// read properties file
		execution.extractTriplesFroRDFAndNameThemInTripleStore(MyValues.creditThreshold, MyValues.namedGraph);
		
		execution.close();
		System.out.println("done");
	}
}
