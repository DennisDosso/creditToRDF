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

## STEP 2/ter: importing a database like DisGeNet, where you have many files
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
I usually go with 18 (in my experiment I then started trying 12 and 6).


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

* epoch.length: the number of queries considered as one epoch. After each epoch (aka "year"), the cache/named graph is refreshed. 


* are.we.distributing.credit

* are.we.interrogating.the.whole.triplestore

* are.we.interrogating.the.cache

* are.we.interrogating.the.whole.named.triplestore

I hope these are self-explanatory. Take care to always run the credit distribution together with the named and cache strategy. Also, never run the whole and the named strategy together, since the underlying triplestore could apply some undetected underlying optimization that only it knows that could pollute the results. 


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


## Step 5: Experiment2 & Experiment3

In this experiment, class *experiment2.Experiment2*, we test the time-based cool-down strategy.
In experiment 3, *experiment3.Experiment3*, we thest the other cool-down function based on decrease of the values.

Properties to set:

##### values.properties

* credit.threshold: when using NONE, it is the constant threshold used to build the cache/named graph (suggestion: keep it to 0)

* construct.check (suggestion: leave it to true)

* how.many.epochs: how many epochs the time-out strategy exploits (e.g. 2, 5, 10)

* year.length: the length of one year in queries. Set maybe to 100?

* epoch.length: the length of an epoch. (e.g. 20, 25, 50, 100)

* are.we.interrogating.the.cache
* are.we.interrogating.the.whole.triplestore
<br>
These last two should be one true and the other false for when you want to query using the cache or only the database,
to get an idea of the times required. 

* cool.down.factor (only for experiment 3): the amount of credit is taken from each triple after each year



##### paths.properties
* querying.index: the path of the index triple store on disk that we are using. 


* query.values.files: path of the file where the query plan is written (use the mixed one)

* cache.times: times required interrogating the cache, once per query
* whole.db.times: times to query the whole DB, without any optimization. Done once per query.



