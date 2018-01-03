package fyp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;

public class StanfordHandler {
	Properties properties;
	StanfordCoreNLP stanfordCoreNLP;
	String annotations;
	
	public StanfordHandler() {
		// TODO Auto-generated constructor stub
		properties = new Properties();
		annotations = "tokenize, ssplit, pos, lemma, depparse";
		properties.setProperty("annotators", annotations);
		stanfordCoreNLP = new StanfordCoreNLP(properties);
	}
	
	String pickClause(SemanticGraph semanticGraph, IndexedWord indexedWord, Set<String> exclude, int fromIndex) {
		return separateResultAndPrint(getArrayOfKidsWrapper(semanticGraph, indexedWord, exclude), fromIndex);
	}
	
	String pickClause(SemanticGraph semanticGraph, IndexedWord indexedWord, int fromIndex) {
		return separateResultAndPrint(getArrayOfKidsWrapper(semanticGraph, indexedWord), fromIndex);
	}
	
	public String separateResultAndPrint(Set<WordInformation> wordInformationSet, int fromIndex) {
        String string = new String();
        Integer[] integers = new Integer[wordInformationSet.size()];
        String[] strings = new String[wordInformationSet.size()];
        int index = 0;

        Iterator<WordInformation> wordInformationIterator = wordInformationSet.iterator();
        while(wordInformationIterator.hasNext()) {
            WordInformation temp = wordInformationIterator.next();
            integers[index] = temp.index;
            strings[index] = temp.text;
            index++;
        }

        for(int i = 0;i < integers.length-1;i++)
            for(int j = i+1;j < integers.length;j++)
                if(integers[i] > integers[j]) {
                    int tempInt = integers[i];
                    integers[i] = integers[j];
                    integers[j] = tempInt;

                    String tempString = strings[i];
                    strings[i] = strings[j];
                    strings[j] = tempString;
                }

        for(int i = fromIndex;i < strings.length;i++)
           string = string.concat(strings[i]+" ");

        return string;
    }
	
	public Set<WordInformation> getArrayOfKidsWrapper(SemanticGraph semanticGraph, IndexedWord indexedWord, Set<String> strings) {
        Set<WordInformation> wordInformationSet = new HashSet<>();
        return getArrayOFKids(semanticGraph, indexedWord, wordInformationSet, strings);
    }
	
	public Set<WordInformation> getArrayOFKids(SemanticGraph semanticGraph, IndexedWord indexedWord, Set<WordInformation> wordInformationSet, Set<String> strings) {
        if(!indexedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class).contains("W"))
        	wordInformationSet.add(new WordInformation(indexedWord.get(CoreAnnotations.TextAnnotation.class), (indexedWord.get(CoreAnnotations.IndexAnnotation.class))));
        String relationString = new String();
        
        Iterator<IndexedWord> indexedWordIterator = semanticGraph.getChildren(indexedWord).iterator();
        while(indexedWordIterator.hasNext()) {
            IndexedWord temp = indexedWordIterator.next();
            relationString = semanticGraph.getEdge(indexedWord, temp).getRelation().toString();
            if(!strings.contains(relationString))
                getArrayOFKids(semanticGraph, temp, wordInformationSet,strings);
        }
        return wordInformationSet;
    }
	
	public Set<WordInformation> getArrayOfKidsWrapper(SemanticGraph semanticGraph, IndexedWord indexedWord) {
        Set<WordInformation> wordInformationSet = new HashSet<>();
        return getArrayOFKids(semanticGraph, indexedWord, wordInformationSet);
    }
	
	public Set<WordInformation> getArrayOFKids(SemanticGraph semanticGraph, IndexedWord indexedWord, Set<WordInformation> wordInformationSet) {
        if(!indexedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class).contains("W"))
        	wordInformationSet.add(new WordInformation(indexedWord.get(CoreAnnotations.TextAnnotation.class), (indexedWord.get(CoreAnnotations.IndexAnnotation.class))));
        String relationString = new String();
        
        Iterator<IndexedWord> indexedWordIterator = semanticGraph.getChildren(indexedWord).iterator();
        while(indexedWordIterator.hasNext()) {
            IndexedWord temp = indexedWordIterator.next();
            relationString = semanticGraph.getEdge(indexedWord, temp).getRelation().toString();
            getArrayOFKids(semanticGraph, temp, wordInformationSet);
        }
        return wordInformationSet;
    }
	
	public IndexedWord getChildWithRelation(SemanticGraph semanticGraph, IndexedWord indexedWord, String relation) {
		Iterator<GrammaticalRelation> grIterator = semanticGraph.childRelns(indexedWord).iterator();
		
		while(grIterator.hasNext()) {
			GrammaticalRelation tempGrammaticalRelation = grIterator.next();
			if(tempGrammaticalRelation.toString().contains(relation))
				return semanticGraph.getChildWithReln(indexedWord, tempGrammaticalRelation);
		}
		
		return null;
	}
}