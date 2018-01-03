package fyp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

//This module is going to take care of text simplification. The class will have some parameters.
//	A file - for input
//	A file - for output
//	StanfordHandler to get necessary things from stanford.
public class TextSimplifier {
	Path inPath, outPath, sentenceAssociations;
	StanfordHandler stanfordHandler;
	Set<String> strings = new HashSet<>();
	Set<String> simplificationsApplied = new HashSet<>();
	int SentenceCount;
	
	public TextSimplifier(StanfordHandler stanfordHandler){
		// TODO Auto-generated constructor stub
		String mainPath = "C:/Users/Sadhana/workspace/System/src/fyp";
		String inputFile = "inputFile.txt";
		String outputFile = "outputFile.txt";
		String sentenceAssoc = "sentenceAssociations.txt";
		SentenceCount = 1;
		
		inPath = FileSystems.getDefault().getPath(mainPath,inputFile);
		outPath = FileSystems.getDefault().getPath(mainPath, outputFile);
		sentenceAssociations = FileSystems.getDefault().getPath(mainPath, sentenceAssoc);
		this.stanfordHandler = stanfordHandler;
		
		strings.add("nsubj");
	    strings.add("dobj");
	    strings.add("punct");
	    strings.add("cc");
	    strings.add("discourse");
	    
	    simplificationsApplied.add("punct");
	}
	
	public TextSimplifier(String mainPath, String inputFile, String outputFile, String sentenceAssocFile, StanfordHandler stanfordHandler) {
		inPath = FileSystems.getDefault().getPath(mainPath,inputFile);
		outPath = FileSystems.getDefault().getPath(mainPath, outputFile);
		sentenceAssociations = FileSystems.getDefault().getPath(mainPath, sentenceAssocFile);
		this.stanfordHandler = stanfordHandler;
		SentenceCount = 1;
		
		strings.add("nsubj");
	    strings.add("dobj");
	    strings.add("punct");
	    //strings.add("cop");
	    strings.add("cc");
	    strings.add("discourse");
	    
	    simplificationsApplied.add("punct");
	    //simplificationsApplied.add("cop");
	}
	