<code>
nohup java -cp creditToRdf-1.0.jar:lib/* experiment2/Experiment2
</code> 
<br/>
To test the other cool-down strategy the parameters are the same. The class, in this case, is Experiment3, the command to run it is the following:
<code>
nohup java -cp creditToRdf-1.0.jar:lib/* experiment3/Experiment3 > 
</code>


================

####Something about Dbpedia

Since the queries obtained from Bonifati et al. on DBpedia present a lot of UNIONS, which we do not like, I decided to only keep the ones that are BGP.

Here the things to do to work with DBPEDIA

###STEP 1: splitting DBpedia in smaller files

Takes DBpedia (bigger files) and makes smaller files, more digestible by whatever processor.

Execute:
<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/utils/SplitDatasetFile > 
</code>

parameters (path.properties)

<ol>
<li> rdf_files_directory: the directory where to take the files to split
<li>  fragmentsOutputDirectory: directory where to save the fragments
</ol>

In this script I added some code to remove certain UNICODE private characters that created problems in the import phase
(rdf4j it appears cannot deal with those characters). The problem was only partially solved. There were certain files
that still contained some malformed URLs. Since they were few, I corrected them by hand using Sublime Text and
regular expressions. Thus, I do not have a clear code for this part. I still have those files saved, thus I am sure
to be able to import them. All these problems are connected to ''peculiar'' URLs, often external to DBpedia.

###STEP 2: import DBpedia in a triple store

Now it is time to convert DBpedia from a set of textual files to your triplestore.

parameters (path.properties)

<ol>
<li> fragments.output.directory: where we take the fragments
<li> index_path: where to save the big index
</ol>

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* experiment1.ImportDatabaseInTripleStoreUsingManyFiles
</code> 


###STEP 2 bis : generate the required queries (this can also be used with other databases, like Dog Food)

Given the dump of queries obtained from <a href="https://aksw.github.io/LSQ/">this website</a>, from here I extracted
real SPARL queries. These are BPR queries that can be answered by our system. 

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/dbpedia/ConvertDumpToListOfQueries
</code>

After this, you also need to create the construct queries, using:
<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/dbpedia/BuildConstructQueriesFromSelectOnes
</code>

I hardcoded things here since I did everything from my MacBook, so it is necessary to change the main method of this class
to make it work. 

This creates a file with a list of queries. I then need to create another file with an equivalent sequence of construct
queries.

### STEP 3: Interrogate DBpedia using simply the database, without any cache

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/dbpedia/QueriesOnDbpedia
</code>

properties to set:
path.properties
<ul>
<li>querying.index: the path of the index triple store on disk that we are using.</li>
<li>whole.db.times: times to query the whole DB</li>
<li>query.select.file: where we have the select queries as produced in the previous step.</li>
</ul>

On rdb.properties, set the properties to connect to the database. 

values.properties
<li>select_query_timeout: timeout of the query</li>


### STEP 4: Interrogate DBpedia using cache, no cooldown, no cap

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/dbpedia/QueriesOnDbpediaWithCache
</code>

properties to set: 

path.properties
<ul>
<li>querying.index: the path of the index triple store on disk that we are using.</li>
<li>query.select.file: where we have the select queries as produced in the previous step.</li>
<li>query.construct.file: where the construct queries are stored.</li>
<li>query.data.file: where the results of our simple select queries are stored (same of whole.db.times at step 3)</li>
<li>cache.times: where to save the results of the times obtained with the cache</li>
<li>overhead.times: where to save the times to update the support RDB</li>
<li>update.cache.times: where to save the times to update the cache and its size</li>
</ul>

On rdb.properties, set the properties to connect to the database.

values.properties
<li>select_query_timeout: timeout of the query</li>
<li>construct.query.timeout: time to compute the construct query for the lineage. Otherwise, a thread goes 
to timeout exception</li>
<li>epoch.length: the length of one epoch (at the end of an epoch the cache is updated)</li>
<li>credit.threshold: threshold to enter the cache.</li>

values.properties
<li>select_query_timeout: timeout of the query</li>
<li>construct_query_timeout: timeout to build the lineage</li>

### STEP 5: Interrogate DBpedia using cache AND cap

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/dbpedia/cachewithcap/QueriesOnDbpediaWithCacheAndCap
</code>

properties to set:

path.properties
<ul>
<li>querying.index: the path of the index triple store on disk that we are using.</li>
<li>query.select.file: where we have the select queries as produced in the previous step.</li>
<li>query.construct.file: where the construct queries are stored.</li>
<li>query.data.file: where the results of our simple select queries are stored (same of whole.db.times at step 3)</li>
<li>cache.times: where to save the results of the times obtained with the cache</li>
<li>overhead.times: where to save the times to upodate the support RDB</li>
<li>update.cache.times: where to save the times to update the cache and its size</li>
</ul>

On rdb.properties, set the properties to connect to the database.

values.properties
<li>select_query_timeout: timeout of the query</li>
<li>construct.query.timeout: time to compute the construct query for the lineage. Otherwise, a thread goes 
to timeout exception</li>
<li>epoch.length: the length of one epoch (at the end of an epoch the cache is updated)</li>
<li>credit.threshold: threshold to enter the cache.</li>
<li>cap: the maximum quantity of triples allowed for the cache</li>


### STEP 6: Interrogate a synthetic DB to get the result set size and the times of the whole DB

Without cache:

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/synthetic/cachewithcap/QueryWithoutCache
</code>

(path.properties)
<li>query.values.file: where to take the queries</li>
<li>whole.db.times</li>

### STEP 7: Interrogate a synthetic DB with the cache and cap

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/synthetic/cachewithcap/QueryWithCacheAndCap
</code>

path.properties
<ul>
<li>querying.index: the path of the index triple store on disk that we are using.</li>
<li>query.values.file: where data to create the queries are stored</li>
<li>cache.times: where to save the results of the times obtained with the cache</li>
<li>overhead.times: where to save the times to upodate the support RDB</li>
<li>update.cache.times: where to save the times to update the cache and its size</li>
</ul>

values.properties
<li>select_query_timeout: timeout of the query</li>
<li>construct.query.timeout: time to compute the construct query for the lineage. Otherwise, a thread goes 
to timeout exception</li>
<li>epoch.length: the length of one epoch (at the end of an epoch the cache is updated)</li>
<li>credit.threshold: threshold to enter the cache.</li>
<li>cap: the maximum quantity of triples allowed for the cache</li>

### STEP 8: interrogate a real DB with Cache, Cap, and Time-Based Cool-Down Strategy

<code>
nohup java -cp creditToRdf-1.0.jar:lib/* it/unipd/dei/ims/credittordf/dbpedia/cachewithcapandcooldown/QueriesWithCacheAndCapAndCooldown
</code>


path.properties
<ul>
<li>querying.index: the path of the index triple store on disk that we are using.</li>
<li>query.select.file: where we have the select queries as produced in the previous step.</li>
<li>query.construct.file: where the construct queries are stored.</li>
<li>query.data.file: where the results of our simple select queries are stored (same of whole.db.times at step 3)</li>
<li>cache.times: where to save the results of the times obtained with the cache</li>
<li>overhead.times: where to save the times to upodate the support RDB</li>
<li>update.cache.times: where to save the times to update the cache and its size</li>
</ul>

values.properties
<li>select_query_timeout: timeout of the query</li>
<li>construct.query.timeout: time to compute the construct query for the lineage. Otherwise, a thread goes 
to timeout exception</li>
<li>epoch.length: the length of one epoch (at the end of an epoch the cache is updated)</li>
<li>credit.threshold: threshold to enter the cache.</li>
<li>cap: the maximum quantity of triples allowed for the cache</li>
<li>year.length: the length of a timeframe</li>
<li>how.many.epochs: number of timeframes we are considering (do not let the name confuse you)</li>



=======

Comments: the set of queries in the directory 0ca1 seems to be all in Russian. Our version of DBpedia is in English, 
so I did not use those queries. 

Certain queries answer even without a cache (i.e. empty cache). It appears that these are queries with only OPTIONAL
operators and a SELECT *, like:
(711) PREFIX  geo:  <http://www.w3.org/2003/01/geo/wgs84_pos#> PREFIX  foaf: <http://xmlns.com/foaf/0.1/> PREFIX  georss: <http://www.georss.org/georss/>  SELECT  * WHERE   { OPTIONAL       { <http://dbpedia.org/resource/Oviedo>                   geo:lat  ?lat .       }     OPTIONAL       { <http://dbpedia.org/resource/Oviedo>                   geo:long  ?long .       }     OPTIONAL       { <http://dbpedia.org/resource/Oviedo>                   foaf:depiction  ?depiction .       }     OPTIONAL       { <http://dbpedia.org/resource/Oviedo>                   foaf:homepage  ?homepage .       }   }  
(854) PREFIX  geo:  <http://www.w3.org/2003/01/geo/wgs84_pos#> PREFIX  foaf: <http://xmlns.com/foaf/0.1/> PREFIX  georss: <http://www.georss.org/georss/>  SELECT  * WHERE   { OPTIONAL       { <http://dbpedia.org/resource/Caso>                   geo:lat  ?lat .       }     OPTIONAL       { <http://dbpedia.org/resource/Caso>                   geo:long  ?long .       }     OPTIONAL       { <http://dbpedia.org/resource/Caso>                   foaf:depiction  ?depiction .       }     OPTIONAL       { <http://dbpedia.org/resource/Caso>                   foaf:homepage  ?homepage .       }   }  

