package fyp;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jena.ontology.OntModel;
import org.xml.sax.SAXException;

import net.sf.extjwnl.JWNLException;

public class RunSystem {
	String generalPath, inFile, textSimplifierOutFile, sentenceAssocFile, anaphoraResolveOutFile, jenaOutputFile, jenaInputFile, questionFile, answerFile;
	StanfordHandler stanfordHandler;
	ExtJWNLHandler extJWNLHandler;
	boolean isDefaultStory;
	
	RunSystem(String inputFilePath, boolean isDefaultStory) throws FileNotFoundException, JWNLException {
		generalPath = "C:/Users/Sadhana/workspace/System/src/fyp";
		inFile = inputFilePath;
		textSimplifierOutFile = "textSimplification.txt";
		sentenceAssocFile = "sentenceAssociations.txt";
		anaphoraResolveOutFile = "outputFile.txt";
		jenaOutputFile = "KnowledgeBase.owl";
		jenaInputFile = "C:/Users/Sadhana/workspace/System/src/fyp/SentenceOntology.owl";
		questionFile = "questionFile.txt";
		answerFile = "answerFile.txt";
		stanfordHandler = new StanfordHandler();
		extJWNLHandler = new ExtJWNLHandler();
		this.isDefaultStory = isDefaultStory;
	}
	
	public void RunTextSimplifier() throws IOException, SAXException, ParserConfigurationException, TransformerException {
		TextSimplifier textSimplifier;
		AnaphoraResolver anaphoraResolver;
		
		textSimplifier = new TextSimplifier(generalPath, inFile, textSimplifierOutFile, sentenceAssocFile, stanfordHandler);
		textSimplifier.runTextSimplifier();
		if(!isDefaultStory) {
			System.out.println("I am in anaphora, now not opening anaphora");
			anaphoraResolver = new AnaphoraResolver(generalPath, textSimplifierOutFile, anaphoraResolveOutFile, stanfordHandler);
		}
		else {
			System.out.println("I am in anaphora, now opening anaphora");
			System.out.println(inFile.substring(0, inFile.indexOf('.')));
			anaphoraResolver = new AnaphoraResolver(generalPath, textSimplifierOutFile, anaphoraResolveOutFile, stanfordHandler, inFile.substring(0, inFile.indexOf('.'))+"_lookup.txt");
		}
		
		anaphoraResolver.runAnaphoraResolver();
	}
	
	public void RunQuestionModule() throws IOException, SAXException, ParserConfigurationException, TransformerException, JWNLException {
		KnowledgeBaseCreator knowledgeBaseCreator, questionBaseCreator;
		QuestionProcessor questionProcessor;
		
		knowledgeBaseCreator = new KnowledgeBaseCreator(jenaInputFile, jenaOutputFile, generalPath, anaphoraResolveOutFile, sentenceAssocFile, stanfordHandler);
		questionBaseCreator = new KnowledgeBaseCreator(jenaInputFile, jenaOutputFile, generalPath, questionFile, sentenceAssocFile, stanfordHandler);
		questionProcessor = new QuestionProcessor(generalPath, questionFile, answerFile, stanfordHandler, extJWNLHandler, knowledgeBaseCreator.runKnowledgeCreator(true), questionBaseCreator.runKnowledgeCreator(false));
		
		OntModel questionModel = questionBaseCreator.runKnowledgeCreator(false).ontModel;
		questionModel.write(System.out);
		
		OntModel paragraphModel = knowledgeBaseCreator.runKnowledgeCreator(true).ontModel;
		paragraphModel.write(System.out);
		
		questionProcessor.runQuestionProcessor();
	}
	
	public static void main(String args[]) throws IOException, SAXException, ParserConfigurationException, TransformerException, JWNLException {
		RunSystem runSystem = new RunSystem("story1.txt", false);
		
		//System.out.println((int)('m'));
		TextSimplifier textSimplifier;
		AnaphoraResolver anaphoraResolver;
		KnowledgeBaseCreator knowledgeBaseCreator, questionBaseCreator;
		QuestionProcessor questionProcessor;
		StanfordHandler stanfordHandler;
		ExtJWNLHandler extJWNLHandler;
		
		stanfordHandler = new StanfordHandler();
		extJWNLHandler = new ExtJWNLHandler();
		
		textSimplifier = new TextSimplifier(runSystem.generalPath, runSystem.inFile, runSystem.textSimplifierOutFile, runSystem.sentenceAssocFile, stanfordHandler);
		textSimplifier.runTextSimplifier();
		if(!runSystem.isDefaultStory) {
			System.out.println("I am in anaphora, now not opening anaphora");
			anaphoraResolver = new AnaphoraResolver(runSystem.generalPath, runSystem.textSimplifierOutFile, runSystem.anaphoraResolveOutFile, stanfordHandler);
		}
		else {
			System.out.println("I am in anaphora, now opening anaphora");
			System.out.println(runSystem.inFile.substring(0, runSystem.inFile.indexOf('.')));
			anaphoraResolver = new AnaphoraResolver(runSystem.generalPath, runSystem.textSimplifierOutFile, runSystem.anaphoraResolveOutFile, stanfordHandler, runSystem.inFile.substring(0, runSystem.inFile.indexOf('.'))+"_lookup.txt");
		}
		
		anaphoraResolver.runAnaphoraResolver();
		knowledgeBaseCreator = new KnowledgeBaseCreator(runSystem.jenaInputFile, runSystem.jenaOutputFile, runSystem.generalPath, runSystem.anaphoraResolveOutFile, runSystem.sentenceAssocFile, stanfordHandler);
		questionBaseCreator = new KnowledgeBaseCreator(runSystem.jenaInputFile, runSystem.jenaOutputFile, runSystem.generalPath, runSystem.questionFile, runSystem.sentenceAssocFile, stanfordHandler);
		questionProcessor = new QuestionProcessor(runSystem.generalPath, runSystem.questionFile, runSystem.answerFile, stanfordHandler, extJWNLHandler, knowledgeBaseCreator.runKnowledgeCreator(true), questionBaseCreator.runKnowledgeCreator(false));
		
		OntModel questionModel = questionBaseCreator.runKnowledgeCreator(false).ontModel;
		questionModel.write(System.out);
		
		OntModel paragraphModel = knowledgeBaseCreator.runKnowledgeCreator(true).ontModel;
		paragraphModel.write(System.out);
		
		questionProcessor.runQuestionProcessor();
	}
}

