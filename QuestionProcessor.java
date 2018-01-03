package fyp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.http.client.cache.ResourceFactory;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelGraphInterface;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.TriplePattern;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PossibleAnswersAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;

public class QuestionProcessor {
	JenaHandler knowledgeBase, questionBase;
	StanfordHandler stanfordHandler;
	ExtJWNLHandler extJWNLHandler;
	Path questionPath, answerPath;
	String source = "platform:/resource/JenaTrial/src/EnOntology.owl";
	ArrayList<String> xsolutions;
	ArrayList<String> ysolutions;
	
	public QuestionProcessor(String mainPath, String questionFile, String answerFile, StanfordHandler stanfordHandler, ExtJWNLHandler extJWNLHandler, JenaHandler knowledgeBase, JenaHandler questionBase) {
		questionPath = FileSystems.getDefault().getPath(mainPath,questionFile);
		answerPath = FileSystems.getDefault().getPath(mainPath, answerFile);
		this.stanfordHandler = stanfordHandler;
		this.knowledgeBase = knowledgeBase;
		this.questionBase = questionBase;
		this.extJWNLHandler = extJWNLHandler;
		xsolutions = new ArrayList<>();
		ysolutions = new ArrayList<>();
	}
	
	public Resource getResourceWithProperty(Resource start, String property) {
		Resource toReturn = null;
		StmtIterator stmtIterator = start.listProperties();
		while(stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			if(statement.getPredicate().toString().equals(property)) {
				 toReturn = statement.getObject().asResource();
				 //System.out.println(predicate);
			}
		}
		return toReturn;
	}
	
	public String getLiteralWithProperty(Resource start, String property) {
		String toReturn = null;
		StmtIterator stmtIterator = start.listProperties();
		while(stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			if(statement.getPredicate().toString().equals(property)) {
				 toReturn = statement.getObject().asLiteral().getString();
				 //System.out.println(predicate);
			}
		}
		return toReturn;
	}
	
	public void printAllStatements(Resource resource) {
		StmtIterator stmtIterator = resource.listProperties();
		while(stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			//System.out.println(statement);
		}
	}
	
	public Set<RDFNode> returnPossiblePredicates(String wordtext) throws JWNLException {
		Set<String> possibleStrings = extJWNLHandler.getSynonyms(POS.VERB,extJWNLHandler.getIndexWord("Verb", wordtext));
		System.out.println(possibleStrings);
		Set<RDFNode> rdfNodes = new HashSet<>();
		
		Iterator<String> iterator = possibleStrings.iterator();
		while(iterator.hasNext()) {
			String something = iterator.next().toUpperCase();
			System.out.println("See if sentence has " + something);
			String query = "SELECT ?sentence ?predicate WHERE { ?predicate <"+source.concat("#Word_Text")+"> \""+something+"\". ?sentence <"+source.concat("#Predicate")+"> ?predicate }";
			QuerySolution querySolution;
			
			try (QueryExecution qexec = QueryExecutionFactory.create(query, knowledgeBase.ontModel)) {
				ResultSet results = qexec.execSelect();
	            
	            while(results.hasNext()) {
	            	System.out.println("There is a sentence");
	            	querySolution = results.next();
	            	rdfNodes.add(querySolution.get("?sentence"));
	            }
			}
			catch(Exception e) {}
		}
			
		return rdfNodes;
	}
	
	public Set<String> getSolutionOnThisNode(RDFNode tempNode, RDFNode predicate, char var, Set<String> solutionBag) {
		if(predicate.isResource() && tempNode.isResource()) {
			//System.out.println("I am in the first if, where both predicate and rdfnode are resources");
			if(predicate.asResource().hasProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text"))) {
				//System.out.println("In the nested if, now I know predicate has Word-Text");
				if(predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) == 'X' || predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) == 'Y' || predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) == 'Z') {
					//System.out.println(rdfNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString());
					//solutions.add(rdfNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString());
					System.out.println("Now I check if the resource is a variable.");
					//System.out.print(rdfNode.toString());
					if(predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) == var) {
						System.out.println(tempNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString());
						solutionBag.add(tempNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString());
					}
					//if(predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) == 'X')
					//	xsolutions.add(rdfNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString());
					//else if(predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) == 'Y')
					//	ysolutions.add(rdfNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString());
					
