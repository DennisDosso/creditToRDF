package disgenetqueries;

public class DisGeNetUnboundedQueries {

	public static String select_1 = DisGeNetQuery1.prefixes 
			+ "SELECT * "
			+ "WHERE {"
			+ "?gda rdf:type ?type; " 
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ " dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. "
			+ "FILTER(?type=sio:SIO_001122) } "
			+ "LIMIT 20";
	
	public static String select_named_1 = DisGeNetQuery1.prefixes 
			+ "SELECT * "
			+ "WHERE { GRAPH %here {"
			+ "?gda rdf:type ?type; " 
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ " dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. "
			+ "FILTER(?type=sio:SIO_001122) }} "
			+ "LIMIT 20";
	
	public static String construct_1 = DisGeNetQuery1.prefixes 
			+ "CONSTRUCT{ "
			+ "?gda rdf:type ?type; " // 1
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ "dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. }"
			+ "WHERE { "
			+ "?gda rdf:type ?type; " // 2
			+ "rdfs:label ?label ; "
			+ "rdfs:comment ?comment ; "
			+ "dcterms:title ?title ;"
			+ " dcterms:identifier ?id ; "
			+ "void:inDataset ?voidSubset. "
			+ "FILTER(?type=sio:SIO_001122) } "
			+ "LIMIT 200";
	
	public static String select_2 = DisGeNetQuery1.prefixes 
			+ "SELECT DISTINCT * \n" + 
			"	WHERE {\n" + 
			"		?gda sio:SIO_000628 ?gene,?disease .\n" +  
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" + 
			"	}\n" + 
			"	LIMIT 20";
	
	public static String select_named_2 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT * \n" + 
			"	WHERE {GRAPH %here {\n" + 
			"		?gda sio:SIO_000628 ?gene,?disease .\n" + 
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" + 
			"	}"
			+ "}\n" + 
			"	LIMIT 20";
	
	
	public static String construct_2 = DisGeNetQuery1.prefixes +
			"CONSTRUCT {" +
			"		?gda sio:SIO_000628 ?gene,?disease .\n" +  
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" 
			+ "} \n" + 
			"	WHERE {\n" + 
			"		?gda sio:SIO_000628 ?gene,?disease .\n" + 
			"		?gene rdf:type ncit:C16612 ;\n" + 
			"			sio:SIO_000205 ?symbolUri .\n" + 
			"    	?symbolUri dcterms:title ?geneSymbol .\n" + 
			"		?disease rdf:type ncit:C7057;\n" + 
			"			dcterms:title ?diseaseName\n" + 
			"	}\n "
			+ "LIMIT 150";
	
	
	public static String select_3 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT * \n" +  
			"WHERE {\n" + 
			"	?gda sio:SIO_000628\n" +  
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	OPTIONAL {\n" + 
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" + 
			"	}\n" + 
			"} LIMIT 20";
	
	public static String select_named_3 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT *\n" +  
			"WHERE {GRAPH %here {\n" + 
			"	?gda sio:SIO_000628\n" +  
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	OPTIONAL {\n" + 
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" + 
			"	}\n" + 
			"}"
			+ "} LIMIT 20";
	
	public static String construct_3 = DisGeNetQuery1.prefixes +
			"CONSTRUCT {" +
			"	?gda sio:SIO_000628\n" + //1 
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" +
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" +
			"}\n" + 
			"WHERE {\n" + 
			"	?gda sio:SIO_000628\n" + // 2
			"		<http://linkedlifedata.com/resource/umls/id/C0035372>,\n" + 
			"		<http://identifiers.org/ncbigene/4204> ;\n" + 
			"		rdf:type ?associationType ;\n" + 
			"		sio:SIO_000216 ?scoreIRI ;\n" + 
			"		sio:SIO_000253 ?source .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	OPTIONAL {\n" + 
			"		?gda sio:SIO_000772 ?pmid .\n" + 
			"		?gda dcterms:description ?sentence .\n" + 
			"	}\n "
			+ "} LIMIT 250";
	
	public static String select_4 = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * \n" + 
			"WHERE {\n" + 
			"	?gda sio:SIO_000628 ?gene, ?disease ;\n" + //1 
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	FILTER (?score >= 0.4)\n" + 
			"}\n" + 
			"ORDER BY DESC(?score)\n" + 
			"LIMIT 20";
	
	public static String select_named_4 = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * \n" + 
			"WHERE { GRAPH %here {\n" + 
			"	?gda sio:SIO_000628 ?gene, ?disease ;\n" + 
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	FILTER (?score >= 0.4)\n" + 
			"}"
			+ "}\n" + 
			"ORDER BY DESC(?score)\n" + 
			"LIMIT 20";
	
	public static String construct_4 = DisGeNetQuery1.prefixes + 
			"CONSTRUCT{" +
			"	?gda sio:SIO_000628 ?gene, ?disease ;\n" + 
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n"
			+ "} \n" + 
			"WHERE {\n" + 
			"	?gda sio:SIO_000628 ?gene, ?disease ;\n" + 
			"		sio:SIO_000253 ?source ;\n" + 
			"		sio:SIO_000216 ?scoreIRI .\n" + 
			"	?disease a ncit:C7057 .\n" + 
			"	?scoreIRI sio:SIO_000300 ?score .\n" + 
			"	FILTER (?score >= 0.4)\n" + 
			"}" + 
			"ORDER BY DESC(?score)\n" + 
			"LIMIT 200";
	
	public static String select_5 =  DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT ?gene ?symbol ?protein ?proteinclassname "
			+ "WHERE { "
			+ "?gda sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " 
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "} "
			+ "LIMIT 10";
	
	public static String select_named_5 =  DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT ?gene ?symbol ?protein ?proteinclassname "
			+ "WHERE { GRAPH %here {"
			+ "?gda sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " //1
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "}} "
			+ "LIMIT 10";
	
	public static String construct_5 =  DisGeNetQuery1.prefixes + 
			"CONSTRUCT {"
			+ "?gda sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " //1
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "}"
			+ "WHERE { "
			+ "?gda sio:SIO_000628 <http://linkedlifedata.com/resource/umls/id/C0036341> , ?gene. " //2
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_010078 ?protein ; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?symbol . "
			+ "?protein a ncit:C17021 ; "
			+ "sio:SIO_000095 ?proteinclass . "
			+ "} "
			+ "LIMIT 100";
	
	
	public static String select_6 = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * "
			+ "WHERE { "
			+ "?gda sio:SIO_000628 ?gene,?disease ; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . } "
			+ "LIMIT 10";
	
	public static String select_named_6 = DisGeNetQuery1.prefixes + 
			"SELECT DISTINCT * "
			+ "WHERE { GRAPH %here {"
			+ "?gda sio:SIO_000628 ?gene,?disease ; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "} }"
			+ "LIMIT 10";
	