	public void runTextSimplifier() {
		//Charset charset = Charset.forName("US-ASCII");
		
		try (BufferedReader reader = Files.newBufferedReader(inPath);
			BufferedWriter writer = Files.newBufferedWriter(outPath);
			BufferedWriter writeAssocWriter = Files.newBufferedWriter(sentenceAssociations);)
		{
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	LinkedList<String> sentences = simplifyText(line, SentenceCount++, writeAssocWriter);
		    	Iterator<String> sIterator = sentences.iterator();
		    	while(sIterator.hasNext()) {
		    		StringBuffer stringBuffer = new StringBuffer(sIterator.next());
		    		stringBuffer = stringBuffer.replace(0, 1, Character.toUpperCase(stringBuffer.charAt(0))+"");
		    		writer.write(new String(stringBuffer).trim()+".\n");
		    	}
		    } 
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	
	LinkedList<String> simplifyText(String line, int beginSentenceCount, BufferedWriter sentenceAssocWriter) throws IOException {
		Annotation annotation = new Annotation(line);
		stanfordHandler.stanfordCoreNLP.annotate(annotation);
		LinkedList<String> sentences = new LinkedList<>();
		
		List<CoreMap> coreMapList = (List<CoreMap>) annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Iterator<CoreMap> coreMapIterator = coreMapList.iterator();
        while(coreMapIterator.hasNext()) {
        	CoreMap sentence = coreMapIterator.next();
        	
            SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
            IndexedWord indexedWord = semanticGraph.getFirstRoot();
            
            String sentenceString = sentence.get(CoreAnnotations.TextAnnotation.class);
            //System.out.println(sentenceString);
            //semanticGraph.prettyPrint();
            //System.out.println();
            sentences = getSimplifiedSentences(semanticGraph, indexedWord, beginSentenceCount, sentenceAssocWriter);
            //System.out.println();
            sentences.add(0, stanfordHandler.pickClause(semanticGraph, indexedWord, simplificationsApplied, 0));
            simplificationsApplied.clear();
            simplificationsApplied.add("punct");
    	    //simplificationsApplied.add("cop");
            //System.out.println();
        }
        
        return sentences;
	}
	
	public LinkedList<String> getSimplifiedSentences(SemanticGraph semanticGraph, IndexedWord indexedWord, int beginSentenceCount, BufferedWriter associationsWriter) throws IOException {
		LinkedList<String> simplifiedText = new LinkedList<>();
		
		Iterator<GrammaticalRelation> grammaticalRelationIterator = semanticGraph.childRelns(indexedWord).iterator();
        while(grammaticalRelationIterator.hasNext()) {
            GrammaticalRelation temp = grammaticalRelationIterator.next();
            if(temp.toString().contains("appos")) {
            	simplifiedText.add(indexedWord.get(CoreAnnotations.TextAnnotation.class)+" is "+ stanfordHandler.pickClause(semanticGraph, indexedWord, strings, 1));
            	SentenceCount++;
            	simplificationsApplied.add("appos");
            }
            else if(temp.toString().contains("advcl")) {
            	IndexedWord advclChild = semanticGraph.getChildWithReln(indexedWord, temp);
            	Iterator<GrammaticalRelation> advclGrIterator = semanticGraph.childRelns(advclChild).iterator();
            	while(advclGrIterator.hasNext()) {
            		GrammaticalRelation advclTemp = advclGrIterator.next();
            		if(advclTemp.toString().contains("mark")) {
            			if(!semanticGraph.getChildWithReln(advclChild, advclTemp).get(CoreAnnotations.TextAnnotation.class).toUpperCase().equals("TO")) {
            				strings.add("mark");
            			}
            		}
            	}
            	//strings.add("mark");
            	strings.remove("nsubj");
            	simplifiedText.add(stanfordHandler.pickClause(semanticGraph, semanticGraph.getChildWithReln(indexedWord, temp), strings, 0));
            	//System.out.println("Associating "+beginSentenceCount+" And "+(SentenceCount++)+" With CAUSES reln.");
            	String stringToWrite = beginSentenceCount+" "+(SentenceCount++)+" BECAUSE\n";
            	//System.out.println(stringToWrite);
            	associationsWriter.write(stringToWrite);
            	strings.remove("mark");
            	strings.add("nsubj");
            	simplificationsApplied.add("advcl");
            }
            else if(temp.toString().contains("acl:relcl")) {
            	//strings.remove("cop");
            	strings.add("nsubjpass");strings.remove("nsubj");
            	simplifiedText.add(stanfordHandler.pickClause(semanticGraph, indexedWord, strings,0));
            	//System.out.println("Associating "+beginSentenceCount+" And "+SentenceCount++);
            	//strings.add("cop");
            	strings.add("nsubj");
            	strings.remove("nsubjpass");
            	simplificationsApplied.add("acl:relcl");
            }
            else if(temp.toString().contains("conj")) {
            	boolean subjectEncountered = false;
            	boolean cleftRequired = false;
            	IndexedWord conjugateChild = semanticGraph.getChildWithReln(indexedWord, temp);
            	if(conjugateChild.get(CoreAnnotations.PartOfSpeechAnnotation.class).contains("V")) {
	            	Iterator<GrammaticalRelation> conjGrammaticalRelationIterator = semanticGraph.childRelns(conjugateChild).iterator();
	                while(conjGrammaticalRelationIterator.hasNext()) {
	                    GrammaticalRelation conjTemp = conjGrammaticalRelationIterator.next();
	                    if(conjTemp.toString().contains("nsubj")) {
	                    	//System.out.println("I need to cleft!");
	                    	strings.remove("nsubj");strings.remove("dobj");
	                    	simplifiedText.add(stanfordHandler.pickClause(semanticGraph, semanticGraph.getChildWithReln(indexedWord, temp), strings,0));
	                    	String stringToWrite = beginSentenceCount+" "+(SentenceCount++)+" "+temp.toString().trim().substring(5).toUpperCase()+"\n";
	                    	//System.out.println(stringToWrite);
	                    	try {
	                    		associationsWriter.write(stringToWrite);
	                    	}
	                    	catch(IOException io) {
	                    		System.out.println("Got IOE");
	                    	}
	                    	strings.add("nsubj");strings.add("dobj");
	                    	simplificationsApplied.add(temp.toString());
	                    	simplificationsApplied.add("cc");
	                    	subjectEncountered = true;
	                    	break;
	                    }
	                }
	                
	                if(!subjectEncountered) {
	                	IndexedWord subject = stanfordHandler.getChildWithRelation(semanticGraph, indexedWord, "nsubj");
	                	
	                	strings.remove("dobj");
	                	simplifiedText.add(subject.get(CoreAnnotations.TextAnnotation.class)+" "+stanfordHandler.pickClause(semanticGraph, semanticGraph.getChildWithReln(indexedWord, temp), strings,0));
	                	strings.add("dobj");
	                	//System.out.println("Associating "+beginSentenceCount+" And "+(SentenceCount++)+" with "+temp.toString().substring(5).toUpperCase()+" relation");
	                	simplificationsApplied.add(temp.toString());
                    	simplificationsApplied.add("cc");
	                }
            	}
            }
            else
            	simplifiedText.addAll(getSimplifiedSentences(semanticGraph, semanticGraph.getChildWithReln(indexedWord, temp), beginSentenceCount, associationsWriter));
        }
        
        return simplifiedText;
	}
}