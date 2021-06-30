package it.unipd.dei.ims.testRdf4j;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

public class TestRdf4JCreateDatabase {

	public static void main(String[] args) throws RDFParseException, UnsupportedRDFormatException, IOException {
		String ex = "http://example.org/";
		
		// Create IRIs for the resources we want to add.
		IRI picasso = iri(ex, "Picasso");
		IRI artist = iri(ex, "Artist");
		
		// Create a new, empty Model object.
		Model model = new TreeModel();
		
		// add our first statement: Picasso is an Artist
		model.add(picasso, RDF.TYPE, artist);
		model.add(picasso, FOAF.FIRST_NAME, literal("Pablo"));

		// print the model
//		System.out.println(model);
		
		// print the statements
//		for (Statement statement: model) {
//		    System.out.println(statement);
//		}
		
		
		// read
		String filename = "/Users/anonymous/eclipse-workspace/bsbmtools-0.2/jar/dataset.ttl";
		InputStream input = TestRdf4JCreateDatabase.class.getResourceAsStream(filename);
		model = Rio.parse(input, "", RDFFormat.TURTLE);
		
		//alternatively
		model.forEach(System.out::println);
		
		System.out.println("done");
		
		// https://github.com/eclipse/rdf4j
	}
	
}