	public static String construct_6 = DisGeNetQuery1.prefixes + 
			"CONSTRUCT {"
			+ "?gda sio:SIO_000628 ?gene,?disease ; " // 1
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "} "
			+ "WHERE { "
			+ "?gda sio:SIO_000628 ?gene,?disease ; " // 2
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?gene rdf:type ncit:C16612 ; "
			+ "dcterms:title ?geneName . "
			+ "?disease rdf:type ncit:C7057 ; "
			+ "dcterms:title \"Alzheimer's Disease\"@en . "
			+ "?scoreIRI sio:SIO_000300 ?score . } "
			+ "LIMIT 200";
	
	
	public static String select_7 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?dName ?gSymbol  "
			+ "WHERE {"
			+ "?gda rdf:type ?type; " 
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "}  "
			+ "LIMIT 10 ";
	
	public static String select_named_7 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?dName ?gSymbol  "
			+ "WHERE { GRAPH %here {"
			+ "?gda rdf:type ?type; " 
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "} } "
			+ "LIMIT 10 ";
	
	public static String construct_7 = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ "?gda rdf:type ?type; "
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "}"
			+ "WHERE { "
			+ "?gda rdf:type ?type; "
			+ "sio:SIO_000628 ?gene,?disease; "
			+ "sio:SIO_000772 ?article . "
			+ "?disease a ncit:C7057; "
			+ "dcterms:title ?dName . "
			+ "?gene a ncit:C16612; "
			+ "sio:SIO_000205 ?symbolUri . "
			+ "?symbolUri dcterms:title ?gSymbol . "
			+ "}  "
			+ "LIMIT 200 ";
	
	public static String select_8 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?disease ?diseaseName ?gene ?disease2 ?diseaseName2 "
			+ "WHERE { "
			+ "?gda sio:SIO_000628 ?disease,?gene . " 
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "FILTER (?disease != ?disease2) "
			+ "FILTER (?gda != ?gda2) }" 
			+ "LIMIT 10";
	
	public static String select_named_8 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?disease ?diseaseName ?gene ?disease2 ?diseaseName2 "
			+ "WHERE { GRAPH %here {"
			+ "?gda sio:SIO_000628 ?disease,?gene . " 
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "FILTER (?disease != ?disease2) "
			+ "FILTER (?gda != ?gda2) " 
			+ "}}"
			+ "LIMIT 10";
	
