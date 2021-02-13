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

## STEP 1: import RDF file in triplestore rdf4j
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

## STEP 2: create values for queries 

We need queries for our experiments. We create a file containing queries that are going to be used for our future experiments. 
Class to use: *BuildAndPrintQueries*

Before starting this class make sure that the file where we write queries is empty or does not exists, because we write in append (maybe one day I'll correct this, but I am too lazy now).

Properties to set:

##### values.properties

* query.plan: two possible values: UNIFORM and MIXED. With UNIFORM, we keep each class separated from the others. For example, you will print 1000 queries, all of the same class. 
With MIXED, you'll print queries mixed with other queries in a unique class composed by "all kinds of queries".


* printing.plan: a string edescribing our printing plan, i.e. the order in which the class of queries are going to be written. For example: ONE,THREE,FIVE,SEVEN,EIGHT will print 1000 queries for the class ONE, 1000 queries for the class THREE etc. 
Alternatively, it contains the class of queries that we use when we print them randomically in a unique class of used queries.


* how.many.queries: the number of queries for each class in UNIFORM or, alternatively, how many queries in general for MIXED.


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
nohup java -Xms4000m -Xmx32000m -cp creditToRdf-1.0.jar:lib/* experiment1/BuildAndPrintQueries
</code>


## STEP 3: run first set of experiments

Class: *Experiment1*


##### values.properties

* cool.down.strategy: the cooldown strategy that we decide to use. Three possible choices:<br> 
- NONE: no strategy for cooldown, thus there is only a threshold
– TIME: strategy based on time
– FUNCTION: strategy based on a cooldown function

* credit.threshold: when using NONE, it is the constant threshold used to build the cache/named graph

* named.graph: url of the named graph used as index for our queries


* are.we.distributing.credit

* are.we.interrogating.the.whole.triplestore

* are.we.interrogating.the.cache

* are.we.interrogating.the.whole.named.triplestore

I hope these are self-explanatory. Take care to always run the credit distribution together with the named and cache strategy. Also, never run the whole and the named strategy together, since the underlying triplestore could apply some undetected underlying optimization that only it knows that could pollute the results. 

What I suggest is two iterations in this way: (true, true, true, false) and (true, false, false, true)

##### paths.properties
* querying.index: the path of the index triple store on disk that we are using. 


* query.values.files: path of the file where the query plan is written
NB, depending on the values of this entry, you are using a UNIFORM or MIXED type of experiment on the types of queries

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


