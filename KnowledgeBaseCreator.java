package fyp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

public class KnowledgeBaseCreator {
	JenaHandler sentenceOntology;//, knowledgeBaseOntology;
	StanfordHandler stanfordHandler;
	Path inPath, sentenceAssoc, outPath;
	OutputStream outputStream;
	String source = "platform:/resource/JenaTrial/src/EnOntology.owl";
	
	public KnowledgeBaseCreator(String sentenceOntologyPath, String knowledgeOntologyPath, String mainPath, String inputFile, String sentenceAssocFile, StanfordHandler stanfordHandler) throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		sentenceOntology = new JenaHandler(sentenceOntologyPath);
		//knowledgeBaseOntology = new JenaHandler(knowledgeOntologyPath);
		this.stanfordHandler = stanfordHandler;
		outputStream = new FileOutputStream(new File(knowledgeOntologyPath));
		
		inPath = FileSystems.getDefault().getPath(mainPath,inputFile);
		outPath = FileSystems.getDefault().getPath("C:/Users/Sadhana/workspace/System/src/fyp/KnowledgeBase.txt");
		sentenceAssoc = FileSystems.getDefault().getPath(mainPath, sentenceAssocFile);
	}
	
	public JenaHandler runKnowledgeCreator(boolean considerSentenceAssoc) {
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedReader reader = Files.newBufferedReader(inPath, charset);
			BufferedReader sentenceAssocReader = Files.newBufferedReader(sentenceAssoc, charset);)
			{
				String wholetext = new String(Files.readAllBytes(inPath));
				populateModel(new Annotation(wholetext), stanfordHandler.stanfordCoreNLP, sentenceOntology.ontModel, wholetext, sentenceAssocReader, considerSentenceAssoc);
			} catch (IOException x) {
			    System.err.format("IOException: %s%n", x);
			}
		return sentenceOntology;//.ontModel.write(System.out);
	}
	
	public void populateModel(Annotation annotation, StanfordCoreNLP stanfordCoreNlp, OntModel ontModel, String paragraph, BufferedReader sentenceAssoc, boolean considerSentenceAssoc) throws IOException {
		Resource firstSentence = null, secondSentence = null;
		Property sentenceProperty = null;
		stanfordCoreNlp.annotate(annotation);
		List<CoreMap> coreMapList = (List<CoreMap>) annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Iterator<CoreMap> coreMapIterator = coreMapList.iterator();
        SemanticGraph semanticGraph;
        IndexedWord indexedWord;
        int sentenceCount = 1;
        
        while(coreMapIterator.hasNext()) {
            CoreMap sentence = coreMapIterator.next();
            
            semanticGraph = sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
            indexedWord = semanticGraph.getFirstRoot();
            
            //System.out.println(sentenceCount);
            //semanticGraph.prettyPrint();
            //System.out.println();
            
            addBasicsToModel(sentence, indexedWord, semanticGraph, ontModel, sentenceCount);
            
            sentenceCount++;
        }
        
        if(considerSentenceAssoc) {
	        String line = null;
	        while((line = sentenceAssoc.readLine()) != null) {
	        	StringTokenizer stringTokenixer = new StringTokenizer(line);
	        	for(int i = 0;i < 3;i++) {
	        		switch(i) {
	        		case 0:	int firstSentenceNumber = Integer.parseInt(stringTokenixer.nextToken());
	        				firstSentence = ontModel.getResource((source+"#").concat(String.valueOf(firstSentenceNumber)));
	        				break;
	        		case 1: int secondSentenceNumber = Integer.parseInt(stringTokenixer.nextToken());
							secondSentence = ontModel.getResource((source+"#").concat(String.valueOf(secondSentenceNumber)));
							break;
	        		case 2: sentenceProperty = ontModel.getProperty((source+"#").concat(String.valueOf(stringTokenixer.nextToken())));
	        				ontModel.add(firstSentence, sentenceProperty, secondSentence);
	        		}
	        	}
	        }
        }
        
        //RDFDataMgr.write(outputStream, ontModel, Lang.RDFXML);
        //OutputStream outputStream  = Files.newOutputStream(outPath);
        //ontModel.write(System.out);
        //ontModel.write(outputStream);
	}
	
	public void addBasicsToModel(CoreMap sentence, IndexedWord indexedWord, SemanticGraph semanticGraph, OntModel ontModel, int sentenceCount) {
		Resource sentenceResource;
		Property sentenceProperty;
		String ns = source+"#";
		String tense = new String();
        String posAnnotation = new String();
		
		for(CoreLabel token: sentence.get(TokensAnnotation.class)) { 
        		sentenceResource = ontModel.createResource(ns.concat(sentenceCount+"_"+token.get(CoreAnnotations.IndexAnnotation.class)));
        		sentenceProperty = ontModel.getProperty(ns.concat("Word_Text"));
        		if(!indexedWord.equals(new IndexedWord(token))) {
					ontModel.add(sentenceResource, sentenceProperty, token.get(CoreAnnotations.LemmaAnnotation.class).toUpperCase().trim());
        		}	
        		else {
        			try {
        				ontModel.add(sentenceResource, sentenceProperty, (token.get(CoreAnnotations.LemmaAnnotation.class)+" "+stanfordHandler.getChildWithRelation(semanticGraph, indexedWord, "compound:prt").get(CoreAnnotations.TextAnnotation.class)).toUpperCase().trim());
        			}
        			catch(NullPointerException nullPointerException) {
        				ontModel.add(sentenceResource, sentenceProperty, (token.get(CoreAnnotations.LemmaAnnotation.class)).toUpperCase().trim());
        			}
        		}
        		sentenceProperty = ontModel.getProperty(ns.concat("POS"));
        		ontModel.add(sentenceResource, sentenceProperty, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
        		sentenceProperty = ontModel.getProperty(ns.concat("Word_Clause"));
        		ontModel.add(sentenceResource, sentenceProperty, stanfordHandler.pickClause(semanticGraph, new IndexedWord(token),0));
        }
        
		sentenceResource = ontModel.createResource(ns.concat(String.valueOf(sentenceCount)));
        sentenceProperty = ontModel.createProperty(ns+"Sentence_Text");
        ontModel.add(sentenceResource, sentenceProperty, sentence.get(CoreAnnotations.TextAnnotation.class));
        
        posAnnotation = indexedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        
        IndexedWord copula = null;
        
        if((copula = stanfordHandler.getChildWithRelation(semanticGraph, indexedWord, "cop")) != null) {
    		//The sentence has no verb so treat it like that.
        	if(posAnnotation.equals("VB") || posAnnotation.equals("VBZ"))
	        	tense = "Present";
	        else if(posAnnotation.equals("VBD") || posAnnotation.equals("VBN"))
	        	tense = "Past";
	        sentenceProperty = ontModel.getProperty(ns.concat("Tense"));
	        ontModel.add(sentenceResource, sentenceProperty, tense);
	        
    		sentenceProperty = ontModel.getProperty(ns+"Predicate");
	        ontModel.add(sentenceResource, sentenceProperty, ontModel.getResource(ns.concat(sentenceCount+"_"+copula.get(CoreAnnotations.IndexAnnotation.class).toString())));
	        
	        Resource predicate = ontModel.getResource(ns.concat(sentenceCount+"_"+copula.get(CoreAnnotations.IndexAnnotation.class).toString()));
	        sentenceProperty = ontModel.getProperty(ns+"Object");
	        ontModel.add(predicate, sentenceProperty, ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class).toString())));
    	
	        sentenceProperty = ontModel.getProperty(ns+"Subject");
	        indexedWord = stanfordHandler.getChildWithRelation(semanticGraph, indexedWord, "nsubj");
	        ontModel.add(predicate, sentenceProperty, ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class).toString())));
        }
        else if((copula = stanfordHandler.getChildWithRelation(semanticGraph, indexedWord, "aux")) != null) {
        	//System.out.println("In aux");
        	if(posAnnotation.equals("VB") || posAnnotation.equals("VBZ"))
	        	tense = "Present";
	        else if(posAnnotation.equals("VBD") || posAnnotation.equals("VBN"))
	        	tense = "Past";
	        sentenceProperty = ontModel.getProperty(ns.concat("Tense"));
	        ontModel.add(sentenceResource, sentenceProperty, tense);
	        
        	sentenceProperty = ontModel.getProperty(ns+"Predicate");
	        ontModel.add(sentenceResource, sentenceProperty, ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class).toString())));
	        
	        addStatementsToModel(semanticGraph, indexedWord, ontModel, sentenceCount);
	    }
        else {
        	if(posAnnotation.contains("V")) {
    	        if(posAnnotation.equals("VB") || posAnnotation.equals("VBZ"))
    	        	tense = "Present";
    	        else if(posAnnotation.equals("VBD") || posAnnotation.equals("VBN"))
    	        	tense = "Past";
    	        sentenceProperty = ontModel.getProperty(ns.concat("Tense"));
    	        ontModel.add(sentenceResource, sentenceProperty, tense);
    	        
    	        sentenceProperty = ontModel.getProperty(ns+"Predicate");
    	        ontModel.add(sentenceResource, sentenceProperty, ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class).toString())));
    	        
    	        addStatementsToModel(semanticGraph, indexedWord, ontModel, sentenceCount);
        	}
        }
        	
	}
	
	public void addStatementsToModel(SemanticGraph semanticGraph, IndexedWord indexedWord, OntModel ontModel, int sentenceCount) {
		Resource wordResource, sentResource;
		Property property = null;
		String ns = source + "#";
        
		IndexedWord child, tempChild;
		GrammaticalRelation tempGrReln;
		
		sentResource = ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class).toString()));
        Iterator<GrammaticalRelation> grammaticalRelationIterator = semanticGraph.childRelns(indexedWord).iterator();
        while(grammaticalRelationIterator.hasNext()) {
            GrammaticalRelation temp = grammaticalRelationIterator.next();
            if (temp.toString().contains("nsubj")) {
            	//System.out.println("Adding subject");
            	child = semanticGraph.getChildWithReln(indexedWord, temp);
            	wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class)));
            	property = ontModel.createProperty(ns+"Subject");
            	//System.out.println(property.toString());
            	ontModel.add(sentResource, property, wordResource);
            	
            	addConjunction(child, semanticGraph, ontModel, sentenceCount);
                
            	if(semanticGraph.hasChildren(child)) addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
            }
            else if(temp.toString().contains("dobj") || temp.toString().contains("xcomp")) {
            	//System.out.println("Adding object");
            	child = semanticGraph.getChildWithReln(indexedWord, temp);
            	wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class)));
            	property = ontModel.createProperty(ns+"Object");
            	//System.out.println(property.toString());
            	ontModel.add(sentResource, property, wordResource);
            	
            	addConjunction(child, semanticGraph, ontModel, sentenceCount);
                
            	if(semanticGraph.hasChildren(child)) addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
            }
            else if(temp.toString().contains("advmod")) {
            	System.out.println("Adding advmod");
            	child = semanticGraph.getChildWithReln(indexedWord, temp);
            	wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class)));
            	property = ontModel.createProperty(ns+"Adjective");
            	System.out.println(property.toString());
            	ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class))));
            }
            else if (temp.toString().contains("nmod")) {
            	//System.out.println("Adding nmod");
                child = semanticGraph.getChildWithReln(indexedWord, temp);
                Iterator<GrammaticalRelation> tempIterator = semanticGraph.childRelns(child).iterator();
                String preposition = new String();
                while(tempIterator.hasNext()) {
                    tempGrReln = tempIterator.next();
                    if(tempGrReln.toString().contains("case")) {
                    	IndexedWord prepositionIndexed = semanticGraph.getChildWithReln(child, tempGrReln);
                        preposition = prepositionIndexed.get(CoreAnnotations.TextAnnotation.class);
                        preposition = preposition.trim();
                        
                        wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class)));
                        
                        property = ontModel.getProperty(ns.concat(preposition));
                        //System.out.println(property.toString());
                        ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class))));
                        addConjunction(child, semanticGraph, ontModel, sentenceCount);
                        
                        if(child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBZ") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBD") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBN") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBP") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VB") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBG")) {
                    		//addBasicsToModel(sentence, indexedWord, semanticGraph, ontModel, sentenceCount);
                        	property = ontModel.getProperty(ns+"ContinuePredicate");
                    		wordResource = ontModel.getResource(ns.concat(String.valueOf(sentenceCount)) + "_" + indexedWord.get(CoreAnnotations.IndexAnnotation.class));
                            ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class).toString())));
                        	
                        	property = ontModel.getProperty(ns+"Predicate");
                    		wordResource = ontModel.getResource(ns.concat(String.valueOf(sentenceCount)));
                            ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class).toString())));                  
                    		addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
                    	}
                        else if(semanticGraph.hasChildren(child)) addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
                    }
                }
            }
            else if (temp.toString().contains("acl")) {
            	//System.out.println("Adding acl");
            	//if(temp.toString().contains("acl:relcl")) System.out.println("Found acl:relcl");
                child = semanticGraph.getChildWithReln(indexedWord, temp);
                if(temp.toString().contains("acl:relcl")) {
                	wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class)));
                	property = ontModel.createProperty(ns+"Adjective");
                	//System.out.println(property.toString());
                	ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class))));
                	addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
                }
                else {
	                Iterator<GrammaticalRelation> tempIterator = semanticGraph.childRelns(child).iterator();
	                String preposition = new String();
	                while(tempIterator.hasNext()) {
	                    tempGrReln = tempIterator.next();
	                    if(tempGrReln.toString().contains("mark")) {
	                        preposition = semanticGraph.getChildWithReln(child, tempGrReln).get(CoreAnnotations.TextAnnotation.class);
	                        preposition = preposition.trim();
	                        
	                        wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class)));
	                        property = ontModel.getProperty(ns.concat(preposition));
	                        //System.out.println(property.toString());
	                        ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class))));
	                        addConjunction(child, semanticGraph, ontModel, sentenceCount);
	                        
	                        if(child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBZ") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBD") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBN") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBP") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VB") || child.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBG")) {
	                    		//addBasicsToModel(sentence, indexedWord, semanticGraph, ontModel, sentenceCount);
	                        	property = ontModel.getProperty(ns+"ContinuePredicate");
	                    		wordResource = ontModel.getResource(ns.concat(String.valueOf(sentenceCount)) + "_" + indexedWord.get(CoreAnnotations.IndexAnnotation.class));
	                            ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class).toString())));
	                        	
	                        	property = ontModel.getProperty(ns+"Predicate");
	                    		wordResource = ontModel.getResource(ns.concat(String.valueOf(sentenceCount)));
	                            ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class).toString())));                  
	                    		addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
	                    	}
	                        else if(semanticGraph.hasChildren(child)) addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
	                    }
	                }
                }
            }
            else if(temp.toString().contains("amod")) {
            	System.out.println("Adding amod and compound");
            	child = semanticGraph.getChildWithReln(indexedWord, temp);
            	wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class)));
                property = ontModel.getProperty(ns.concat("Adjective"));
                //System.out.println(property.toString());
                ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class))));
            	
                addConjunction(child, semanticGraph, ontModel, sentenceCount);
                addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
            }
            else if(temp.toString().contains("compound")) {
            	System.out.println("Adding amod and compound");
            	child = semanticGraph.getChildWithReln(indexedWord, temp);
            	wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class)));
                property = ontModel.getProperty(ns.concat("Adjective"));
                //System.out.println(property.toString());
                ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class))));
            	
                addConjunction(child, semanticGraph, ontModel, sentenceCount);
                addStatementsToModel(semanticGraph, child, ontModel, sentenceCount);
            }
            else {
            	child = semanticGraph.getChildWithReln(indexedWord, temp);
            	wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+indexedWord.get(CoreAnnotations.IndexAnnotation.class)));
            	property = ontModel.getProperty(ns.concat(temp.toString()));
            	ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class))));
            }

        }
	}
	
	public void addConjunction(IndexedWord child, SemanticGraph semanticGraph, OntModel ontModel, int sentenceCount) {
		Resource wordResource;
		Property property;
		String ns = source + "#";
		
		IndexedWord tempChild;
		GrammaticalRelation tempGrReln;
		
		Iterator<IndexedWord> tempIterator1 = semanticGraph.getChildren(child).iterator();
        //System.out.println("Iterating children of : "+child.get(CoreAnnotations.TextAnnotation.class));
        wordResource = ontModel.getResource(ns.concat(sentenceCount+"_"+child.get(CoreAnnotations.IndexAnnotation.class)));
        while(tempIterator1.hasNext()) {
        	tempChild = tempIterator1.next();
        	tempGrReln = semanticGraph.getEdge(child, tempChild).getRelation();
            if(tempGrReln.toString().contains("conj")) {
                //System.out.println("   GOT: "+tempChild.get(CoreAnnotations.TextAnnotation.class));
                property = ontModel.createProperty(ns+"Conjugate");
            	ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+tempChild.get(CoreAnnotations.IndexAnnotation.class))));
            	if(tempChild.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBZ") || tempChild.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBD") || tempChild.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBN") || tempChild.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBP") || tempChild.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VB") || tempChild.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("VBG")) {
            		//addBasicsToModel(sentence, indexedWord, semanticGraph, ontModel, sentenceCount);
            		property = ontModel.getProperty(ns+"Predicate");
            		wordResource = ontModel.getResource(ns.concat(String.valueOf(sentenceCount)));
                    ontModel.add(wordResource, property, ontModel.getResource(ns.concat(sentenceCount+"_"+tempChild.get(CoreAnnotations.IndexAnnotation.class).toString())));                  
            		addStatementsToModel(semanticGraph, tempChild, ontModel, sentenceCount);
            	}
            }
        }
	}
}
