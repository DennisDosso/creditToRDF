package it.unipd.dei.ims.database;

import it.unipd.dei.ims.data.MyPaths;
import it.unipd.dei.ims.data.MyValues;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

public class FromXMLToTriplestore {

    // name of the file in input
    static String filename;


    public static void main(String[] args) throws FileNotFoundException, IOException {
        MyValues.setup();
        MyPaths.setup();

        // path of the directory where the turtle files are located
        String inputDirectory = MyPaths.fragmentsOutputDirectory;
//        String inputDirectory = "/Users/anonymous/Documents/databases/SW Dog Food/SWDFood";

        System.out.println("importing files from " + inputDirectory);

        // list all files, recursively (they must have extension .rdf)
        Collection<File> files = FileUtils.listFiles(new File(inputDirectory), new String[]{"rdf"}, true);

        // open the triplestore
        File dataDir = new File(MyPaths.index_path);
        String indexes = MyValues.indexString; // the indexes
        Repository db = new SailRepository(new NativeStore(dataDir, indexes));
        db.init();
        RepositoryConnection conn = db.getConnection();

        for (File f : files) {
            if (!f.exists())
                continue;
            System.out.println("Gonna import " + f.getName());


            // read data from file in Turtle format and save them in the triplestore
            try (FileInputStream input = new FileInputStream(f)) {
                // add the RDF data from the inputstream directly to our database
                conn.add(input, "", RDFFormat.RDFXML);
                conn.commit();
                System.out.println("Woo! Committed the file " + f.getName());

            } catch (RDFParseException e) {
                System.out.println("en error occurred with file " + f + ", moving on");
                e.printStackTrace();
            }

        }
    }
}