	public static String construct_8 = DisGeNetQuery1.prefixes +
			" CONSTRUCT {"
			+ "?gda sio:SIO_000628 ?disease,?gene . " 
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "} "
			+ "WHERE { "
			+ "?gda sio:SIO_000628 ?disease,?gene . " 
			+ "?gda2 sio:SIO_000628 ?disease2,?gene . "
			+ "?disease dcterms:title ?diseaseName . "
			+ "?disease2 dcterms:title ?diseaseName2 . "
			+ "FILTER (?disease != ?disease2) "
			+ "FILTER (?gda != ?gda2) " 
			+ "}" 
			+ "LIMIT 200";
	
	
	public static String select_9 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?gene ?geneName ?disease ?gene2 ?geneName2 "
			+ "WHERE { "
			+ "?gda sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "FILTER (?gene != ?gene2) "
			+ "FILTER (?gda != ?gda2) } " // 2
			+ "LIMIT 10";
	
	
	public static String select_named_9 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?gene ?geneName ?disease ?gene2 ?geneName2 "
			+ "WHERE { GRAPH %here {"
			+ "?gda sio:SIO_000628 ?disease,?gene . " // 1
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "FILTER (?gene != ?gene2) "
			+ "FILTER (?gda != ?gda2) } " // 2
			+ "}" 
			+ "LIMIT 10";
	
	
	public static String construct_9 = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ "?gda sio:SIO_000628 ?disease,?gene . " 
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "} "
			+ "WHERE { "
			+ "?gda sio:SIO_000628 ?disease,?gene . " 
			+ "?gda2 sio:SIO_000628 ?disease,?gene2 . "
			+ "?gene dcterms:title ?geneName . "
			+ "?gene2 dcterms:title ?geneName2 . "
			+ "FILTER (?gene != ?gene2) "
			+ "FILTER (?gda != ?gda2) } " 
			+ "LIMIT 200";
	
	public static String select_10 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?umls ?umlsTerm ?doid ?doTerm "
			+ "WHERE {"
			+ "?gda sio:SIO_000628 ?umls . "
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "} "
			+ "LIMIT 10";
	
	public static String select_named_10 = DisGeNetQuery1.prefixes +
			"SELECT DISTINCT ?umls ?umlsTerm ?doid ?doTerm "
			+ "WHERE { GRAPH %here {"
			+ "?gda sio:SIO_000628 ?umls . "
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "} "
			+ "}"
			+ "LIMIT 10";
	
	public static String construct_10 = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ "?gda sio:SIO_000628 ?umls . " // 1
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "}"
			+ "WHERE {"
			+ "?gda sio:SIO_000628 ?umls . " // 2
			+ "?umls dcterms:title ?umlsTerm ; "
			+ "skos:exactMatch ?doid . "
			+ "?doid rdfs:label ?doTerm . "
			+ "} "
			+ "LIMIT 100";
	
	
	public static String select_11 = DisGeNetQuery1.prefixes +
			" SELECT * "
			+ "WHERE { "
			+ "?vda sio:SIO_000628 ?variant,?disease; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "LIMIT 20";
	
	
	public static String select_named_11 = DisGeNetQuery1.prefixes +
			" SELECT * "
			+ "WHERE { GRAPH %here { "
			+ "?vda sio:SIO_000628 ?variant,?disease; "
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "}"
			+ "LIMIT 20";
	
	public static String construct_11 = DisGeNetQuery1.prefixes +
			" CONSTRUCT {"
			+ "?vda sio:SIO_000628 ?variant,?disease; " // 1
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "WHERE { "
			+ "?vda sio:SIO_000628 ?variant,?disease; " // 2
			+ "sio:SIO_000216 ?scoreIRI . "
			+ "?scoreIRI sio:SIO_000300 ?score . "
			+ "?disease a ncit:C7057 . "
			+ "?disease dcterms:title ?diseaseTitle . "
			+ "?variant a ?type . "
			+ "?variant dcterms:title ?variantTitle .  "
			+ "?variant sio:SIO_000216 ?spe,?pleio . "
			+ "?spe a sio:SIO_001351 ; "
			+ "sio:SIO_000300 ?speValue . "
			+ "?pleio a sio:SIO_001352 ; "
			+ "sio:SIO_000300 ?pleioValue . "
			+ "?variant sio:SIO_000061 ?chr . "
			+ "?chr sio:SIO_000300 ?chrValue . } "
			+ "LIMIT 400";

	
	public static String select_12 = DisGeNetQuery1.prefixes +
			"SELECT * "
			+ "WHERE {"
			+ " ?variant a so:0001060 ; "
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal;"
			+ " sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "OPTIONAL { ?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal . }"
			+ "} "
			+ "LIMIT 20";
	
	
	public static String select_named_12 = DisGeNetQuery1.prefixes +
			"SELECT * "
			+ "WHERE { GRAPH %here {"
			+ " ?variant a so:0001060 ; "
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal;"
			+ " sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "OPTIONAL { ?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal . }"
			+ "} }"
			+ "LIMIT 20";
	
	public static String construct_12 = DisGeNetQuery1.prefixes +
			"CONSTRUCT {"
			+ " ?variant a so:0001060 ; " // 1
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal;"
			+ " sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal ."
			+ "} "
			+ "WHERE {"
			+ " ?variant a so:0001060 ; " // 2
			+ "sio:SIO_000223 ?refAl,?altAl . "
			+ "?refAl sio:SIO_000300 ?refAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap . "
			+ "?altAl sio:SIO_000300 ?altAlVal; "
			+ "sio:SIO_000253 ?source; "
			+ "sio:SIO_000628 ?vsap; "
			+ "sio:SIO_000900 ?altAlFreq . "
			+ "OPTIONAL { ?altAlFreq a sio:SIO_001367; sio:SIO_000300 ?altAlFreqVal . }"
			+ "} "
			+ "LIMIT 300";
	
}