					//return true;
				}
			}
			
			StmtIterator statements = predicate.asResource().listProperties();
			while(statements.hasNext()) {
				Statement tempStatement = statements.next();//System.out.println("------ Statement I encountered is "+tempStatement);
				Property property = tempStatement.getPredicate();//System.out.println("------ The property I am testing tempnode for is, "+property);
				if(tempNode.asResource().hasProperty(property)) {
					try {
						getSolutionOnThisNode(tempNode.asResource().getProperty(property).getObject(), predicate.asResource().getProperty(property).getObject(), var,solutionBag);
					}
					catch(Exception e) {
						System.out.println("----------- Exception occured");
					}
				}
			}
		}
		
		return solutionBag;
	}
	
	public Set<String> getPossibleSolutions(Set<RDFNode> rdfNodes, RDFNode predicate, char var) throws IOException, JWNLException {
		Set<String> strings = new HashSet<>();
		System.out.println("Getting posssible solutions");
		boolean flagDontConsider = false;
		Property theUnkonwn = null;
		/**
		 * 1.	For each RDFNode in rdfnodes as rdfnode
		 * 2.		I get the dependencies of rdfnode
		 * 3.		I loop through the dependencies
		 * 4.			If the dependency exists, I  check for values of both
		 * 5.				If the values are equal, I do not do anything
		 * 6.				else if one of the values is X,Y,Z etc., I do not do anything
		 * 7.				else, I ignore this rdfnode and go for next rdfnode
		 * 8.			else, I ignore this rdfnode and go for next rdfnode
		 * 9.		Despite all the difficulties, if I reach here, add that answer to the set.
		 */
		
		FileWriter fileWriter = new FileWriter("C:/Users/Sadhana/workspace/System/src/fyp/referenceText.txt", true);
		
		Iterator<RDFNode> rdfiterator = rdfNodes.iterator();
		Set<String> solutionBag = new HashSet<>();
		while(rdfiterator.hasNext()) {
			RDFNode tempNode = rdfiterator.next();
			tempNode = tempNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Predicate")).getObject();
			//System.out.println(compareRDFNodes(predicate, tempNode));
			if(compareRDFNodes(predicate, tempNode)) {
				//System.out.println("Reference sentence is :" + tempNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Clause")).getObject().toString());
				//tempNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString();
				//System.out.println(tempNode.asNode().getURI());
				String tempNodeID = tempNode.asNode().getURI();
				tempNodeID = tempNodeID.substring(tempNodeID.indexOf('#')+1);
				tempNodeID = tempNodeID.substring(0, tempNodeID.indexOf('_'));
				
				System.out.println(tempNodeID);
				fileWriter.write(knowledgeBase.ontModel.getBaseModel().getResource(source+"#"+tempNodeID).asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Sentence_Text")).getObject().toString());
				//int sentenceNumber = tempNode.asResource().getProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Predicate")
				getSolutionOnThisNode(tempNode, predicate, var, solutionBag);
				System.out.println(solutionBag);
			}
		}
		System.out.print("At the end of the while loop: ");
		System.out.println(solutionBag);
		fileWriter.close();
		return solutionBag;
	}
	
	public boolean compareRDFNodes(RDFNode predicate, RDFNode rdfNode) throws JWNLException {
		boolean toReturn = true;
		//System.out.println("Should consider equality? : "+considerEquality);
		
		if(predicate.isResource() && rdfNode.isResource()) {
			if(predicate.asResource().hasProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text"))) {
				if(rdfNode.asResource().hasProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text"))) {
					if(predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) != 'X' && predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) != 'Y' && predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().charAt(0) != 'Z') {
						//boolean areEqual = predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString().equals(rdfNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString());
						String posOfPredicate = predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#POS")).getObject().toString();
						String predicateSide = predicate.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString();
						predicateSide = predicateSide.toLowerCase();
						System.out.println(predicateSide);
						String nodeSide = rdfNode.asResource().getProperty(org.apache.jena.rdf.model.ResourceFactory.createProperty("platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text")).getObject().toString();
						nodeSide = nodeSide.toLowerCase();
						System.out.println(nodeSide);
						if(posOfPredicate.contains("VB")) {
							System.out.println("I am in verb comparison");
							//System.out.println(extJWNLHandler.getSynonyms(POS.VERB, extJWNLHandler.getIndexWord("Verb", predicateSide)));
							try {
								extJWNLHandler.getSynonyms(POS.VERB, extJWNLHandler.getIndexWord("Verb", predicateSide));
								extJWNLHandler.getSynonyms(POS.VERB, extJWNLHandler.getIndexWord("Verb", nodeSide));
							}
							catch(NullPointerException nullPointerException) {
								return false;
							}
							if(!extJWNLHandler.getSynonyms(POS.VERB, extJWNLHandler.getIndexWord("Verb", predicateSide)).contains(nodeSide)) {
								System.out.println("The words are not similar at all");
								return false;
							}
						}
						else if(posOfPredicate.contains("NN")) {
							System.out.println("I am in noun comparison");
							//System.out.println(extJWNLHandler.getSynonyms(POS.NOUN, extJWNLHandler.getIndexWord("Noun", predicateSide)));
							//System.out.println(predicateSide);
							//System.out.println(nodeSide);
							try {
								extJWNLHandler.getSynonyms(POS.NOUN, extJWNLHandler.getIndexWord("Noun", predicateSide));
								extJWNLHandler.getSynonyms(POS.NOUN, extJWNLHandler.getIndexWord("Noun", nodeSide));
							}
							catch(NullPointerException nullPointerException) {
								return false;
							}
							if(!extJWNLHandler.getSynonyms(POS.NOUN, extJWNLHandler.getIndexWord("Noun", predicateSide)).contains(nodeSide)) {
								System.out.println("The words are not similar at all");
								return false;
							}
						}
						else if(posOfPredicate.contains("JJ")) {
							System.out.println("I am in adjective comparison");
							//System.out.println(extJWNLHandler.getSynonyms(POS.ADJECTIVE, extJWNLHandler.getIndexWord("Adjective", predicateSide)));
							try {
								extJWNLHandler.getSynonyms(POS.ADJECTIVE, extJWNLHandler.getIndexWord("Adjective", predicateSide));
								extJWNLHandler.getSynonyms(POS.ADJECTIVE, extJWNLHandler.getIndexWord("Adjective", nodeSide));
							}
							catch(NullPointerException nullPointerException) {
								return false;
							}
							if(!extJWNLHandler.getSynonyms(POS.ADJECTIVE, extJWNLHandler.getIndexWord("Adjective", predicateSide)).contains(nodeSide)) {
								System.out.println("The words are not similar at all");
								return false;
							}
						}
						else if(posOfPredicate.contains("RB")) {
							System.out.println("I am in adverb comparison");
							//System.out.println(extJWNLHandler.getSynonyms(POS.ADVERB, extJWNLHandler.getIndexWord("Adverb", predicateSide)));
							try {
								extJWNLHandler.getSynonyms(POS.ADVERB, extJWNLHandler.getIndexWord("Adverb", predicateSide));
								extJWNLHandler.getSynonyms(POS.ADVERB, extJWNLHandler.getIndexWord("Adverb", nodeSide));
							}
							catch(NullPointerException nullPointerException) {
								return false;
							}
							if(!extJWNLHandler.getSynonyms(POS.ADVERB, extJWNLHandler.getIndexWord("Adverb", predicateSide)).contains(nodeSide)) {
								System.out.println("The words are not similar at all");
								return false;
							}
						}

					}
				}
			}
			
			StmtIterator statements = predicate.asResource().listProperties();
			while(statements.hasNext()) {
				Statement tempStatement = statements.next();
				System.out.println("------ Statement I encountered is "+tempStatement);
				Property property = tempStatement.getPredicate();
				System.out.println("------ The property I am testing tempnode for is, "+property);
				if(rdfNode.asResource().hasProperty(property)) {
					try {
						System.out.println("RdfNode property : "+property.toString());
						toReturn = toReturn && compareRDFNodes(predicate.asResource().getProperty(property).getObject(), rdfNode.asResource().getProperty(property).getObject());
					}
					catch(Exception e) {
						System.out.println("----------- Exception occured");
					}
				}
				else {
					System.out.println("Returning because of absence of property");
					return false;
				}
			}
		}
		
		System.out.println("Returning in general");
		return toReturn;
	}
	
	private ArrayList<Integer> getAllQuestionsWithVariable(char c) {
		// TODO Auto-generated method stub
		ArrayList<Integer> integers = new ArrayList<>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = Files.newBufferedReader(questionPath);
		}
		catch(IOException io) {
			System.out.println("IO exception "+io.getMessage());
		}
		int counter = 1;
		while(true) {
			try {
				String line = bufferedReader.readLine();
				
				StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
				while(stringTokenizer.hasMoreTokens()) {
					String token = stringTokenizer.nextToken();
					if(token.charAt(0) == c)
						integers.add(counter);
				}
				counter++;
			}
			catch(Exception e) {
				break;
			}
		}
		return integers;
	}

	public void runQuestionProcessor() throws JWNLException, IOException {
		//get Predicate from question base
		//	1.	We get the predicate
		//	2.	We get all the sentences which have same/similar predicate
		//	3.	We go through the dependencies of possible question and sentence
		//	4.
		BufferedReader bufferedReader = null;
		
		
		File file = new File("C:/Users/Sadhana/workspace/System/src/fyp/referenceText.txt");
		PrintWriter writer = new PrintWriter(file);
		writer.print("");
		writer.close();
		
		file = new File("C:/Users/Sadhana/workspace/System/src/fyp/answerFile.txt");
		writer = new PrintWriter(file);
		writer.print("");
		writer.close();
		
		char c = 'W';
		for(int i = 0;i < 3;i++) {	
			Set<Set<String>> sets = new HashSet<>();
			Set<String> answers = new HashSet<>();
			c = (char) (c + 1);
			ArrayList<Integer> integers = new ArrayList();
			integers = getAllQuestionsWithVariable(c);
			
			System.out.println("Processing answers for "+c+" the sentences in which it appears is "+integers);
			Iterator<Integer> iterator = integers.iterator();
			while(iterator.hasNext()) {
				try {
					int counter = iterator.next();
					String line = null;
					
					try {
						bufferedReader = Files.newBufferedReader(questionPath);
					}
					catch(IOException io) {
						System.out.println("IO exception "+io.getMessage());
					}
					
					for(int j = 0;j < counter;j++) {
						line = bufferedReader.readLine();
					}
					
					if(line!=null) {
						System.out.println("Question "+counter+" : "+line);
					
						Resource sentence = questionBase.ontModel.getBaseModel().getResource(source+"#"+counter);
						Resource predicate = getResourceWithProperty(sentence, "platform:/resource/JenaTrial/src/EnOntology.owl#Predicate");
						String wordtext = getLiteralWithProperty(predicate, "platform:/resource/JenaTrial/src/EnOntology.owl#Word_Text");
						//System.out.println(wordtext);
						System.out.println("I am running question processor now!");
						System.out.println("Searching for : "+wordtext);
						Set<RDFNode> rdfNodes = returnPossiblePredicates(wordtext);
						System.out.println("possible predicates are "+rdfNodes);
						Set<String> possibleSolutions = getPossibleSolutions(rdfNodes, predicate, c);
						//System.out.println(solutions);
						sets.add(possibleSolutions);
					}
					counter++;
				}
				catch(NullPointerException nullPointerException) {
					//System.out.println("Breaking at "+counter);
					break;
				}
				
				System.out.println(sets);
				
				answers = new HashSet<>();
				Iterator<Set<String>> setIterstor = sets.iterator();
				answers = setIterstor.next();
				while(setIterstor.hasNext()) {
					answers.retainAll(setIterstor.next());
				}
				System.out.println("Intersection : "+answers);
				System.out.println("---------------------------------------------------------------------------");
				
			}
			
			System.out.println("Actual answer : "+answers);
			Iterator<String> siterator = answers.iterator();
			Charset charset = Charset.forName("US-ASCII");
			try(FileWriter writeToAnswer = new FileWriter("C:/Users/Sadhana/workspace/System/src/fyp/answerFile.txt", true)) {
				writeToAnswer.write(new String(c+" "));
				while(siterator.hasNext()) {
					String nextAns = siterator.next();
					if(nextAns.isEmpty())
						writeToAnswer.write("--- No answer ---");
					else
						writeToAnswer.write(nextAns+" ");
				}
				writeToAnswer.write(new String("\n"));
			}
			catch(Exception e) {
				System.out.println("Writing to ans failed");
			}
			System.out.println("---------------------------------------------------------------------------");
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		//Charset charset = Charset.forName("US-ASCII");
		
		/**try (BufferedReader reader = Files.newBufferedReader(questionPath, charset);
			BufferedWriter writer = Files.newBufferedWriter(answerPath, charset);)
		{
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	//System.out.println(line);
		    	processQuestion(new Annotation(line), stanfordHandler.stanfordCoreNLP, knowledgeBase.ontModel, line);
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}**/
	}


	public void processQuestion(Annotation annotation, StanfordCoreNLP stanfordCoreNlp, OntModel ontModel, String question) throws JWNLException {
        stanfordCoreNlp.annotate(annotation);
        
        List<CoreMap> coreMapList = (List<CoreMap>) annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Iterator<CoreMap> coreMapIterator = coreMapList.iterator();
        SemanticGraph semanticGraph;
        IndexedWord indexedWord;
        
        while(coreMapIterator.hasNext()) {
            CoreMap sentence = coreMapIterator.next();
            System.out.println(sentence.get(CoreAnnotations.TextAnnotation.class));
            semanticGraph = sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
            indexedWord = semanticGraph.getFirstRoot();
            
            //semanticGraph.prettyPrint();
            formulateQuestion(semanticGraph, indexedWord, ontModel);
        }
	}
	
	public void formulateQuestion(SemanticGraph semanticGraph, IndexedWord indexedWord, OntModel ontModel) throws JWNLException {
		String subject, object, predicate, preposition, preposition1, unknown, POSAnnotation, query;
		String ns = source + "#";
		IndexedWord child;
		
		unknown = new String();
		predicate = object = subject = query = preposition = preposition1 = new String();
		POSAnnotation = indexedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class);
		if(!(POSAnnotation.contains("WDT") || POSAnnotation.contains("WP") || POSAnnotation.contains("WRB"))) {
			predicate = indexedWord.get(CoreAnnotations.LemmaAnnotation.class).toUpperCase().trim();
			//System.out.println(predicate);
			Iterator<GrammaticalRelation> gIterator = semanticGraph.childRelns(indexedWord).iterator();
			while(gIterator.hasNext())
			{
				GrammaticalRelation temp = gIterator.next();
				if(temp.toString().contains("nmod")) {
					preposition = semanticGraph.getChildWithReln(indexedWord, temp).get(CoreAnnotations.TextAnnotation.class).toLowerCase();
					//System.out.println(preposition);
					
				}
			}
		}
		else
			unknown = "PREDICATE";
		
		Iterator<GrammaticalRelation> grammaticalRelationIterator = semanticGraph.childRelns(indexedWord).iterator();
        while(grammaticalRelationIterator.hasNext()) {
            GrammaticalRelation temp = grammaticalRelationIterator.next();
            child = semanticGraph.getChildWithReln(indexedWord, temp);
            if (temp.toString().contains("nsubj")) {
            	POSAnnotation = child.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            	if(!(POSAnnotation.contains("WDT") || POSAnnotation.contains("WP") || POSAnnotation.contains("WRB"))) {
            		subject = child.get(CoreAnnotations.TextAnnotation.class).toUpperCase().trim();
            	}
            	else
            		unknown = new String("SUBJECT");
            }
            else if(temp.toString().contains("dobj")) {
            	POSAnnotation = child.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            	if(!(POSAnnotation.contains("WDT") || POSAnnotation.contains("WP") || POSAnnotation.contains("WRB")))
            		object = child.get(CoreAnnotations.TextAnnotation.class).toUpperCase().trim();
            	else
            		unknown = new String("OBJECT");
            }
        }
        
        if(unknown.equals("SUBJECT")) {
        	System.out.println("Processing Subject");
        	Set<String> possiblePredicates = extJWNLHandler.getSynonyms(POS.VERB, extJWNLHandler.getIndexWord("Verb", predicate));
        	System.out.println(possiblePredicates);
        	
        	Iterator<String> sIterator = possiblePredicates.iterator();
        	while(sIterator.hasNext()) {
        		String nextPredicate = sIterator.next().trim().toUpperCase();
        		query = "SELECT ?x ?adjx ?sentenceText WHERE { ?predicate <"+ns.concat("Word_Text")+"> \""+nextPredicate+"\" .?object <"+ns.concat("Word_Text")+"> \""+object+"\" . ?sentence <"+ns.concat("Predicate")+"> ?predicate . ?predicate <"+ns.concat("Object")+"> ?object. ?sentence <"+ns.concat("Sentence_Text")+"> ?sentenceText .?sentence <"+ns.concat("Subject")+"> ?wanted . ?wanted <"+ns.concat("Word_Text")+"> ?x . OPTIONAL { ?wanted <"+ns.concat("Adjective")+"> ?adjwantex . ?adjwanted <"+ns.concat("Word_Text")+"> ?adjx .} . OPTIONAL { ?wanted <"+ns.concat("Conjugate")+"> ?conjugate . ?conjugate <"+ns.concat("Word_Text")+"> ?conjx .}}";
        		executeQuery(ontModel, query);
        	}
        		//System.out.println(query);
        	//if(!executeQuery(ontModel, query)) {
        		//query = "SELECT ?x ?adjx ?sentenceText WHERE { ?predicate <"+ns.concat("Word_Text")+"> \""+predicate+"\" . ?sentence <"+ns.concat("Predicate")+"> ?predicate . ?sentence <"+ns.concat("Sentence_Text")+"> ?sentenceText .?sentence <"+ns.concat("Subject")+"> ?wanted . ?wanted <"+ns.concat("Word_Text")+"> ?x . OPTIONAL { ?wanted <"+ns.concat("Adjective")+"> ?adjwanted . ?adjwanted <"+ns.concat("Word_Text")+"> ?adjx .} . OPTIONAL { ?wanted <"+ns.concat("Conjugate")+"> ?conjugate . ?conjugate <"+ns.concat("Word_Text")+"> ?conjx .}}";
        		//executeQuery(ontModel, query);
        	//}
        }
        else if(unknown.equals("OBJECT")) {
        	System.out.println("Processing Object");
        	Set<String> possiblePredicates = extJWNLHandler.getSynonyms(POS.VERB, extJWNLHandler.getIndexWord("Verb", predicate));
        	System.out.println(possiblePredicates);
        	
        	Iterator<String> sIterator = possiblePredicates.iterator();
        	while(sIterator.hasNext()) {
        		String nextPredicate = sIterator.next().trim().toUpperCase();
        		query = "SELECT ?adjx ?sentenceText ?x WHERE { ?predicate <"+ns.concat("Word_Text")+"> \""+nextPredicate+"\" .?subject <"+ns.concat("Word_Text")+"> \""+subject+"\" . ?sentence <"+ns.concat("Predicate")+"> ?predicate . ?predicate <"+ns.concat("Subject")+"> ?subject . ?sentence <"+ns.concat("Sentence_Text")+"> ?sentenceText . ?predicate <"+ns.concat("Object")+"> ?wanted . ?wanted <"+ns.concat("Word_Clause")+"> ?x . OPTIONAL { ?wanted <"+ns.concat("Adjective")+"> ?adjwanted . ?adjwanted <"+ns.concat("Word_Text")+"> ?adjx .} . OPTIONAL { ?wanted <"+ns.concat("Conjugate")+"> ?conjugate . ?conjugate <"+ns.concat("Word_Text")+"> ?conjx .}}";
        		executeQuery(ontModel, query);
        	}
        	//query = "SELECT ?adjx ?sentenceText ?x WHERE { ?predicate <"+ns.concat("Word_Text")+"> \""+predicate+"\" .?subject <"+ns.concat("Word_Text")+"> \""+subject+"\" . ?sentence <"+ns.concat("Predicate")+"> ?predicate . ?predicate <"+ns.concat("Subject")+"> ?subject . ?sentence <"+ns.concat("Sentence_Text")+"> ?sentenceText . ?predicate <"+ns.concat("Object")+"> ?wanted . ?wanted <"+ns.concat("Word_Clause")+"> ?x . OPTIONAL { ?wanted <"+ns.concat("Adjective")+"> ?adjwanted . ?adjwanted <"+ns.concat("Word_Text")+"> ?adjx .} . OPTIONAL { ?wanted <"+ns.concat("Conjugate")+"> ?conjugate . ?conjugate <"+ns.concat("Word_Text")+"> ?conjx .}}";
        	//executeQuery(ontModel, query);
        }
        else if(unknown.equals("PREDICATE")) {
        	System.out.println("Processing Predicate");
        	Set<String> possiblePredicates = new HashSet<>();
        	possiblePredicates.add(subject);
        	try {
        		possiblePredicates.addAll(extJWNLHandler.getSynonyms(POS.NOUN, extJWNLHandler.getIndexWord("Noun", subject)));
        	}
        	catch(Exception e) {
        		
        	}
        	System.out.println(possiblePredicates);
        	
        	Iterator<String> sIterator = possiblePredicates.iterator();
        	while(sIterator.hasNext()) {
        		String nextPredicate = sIterator.next().trim().toUpperCase();
	        	query = "SELECT ?x ?adjx ?sentenceText WHERE { ?subject <"+ns.concat("Word_Text")+"> \""+nextPredicate+"\" . ?predicate <"+ns.concat("Subject")+"> ?subject. ?sentence <"+ns.concat("Predicate")+"> ?predicate. ?sentence <"+ns.concat("Sentence_Text")+"> ?sentenceText. ?predicate <"+ns.concat("Word_Clause")+"> ?x .}";//?sentence <"+ns.concat("Subject")+"> ?subject. ?sentence <"+ns.concat("Sentence_Text")+"> ?sentenceText .?sentence <"+ns.concat("Predicate")+"> ?wanted . ?wanted <"+ns.concat("Word_Text")+"> ?x . ?wanted <"+ns.concat("Adjective")+"> ?adjwanted . ?adjwanted <"+ns.concat("Word_Text")+"> ?adjx .} LIMIT 1";
	        	executeQuery(ontModel, query);
        	}
        }
        //System.out.println(query);
	}
	
	public boolean executeQuery(OntModel model, String query) {
        QuerySolution soln;
        Set<String> solutions = new HashSet<>();
        Set<String> possibleReference = new HashSet<>();
        
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if(!results.hasNext()) {
            	//System.out.println("No solution found");
            	return false;
            }
        	
            while(results.hasNext()) {
            	String solutionString = new String(), sentenceString = new String();
            	soln = results.next();
            	//if(soln.get("?adjx") != null)
            	//	solutionString = solutionString.concat(soln.get("?adjx")+" ");
            	solutionString = solutionString.concat(soln.get("?x") + " ");
            	if(soln.get("?conjx") != null)
            		System.out.print(soln.get("?conjx") + " ");
            	solutions.add(solutionString);
            	possibleReference.add(soln.get("?sentenceText").toString());
            }
            
            System.out.print("Solution = ");
            System.out.println(solutions);
            System.out.print("Posiible reference = ");
            System.out.println(possibleReference);
            
            System.out.println("-------------------------------------------------");
        }
        return true;
	}
}
