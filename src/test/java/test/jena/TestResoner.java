package test.jena;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;


public class TestResoner {

	public static void main(String[] args) {
		// namespace, first part of the URL
		String NS = "urn:x-hp-jena:eg/";

		// Build a trivial example data set
		Model rdfsExample = ModelFactory.createDefaultModel();
		
		Property p = rdfsExample.createProperty(NS, "p");
		Property q = rdfsExample.createProperty(NS, "q");
		rdfsExample.add(p, RDFS.subPropertyOf, q);
		rdfsExample.createResource(NS+"a").addProperty(p, "foo");
		
		
		// does the reasoning
		InfModel inf = ModelFactory.createRDFSModel(rdfsExample);  // [1]
		
		inf.write(System.out);
		
		Resource a = inf.getResource(NS+"a");
		System.out.println("Statement: " + a.getProperty(q));
		
		StmtIterator it = inf.listStatements();
		
		while(it.hasNext()) {
			Statement stmt = it.nextStatement();
			
			Resource  subject   = stmt.getSubject();     // get the subject
		    Property  predicate = stmt.getPredicate();   // get the predicate
		    RDFNode   object    = stmt.getObject();      // get the object

		    System.out.print(subject.toString());
		    System.out.print(" " + predicate.toString() + " ");
		    if (object instanceof Resource) {
		       System.out.print(object.toString());
		    } else {
		        // object is a literal
		        System.out.print(" \"" + object.toString() + "\"");
		    }

		    System.out.println(" .");
		}

	}
}
