package fyp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class ExtJWNLHandler {
	Dictionary dictionary;
	String pathToDictionary;
	
	public ExtJWNLHandler(String path) throws FileNotFoundException, JWNLException {
		// TODO Auto-generated constructor stub
		FileInputStream inputStream = new FileInputStream(path);
		dictionary = Dictionary.getInstance(inputStream);
	}
	
	public ExtJWNLHandler() throws FileNotFoundException, JWNLException {
		// TODO Auto-generated constructor stub
		FileInputStream inputStream = new FileInputStream("C:/Users/Sadhana/workspace/System/Windows Wordnet/extjwnl-1.9.2/src/extjwnl/src/main/resources/net/sf/extjwnl/dictionary/file_properties.xml");
		dictionary = Dictionary.getInstance(inputStream);
	}
	
	public IndexWord getIndexWord(String pos, String word) throws JWNLException {
		POS partOfSpeech = POS.valueOf(pos.trim().toUpperCase());
		return dictionary.lookupIndexWord(partOfSpeech, word);
	}
	
	public Set<String> getAllWordsOfRelationAndPOS(PointerType pointerType, POS pos, IndexWord indexWord) throws JWNLException {
		Set<String> verbs = new HashSet<>();
		
		Iterator<Synset> sIterator = indexWord.getSenses().iterator();
		while(sIterator.hasNext()) {
			Synset synset = sIterator.next();
			Iterator<PointerTarget> pointerTIterator = synset.getTargets(pointerType).iterator();
			while(pointerTIterator.hasNext()) {
				PointerTarget pointerTarget = pointerTIterator.next();
				if(pointerTarget.getPOS().equals(pos)) {
					//System.out.println("Found verb : "+pointerTarget.toString());
					Synset targetSynset;
					targetSynset = pointerTarget.getSynset();
					Iterator<Word> wIterator = targetSynset.getWords().iterator();
					while(wIterator.hasNext()) {
						verbs.add(wIterator.next().getLemma());
					}
				}
			}
		}
		return verbs;
	}
	
	public Set<String> getSynonyms(POS pos, IndexWord indexWord) throws JWNLException {
		Set<String> verbs = new HashSet<>();
		
		Iterator<Synset> sIterator = indexWord.getSenses().iterator();
		while(sIterator.hasNext()) {
			Synset synset = sIterator.next();
			Iterator<Word> wIterator = synset.getWords().iterator();
			while(wIterator.hasNext()) {
				Word word = wIterator.next();
				verbs.add(word.getLemma());
			}
		}
		return verbs;
	}
	
	public static void main(String args[]) throws JWNLException, FileNotFoundException {
		ExtJWNLHandler extJWNLHandler = new ExtJWNLHandler();
		IndexWord indexWord = extJWNLHandler.getIndexWord("Verb", "is");
		System.out.println(extJWNLHandler.getSynonyms(POS.VERB, indexWord));
		//System.out.println(extJWNLHandler.getAllWordsOfRelationAndPOS(PointerType.HYPERNYM, POS.NOUN, extJWNLHandler.getIndexWord("Noun", "king")));
	}
}