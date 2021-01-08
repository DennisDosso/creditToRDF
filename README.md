This project can become a little chaotic at times, so here the sum up of what I did. Maybe it makes some sense. 



STEP 0 - this is done few times, you can probably skip to 3
The first step is to create an RDF file, containing triples. This is made using the jar
of BSBM, with the right parameters. The command is as follows

java -cp bsbm.jar benchmark/generator/Generator -s ttl -fn dataset.ttl -fc -pc 200

pc is the number of products. 200 generates around 75.000 triples. Do your math man.





STEP 1 - this is done few times, you can probably skip to 3
The second step is to convert the database in a triple store. This has to be done only once for the 100K, 1M, 10M versions. This is done with the FromTurtleToTripleStore class.
Properties values to be set are in the file paths.properties, and are the first two in the section STEP 1, text.rdf.file and index_path 




STEP 2 - this is done few times, you can probably skip to 3
Then you need to move the triples in a relational triple store. The database has only one table (or should it have 3 tables?), each table has the values subject, predicate and object, and a column for the credit. 
Properties: index_path in paths.properties
the data for the connection to the database are in the rdb.properties file


###STEP 3
Distribute credit in the relational database.

NB: REMEMBER to put to 0 the credit on the tuples of the relational database!!! The SQL query is something like:
update triplestore 
set credit = 0
where credit <> 0

To do so, we perform a query on the triple store. We use the triples returned by the triple store to give
credit in the relational database.

The class is *BSBMCreditDistributor*.

parameters to set:

in paths.properties
1) index_path: the paths of the triple store where we ask the query

2) values_path: path of a txt file with the values that make up the query - this file needs to be created using 
GraphDB, by putting a SPARQL query in a smart way to obtain a csv file with the values. I created one file per class, with 10.000 lines, i.e. 10.000 different queries. Each class of queries has a different file

3) times_file_path: a file containing 10.000 lines. Each line is a number. Each number represent the number of times the corresponding query in values_path is executed. These too are created using another class, CreateAndPrintRandomNumbers.java 

in values.properties
4) class: the class of queries we are dealing with. For now we are using class 1 and 8


### STEP 4
Here there are two alternatives:

4.a
ExtractTriplesFromRDBAndNameThemInATriplestore
Here we name the triples with credit in the main triple store, and do not create a smaller triple store.
Properties to set:

rdb.properties
1) the properties to connect to the relational database, since we are extracting the values for the connection

paths.properties
2) renaming.triple.store: the path of the triple store where the triples are going to be renamed

values.properties
3) credit.threshold: the threshold to use when extracting the triples. It is the minimum quantity of credit required to the truples (value must be > of the threshold). Default is 0

4) named.graph: the name of the named graph that we are giving to our triples


4.b
ExtractTriplesFromRDBAndBuildTripleStore
Here we are using triples taken from the RDB and we build a new subgraph with less triples

The properties to set:
rdb.properties
1) the properties to connect to the relational database, since we are extracting the values for the connection

paths.properties
2) reduced.index.path: the path of the new limited triple store with only the xredited triples

values.properties
3) credit.threshold: the threshold to use when extracting the triples. It is the minimum quantity of credit required to the truples (value must be > of the threshold). Default is 0

4) named.graph: the name of the named graph that we are giving to our triples


STEP 5
PerformSameQueryOnTwoRepositoriesAndTakeTimes

We perform all the queries that we have at our disposal, and we take the average time
As of now we perform different tests: on the full database without named graphs, on the database with named graphs, 
and on the reduced graph

properties to set:

paths.properties
1)values_path: path of the file containing the values needed for the select query

2) querying.index: index that is being queried

3) are.we.interrogating.the.whole.triplestore : the following three decide if we are interrogating the whole database, or its reduced or named version.

4) are.we.interrogating.the.whole.named.triplestore

5) are.we.interrogating.the.reduced.triplestore


