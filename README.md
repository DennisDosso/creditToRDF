# Credit to RDF project

## Step 1: prepare the RDF file

The first step is to create an RDF file, containing triples. This is made using the jar
of BSBM, with the right parameters. The command is as follows

java -cp bsbm.jar benchmark/generator/Generator -s ttl -fn dataset.ttl -fc -pc 200

pc is the number of products. 200 generates around 75.000 triples. Do your math man.
Ok, I'll do it for you: 
* -pc 300 produces 100K triples
* -pc 3000 produced 1M triples
* -pc 30000 produces 10M triples
* -pc 300000 produces 100M triples (stop here, for God's sake!)

command:
<code>
java -cp bsbm.jar benchmark/generator/Generator -s ttl -fn dataset1m -pc 3000
</code>

## STEP 2: import RDF file in triplestore rdf4j
The second step is to convert the database in a triple store. This has to be done only once for the 100K, 1M, 10M versions. This is done with the FromTurtleToTripleStore class.
Class to use: *FromTurtleToTripleStore*

Properties to set:

##### paths.properties file
* text.rdf.file: path of the turtle file with the triples to be inserted in the triplestore

* index_path: path of the directory where to save the triplestore


##### values.properties
* indexes.string: string describing the indexes we will use for the triple store. E.g. spoc,psoc,cspo. It needs to stay the same throughout the execution

<code>
java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/database/FromTurtleToTripleStore
</code> 

or, if you need to set the necessary RAM:
<br/>
<code>
nohup java -Xms4000m -Xmx32000m -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/database/FromTurtleToTripleStore
</code>


## STEP 2/bis: import a very large RDF file in triplestore rdf4j

To directly use rdf4j to import a 100M BSBM proved to be too much. The execution, after one whole night, did not complete, which is simply impossible. 
Thus, I devised a different strategy. First, we divide the big turtle file in smaller parts. To do so, use the *SplitDatasetFile* class.

properties:

##### paths.properties
* text.rdf.file: path of the big turtle file
* fragments.output.directory: where we need to save the split files

<code>
java -cp creditToRdf-1.0.jar:lib/* experiment1.SplitDatasetFile
</code> 

After this, we can now import the fragments, using the class *ImportDatabaseTripleStoreUsingManyFiles*. This class iterates through the many smaller files to build the bigger index.

In this case, the necessary properties to set are the following:

##### paths.properties
* fragments.output.directory: where we take the fragments
* index_path: where to save the big index

##### values.properties
* indexes.string: string describing the indexes we will use for the triple store. E.g. spoc,psoc,cspo. It needs to stay the same throughout the execution. For a big database, I would only use spoc,cspo

Code for this case:

<code>
java -cp creditToRdf-1.0.jar:lib/* experiment1.ImportDatabaseInTripleStoreUsingManyFiles
</code> 


## STEP 3: create values for queries 

We need queries for our experiments. We create a file containing queries that are going to be used for our future experiments. 
Class to use: *BuildAndPrintQueries*

Before starting this class make sure that the file where we write queries is empty or does not exists, because we write in append (maybe one day I'll correct this, but I am too lazy now).

Properties to set:

##### values.properties

* query.plan: two possible values: UNIFORM and MIXED. With UNIFORM, we keep each class separated from the others. For example, you will print 1000 queries, all of the same class. 
With MIXED, you'll print queries mixed with other queries in a unique class composed by "all kinds of queries".


* printing.plan: a string edescribing our printing plan, i.e. the order in which the class of queries are going to be written. For example: ONE,THREE,FIVE,SEVEN,EIGHT will print 1000 queries for the class ONE, 1000 queries for the class THREE etc. 
Alternatively, it contains the class of queries that we use when we print them randomically in a unique class of used queries. De facto, this string will probably be kept untouched within the same database


* how.many.queries: the number of queries for each class in UNIFORM or, alternatively, how many queries in general for MIXED. For the first case, 1000 should be a reasonable choice. In the second choice, it depends on the quantity of epochs you want to consider. 100 queries makes an epoch. Thus, 2000 queries should be ok to experiment also with the TIME strategy.


* standard.deviation.ratio: the number with which we produce a standard deviation of the normal distribution. The higher this number, the more concentrated will be the normal distribution around the mean.
I usually go with 6.


##### paths.properties
* querying.index: the path of the index triple store on disk that we are using


* query.values.files: path of the file where to write the queries

<code>
java -cp creditToRdf-1.0.jar:lib/* experiment1/BuildAndPrintQueries
</code> 

<br>

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* experiment1/BuildAndPrintQueries
</code>


## STEP 4: run first set of experiments

Class: *Experiment1*


##### values.properties

* cool.down.strategy: the cooldown strategy that we decide to use. Three possible choices:<br> 
- NONE: no strategy for cooldown, thus there is only a threshold
– TIME: strategy based on time
– FUNCTION: strategy based on a cooldown function
(actually, this choice does not mean anything, since I created 3 classes for the 3 choices. Experiment1 covers the first choice)

* credit.threshold: when using NONE, it is the constant threshold used to build the cache/named graph

* named.graph: url of the named graph used as index for our queries

* construct.check: set it to true if you want the algorithm to check for the presence of triples returned by the CONSTRUCT queries in the original whole database. This is necessary when using queries with OPTIONAL in them, since the CONSTRUCT may build triples that are not present in the original database


* are.we.distributing.credit

* are.we.interrogating.the.whole.triplestore

* are.we.interrogating.the.cache

* are.we.interrogating.the.whole.named.triplestore

I hope these are self-explanatory. Take care to always run the credit distribution together with the named and cache strategy. Also, never run the whole and the named strategy together, since the underlying triplestore could apply some undetected underlying optimization that only it knows that could pollute the results. 

What I suggest is two iterations in this way: (true, true, true, false) and (true, false, false, true)

##### paths.properties
* querying.index: the path of the index triple store on disk that we are using. 


* query.values.files: path of the file where the query plan is written
<br>

##### NB!!
depending on the values of this entry, you are using a UNIFORM or MIXED type of experiment on the types of queries

* overhead.times: where to write the time used to update the RDB with the hits from the lineage. Done once per query

* whole.db.times: times to query the whole DB, without any optimization. Done once per query.

* named.db.times: path of the times to query the named graph. One value per query executed

* cache.times: same thing, for the cache. One value per query executed. 

* update.cache.times: times spent at each epoch to update the cache

* update.named.times: same thing as above, but when using the named graph

<code>
java -cp creditToRdf-1.0.jar:lib/* experiment1/Experiment1
</code> 
<br>
or 
<br>
<code>
nohup java -Xms4000m -Xmx32000m -cp creditToRdf-1.0.jar:lib/* experiment1/Experiment1
</code>


## Step 5: Experiment2

In this experiment, class *experiment2.Experiment2*, we test the time-based cooldown strategy.

Properties to set:

##### values.properties

* credit.threshold: when using NONE, it is the constant threshold used to build the cache/named graph (suggestion: keep it to 0)

* construct.check (suggestion: leave it to true)

* how.many.epochs: how many epochs the time-out strategy exploits (e.g. 2, 5, 10)

* are.we.interrogating.the.cache
* are.we.interrogating.the.whole.triplestore
<br>
These last two should be one true and the other false for when you want to query using the cache or only the database,
to get an idea of the times required. 



##### paths.properties
* querying.index: the path of the index triple store on disk that we are using. 


* query.values.files: path of the file where the query plan is written (use the mixed one)

* cache.times: times required interrogating the cache, once per query
* whole.db.times: times to query the whole DB, without any optimization. Done once per query.



<code>
java -cp creditToRdf-1.0.jar:lib/* experiment2/Experiment2
</code> 
