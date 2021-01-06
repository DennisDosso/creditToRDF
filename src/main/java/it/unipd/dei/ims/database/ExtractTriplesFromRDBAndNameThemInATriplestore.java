package it.unipd.dei.ims.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import it.unipd.dei.ims.credittordf.utils.ConnectionHandler;
import it.unipd.dei.ims.credittordf.utils.PropertiesUtils;
import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.RDB;

/** It is thought to extract from a relational databse all the triples with 
 * a certain quantity of credit, and name them in the correlated triple store
 * 
 * */
public class ExtractTriplesFromRDBAndNameThemInATriplestore {

	private Connection connection;
	private RepositoryConnection repo;
	
	/** Query to select triples in the relational database that have credit bigger than 0 
	 * */
	private String SELECT_CREDITED_TRIPLES = "select t.subject , t.predicate , t.\"object\" \n" + 
			"from triplestore t \n" + 
			"where credit > ?";
	

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

	public void extractTriplesFroRDFAndNameThemInTripleStore(int threshold) {
		// asks to the relational db all the triples with a certain quantity of credit
		PreparedStatement stmt = this.connection.prepareStatement(this.SELECT_CREDITED_TRIPLES);
		stmt.setDouble(1, threshold);
		ResultSet r = stmt.executeQuery();

		// prepare an rdf model in-memory
		ModelBuilder builder = new ModelBuilder().setNamespace("q1", "http://bsbm.query1.it/");
		Model model;
		int counter = 0;

		while (r.next()) {

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
}
