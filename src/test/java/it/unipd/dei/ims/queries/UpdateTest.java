package it.unipd.dei.ims.queries;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import it.unipd.dei.ims.credittordf.utils.TripleStoreHandler;
import it.unipd.dei.ims.data.MyPaths;

/** class to test the ability of rdf4j to update a repository */
public class UpdateTest {

	public static void main(String[] args) {

		// read paths from properties
		new MyPaths();
		String tripleStore = MyPaths.querying_index;
		
		RepositoryConnection repConn = TripleStoreHandler.openRepositoryAndConnection(tripleStore);
		
		String query;
		
		// insert in anonymous graph
		query = "PREFIX ns: <http://example.org/ns#>\n" + 
				"INSERT DATA\n" + 
				" { <http://example/book1>  ns:price  45 } ";
		
		// insert in named graph
		query = "PREFIX ns: <http://example.org/ns#>\n" + 
				"INSERT DATA\n" + 
				" { GRAPH <http://example/bookStore> "
				+ "{ <http://example/book1>  ns:price  45 } }";
		
		// DELETE !!!!
		query = "PREFIX ns: <http://example.org/ns#>\n" + 
				"DELETE DATA\n" + 
//				" { GRAPH <http://example/bookStore> " +
				"{ <http://example/book1>  ns:price  45 } " 
//				 + "}"
				;
		
		Update q = repConn.prepareUpdate(QueryLanguage.SPARQL, query);
		q.execute();
		
//		q.evaluate();
		
//		result.close();
		TripleStoreHandler.closeRepositoryAndConnextion();
		
		
	}

}
