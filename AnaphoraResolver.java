package fyp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.Trees;

public class AnaphoraResolver {
	
	final int MAXPREVSENTENCES = 5;
	Path inPath, outPath, lookupPath;
	StanfordHandler stanfordHandler;
	boolean isTestingForSimpleSentences;
	Scanner t;
    
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    AnaphoraResolver(String mainPath, String inputFile, String outputFile, StanfordHandler stanfordHandler) {
    	inPath = FileSystems.getDefault().getPath(mainPath,inputFile);
		outPath = FileSystems.getDefault().getPath(mainPath, outputFile);
		this.stanfordHandler = stanfordHandler;
		
		isTestingForSimpleSentences = true;
		t = new Scanner(System.in);
    }
    
	AnaphoraResolver(String mainPath, String inputFile, String outputFile, StanfordHandler stanfordHandler, String lookup) {
		inPath = FileSystems.getDefault().getPath(mainPath,inputFile);
		outPath = FileSystems.getDefault().getPath(mainPath, outputFile);
		lookupPath = FileSystems.getDefault().getPath(mainPath, lookup);
		this.stanfordHandler = stanfordHandler;
		
		isTestingForSimpleSentences = false;
		t = new Scanner(System.in);
	}
	
	public void runAnaphoraResolver() throws IOException, SAXException, ParserConfigurationException, TransformerException  
    {	
    	String wholetext = new String(Files.readAllBytes(inPath)); 
    	//System.out.println("Wholetext : " + wholetext);

    	//final String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";        

        //final LexicalizedParser lp = LexicalizedParser.loadModel(PCG_MODEL);
        
        //System.out.println(wholetext);
        String returned = getText(wholetext);
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(outPath, charset);){
        	writer.write(returned);
        }
        //Write output to File
    }

	
	public String getText(String wholetext)  throws IOException, SAXException, ParserConfigurationException, TransformerException{

    	final String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";        

        //final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");

        final LexicalizedParser lp = LexicalizedParser.loadModel(PCG_MODEL);
        
        ArrayList<Tree> parseTrees = new ArrayList<Tree>();
        @SuppressWarnings("unused")
		String asd = "";
        int j = 0;
        StringReader stringreader = new StringReader(wholetext);
        DocumentPreprocessor dp = new DocumentPreprocessor(stringreader);
        
        ArrayList<List> sentences = preprocess(dp);
        
        for (List sentence : sentences) 
        {
        	// Parsing a new sentence and adding it to the parsed tree
        	parseTrees.add( lp.apply(sentence) ); 
        	// Locating all pronouns to be resolved which are present in the sentence
            ArrayList<Tree> PronounsList = findPronouns(parseTrees.get(j)); 
            //System.out.println("PRONOUN: "+PronounsList);
            Tree corefedTree;
            for (Tree pronounTree : PronounsList) 
            { 
            	// Resolving the coref and modifying the tree for each pronoun
                parseTrees.set(parseTrees.size()-1, HobbsResolve(pronounTree, parseTrees));
                //System.out.println("I am here.");
                //System.out.println(parseTrees);
            }
            StringWriter strwr = new StringWriter();
            PrintWriter prwr = new PrintWriter(strwr);
            TreePrint tp = new TreePrint("penn");
            tp.printTree(parseTrees.get(j), prwr);
            prwr.flush();   
            asd += strwr.toString();
            j++;
        }
        String printMyTree = "";
        for (Tree sentence : parseTrees) 
        {
            for (Tree leaf : Trees.leaves(sentence))
                printMyTree += leaf + " ";					
        }
        System.out.println(printMyTree);
        return printMyTree;
        //System.out.println("All done.");
    }

	public Tree HobbsResolve(Tree pronoun, ArrayList<Tree> forest) throws IOException 
    {
    	//System.out.println();
    	System.out.println("I am in HobbsResolve...");
    	// The last one is the one I am going to start from
    	Tree wholetree = forest.get(forest.size()-1);
    	//System.out.println(wholetree);
    	//System.out.println("I am here in my forest...");
        
    	ArrayList<Tree> candidates = new ArrayList<Tree>();
    	
        List<Tree> path = wholetree.pathNodeToNode(wholetree, pronoun);
        //System.out.println("Printing Path...");
        //System.out.println(path);
        //System.out.println("End of Path");
        // Step 1
     // This one locates the NP the pronoun is in, therefore we need one more "parenting" !
        Tree ancestor = pronoun.parent(wholetree);
        
        //System.out.println("This is the ancestor: "+ancestor);
        
        // Step 2
        //Going up the tree to find first NP or S node encountered
        ancestor = ancestor.parent(wholetree);
 //System.out.println("LABEL: "+pronoun.label().value() + "\n\tVALUE: "+pronoun.firstChild());
        while ( !ancestor.label().value().equals("NP") && !ancestor.label().value().equals("S") )
            ancestor = ancestor.parent(wholetree);
        
        //Declare this node as X, call the path used to reach it as 'path'.
        Tree X = ancestor;
        path = X.pathNodeToNode(wholetree, pronoun);
        //System.out.println(path);
        
        // Step 3
        
        //Traverse all branches below node X to the left of path p in a L2R BFS fashion. 
        //Propose as the antecedent any NP node that is encountered which has an NP or S node 
        //between it and X.
        for (Tree relative : X.children()) 
        {
            for (Tree candidate : relative) 
            {
            	// I am looking to all the nodes to the LEFT (i.e. coming before) 
            	// the path leading to X. contain <-> in the path
                if (candidate.contains(pronoun)) break; 
      //System.out.println("LABEL: "+relative.label().value() + "\n\tVALUE: "+relative.firstChild());
                if ( (candidate.parent(wholetree) != X) && 
                		(candidate.parent(wholetree).label().value().equals("NP") 
                				|| candidate.parent(wholetree).label().value().equals("S")) )
                    if (candidate.label().value().equals("NP"))
                    {	
                    	// Propose as the antecedent any NP node that is encountered which has an NP or S node between it and X"
                        candidates.add(candidate);
                        //System.out.println("I am in step 3. This the candidate: "+candidate);
                    }    
            }
        }
        // Step 9 is a GOTO step 4, hence I will envelope steps 4 to 8 inside a while statement.
        while (true) 
        { // It is NOT an infinite loop. 
            // Step 4
        	//If node X is the highest S node in the sentence, traverse the surface parse 
        	//trees of previous sentences in the text in order of recency, the most recent 
        	//first; each tree is traversed in a left-to-right, breadth-first manner, 
        	//and when an NP node is encountered, it is proposed as antecedent. 
        	//If X is not the highest S node in the sentence, continue to step 5.
            if (X.parent(wholetree) == wholetree) 
            {
                for (int q=1 ; q < MAXPREVSENTENCES; ++q) 
                {// I am looking for the previous sentence (hence we start with 1)
                    if (forest.size()-1 < q) break; // If I don't have it, break
                    Tree prevTree = forest.get(forest.size()-1-q); // go to previous tree
                    // Now we look for each S subtree, in order of recency 
                    //(hence right-to-left, hence opposite order of that of .children() ).
                    ArrayList<Tree> backlist = new ArrayList<Tree>();
                    for (Tree child : prevTree.children()) 
                    {
                        for (Tree subtree : child) 
                        {
                            if (subtree.label().value().equals("S")) 
                            {
                                backlist.add(child);
                                break;
                            }
                        }
                    }
                    for (int i = backlist.size()-1 ; i >=0 ; --i) 
                    {
                        Tree Treetovisit = backlist.get(i);
                        for (Tree relative : Treetovisit.children()) 
                        {
                            for (Tree candidate : relative) 
                            { //I am looking to all the nodes to the LEFT (i.e. coming before)
                            // the path leading to X. contain <-> in the path 
                                if (candidate.contains(pronoun)) continue; 
                            //System.out.println("LABEL: "+relative.label().value() + "\n\tVALUE: "+relative.firstChild());
                                if (candidate.label().value().equals("NP")) 
                                { // "Propose as the antecedent any NP node that you find"
                                    if (!candidates.contains(candidate)) 
                                    	candidates.add(candidate);
                                }
                            }
                        }
                    }
                }
                break; // It will always come here eventually
            }
          //In step 4 we did -> If X is not the highest S node in the sentence, continue to step 5.
            
            // Step 5
            //From node X, go up the tree to the first NP or S node encountered. 
            //Call this new node X, and call the path traversed to reach it p.
            ancestor = X.parent(wholetree);
         //System.out.println("LABEL: "+pronoun.label().value() + "\n\tVALUE: "+pronoun.firstChild());
            while ( !ancestor.label().value().equals("NP") && !ancestor.label().value().equals("S") )
                ancestor = ancestor.parent(wholetree);
            X = ancestor;
            
            
            // Step 6
            //If X is an NP node and if the path to X did not pass through 
            //the Nominal node that X immediately dominates, propose X as the antecedent.
            if (X.label().value().equals("NP")) 
            { // If X is an NP
                for (Tree child : X.children()) 
                { // Find the nominal nodes that X directly dominates
                    if (child.label().value().equals("NN") 
                    		|| child.label().value().equals("NNS") 
                    		|| child.label().value().equals("NNP") 
                    		|| child.label().value().equals("NNPS") )
                        if (! child.contains(pronoun))
                        {	
                        // If one of them is not in the path between X and the pronoun, 
                        //add X to the antecedents
                        	candidates.add(X);
                        }	
                        	
                }
            }
            // Step 7
            //Traverse all branches below node X to the left of path p in L2R BFS manner. 
            //Propose any NP node encountered as the antecedent.
            for (Tree relative : X.children()) 
            {
                for (Tree candidate : relative) 
                {
                    if (candidate.contains(pronoun))
                    {	// I am looking to all the nodes to the LEFT (i.e. coming before) 
                    	//the path leading to X. contain <-> in the path
                    	continue; 
                    }	
            //System.out.println("LABEL: "+relative.label().value() + "\n\tVALUE: "+relative.firstChild());
                    if (candidate.label().value().equals("NP")) 
                    { // "Propose as the antecedent any NP node that you find"
                        boolean contains = false;
                        for (Tree oldercandidate : candidates) 
                        {
                            if (oldercandidate.contains(candidate)) 
                            { 
                                contains=true;
                                break;
                            }
                        }
                        if (!contains) candidates.add(candidate);
                    }
                }
            }
            
            
            // Step 8
            //If X is an S node, traverse all branches of node X to the right of path p in a L2R BFS manner, 
            //but do not go below any NP or S node encountered. 
            //Propose any NP node encountered as the antecedent.
            if (X.label().value().equals("S")) 
            {
                boolean right = false;
                // Now we want all branches to the RIGHT of the path pronoun -> X.
                for (Tree relative : X.children()) 
                {
                    if (relative.contains(pronoun)) 
                    {
                        right = true;
                        continue;
                    }
                    if (!right) continue;
                    for (Tree child : relative) 
                    { // Go in but do not go below any NP or S node. Go below the rest
                        if (child.label().value().equals("NP")) 
                        {
                            candidates.add(child);
                            break; // not sure if this means avoid going below NP but continuing with the rest of non-NP children. Should be since its DFS.
                        }
                        if (child.label().value().equals("S")) break; // Same as the last comment
                    }
                }
            }
        } //END OF while LOOP of step 4 to 8

        // Step 9 is a GOTO, so we used a while.
        // End of Hobbs Algorithm.
        
        
        //System.out.println(pronoun + ": CANDIDATES CHAIN IS " + candidates.toString());
        
        //Scoring System
        //LOG: 22/01/2017
        //Scoring system needs to be fixed to introduce categories for the candidates.
        // If PRP is 'it', choose NN
        // If PRP is 'he' or 'she', choose NNP
        // If PRP is 'they', choose NNS or NNPS
        
             
                
        //System.out.println("Pronoun is: "+pronoun);
        //System.out.println("Printing original Candidates...");
        for (int j=0; j < candidates.size(); ++j) 
        {
        	//System.out.println("Candidate #"+j+": "+candidates.get(j));    	
        	
        }
        
        System.out.println("...Done Printing original Candidates...");
       
        //LOG 201703091154
        //test animacy for all the candidates of this pronoun.
        //remove the candidates which do not agree with the pronoun-animacy relation
        //i.e. if the pronoun is HE and candidates are Roy, Sam, rice
        // then remove rice.
        
     //Roy eats dinner. He is hungry. Sam eats with him. She is very hungry. 
     //She eats rice. She loves it.
        
        if (isTestingForSimpleSentences == true)
        {
        	System.out.println("I am here...");
	        String PERSONS[] = {
			"Roy",
			"Sam",
			"Yoko",
			"Nikhil",
			"Sadhana",
			"Sadhana, on, Saturday",
			"Ruth",
			"Ram",
			"the, farmers"
			};
	        
	        String THINGS[] = {
			"rice",
			"dinner",
			"pudding", 
			"Vidyalankar, Institute",
			"AI",
			"Vidyalankar, Institute, of, Technology",
			"Technology",
			"the, gym",
			"shape",
			"Saturday",
			"all",
			"the, factory",
			"Java",
			"college",
			"every, day",
			"Python",
			"Germany",
			"pencil",
			"a, village",
			"a, hundred, sheep",
			"sheep",
			"pastures",
			"a, big, banyan, tree",
			"books",
			"lunch",
			"the, sheep",
			"a, prank",
			"a, tree",
			"a, wolf",
			"the, banyan, tree",
			"the, wolf",
			"home",
			"a, lesson"
			}; 
	        
	        for (int m=0; m < candidates.size(); ++m) 
	        {
	        	Tree candidate = candidates.get(m);
	        	
	        	//getting the pronoun (i.e. he, she, it, etc) in string form
	            List<Tree> prp = pronoun.getLeaves();
	        	String pro = pronoun.getLeaves().toString();
	        	
	        	System.out.println("pronoun -> "+pro);
	        	
	        	//System.out.println(candidate);
	        	
	        	String candidateObj = candidate.getLeaves().toString(); 
	        	//Gives the candidate in string form as [Roy]
	        	
	        	String candidateName = candidateObj.substring(1, (candidateObj.length()-1)); 
	        	// Have to do this bullshit to remove those square brackets.
	        	
	        	//System.out.println("Sending "+candidateObj+" for simple animacy test...");
	        	
	        	String animacyLookup = SimpleAnimacyCheck(candidateName,PERSONS,THINGS); 
	        	
	        	if (animacyLookup.equals("Person"))
	        	{
	        		if(!(pro.equalsIgnoreCase("[He]") || pro.equalsIgnoreCase("[She]") || pro.equalsIgnoreCase("[Him]") || pro.equalsIgnoreCase("[Her]")))
	        		{
	        			candidates.remove(m);
	            		//System.out.println(candidateName+" removed from list as it failed animacy test and number test for He,She");
	            		m--;
	        		}
	        	
	        	}
	        	
	        	if (animacyLookup.equals("Thing"))
	        	{
	        		if(!(pro.equalsIgnoreCase("[It]")|| pro.equalsIgnoreCase("[Its]")))
	        		{
	        			    candidates.remove(m);
		            		//System.out.println(candidateName+" removed from list as it failed animacy test");
		            		m--;
	        		}
	        	}
	        	
	        	/*if (animacyLookup.equals("Error"))
	        	{
	        		candidates.remove(m);
	        		System.out.println(candidateName+" removed from list as it failed animacy test. Neither Person nor Thing");
	        		m--;
	        	}*/
	        	
	        	//System.out.println("Printing filtered Candidates...");
	            for (int k=0; k < candidates.size(); ++k) 
	            {
	       
	             	//System.out.println("Candidate #"+k+": "+candidates.get(k));
	            }
	             
	             System.out.println("...Done Printing filtered Candidates...");
	        	
	        }
        }
        
        if(isTestingForSimpleSentences == false) 
        {   // if sending a file of text, then we'll need to also take in the animacyLookup file
        	// only then we can test for animacy
        	
	        ArrayList<String> persons = new ArrayList<String>();
	        ArrayList<String> things = new ArrayList<String>();
	        System.out.println("I now going to lookup");
	        //dividing the dictionary into persons and things
	        try (BufferedReader br = Files.newBufferedReader(lookupPath)) 
	        {
	            String line;
	            System.out.println("Reading now.");
	            while ((line = br.readLine()) != null) 
	            {         		
	            	if (line.matches("(.*)-p-(.*)"))
	            		persons.add(line);
	            	if (line.matches("(.*)-t-(.*)"))
	            		things.add(line);            	   	  	  
	            }  
	        }
	
	        
	        System.out.println("Created Dictionary");
	        
	        System.out.println("PRINTING PERSONS...");
	        for(int m = 0; m < persons.size();m++)
	        	System.out.println(persons.get(m));
	        System.out.println("PRINTING THINGS....");
	        for(int m = 0; m < things.size();m++)
	        	System.out.println(things.get(m));
	        
	        
	     
	        System.out.println("Checking animacy....");
	        
	        
	        for (int m=0; m < candidates.size(); ++m) 
	        {
	        	Tree candidate = candidates.get(m);
	        	
	        	//getting the pronoun (i.e. he, she, it, etc) in string form
	            List<Tree> prp = pronoun.getLeaves();
	        	String pro = pronoun.getLeaves().toString();
	        	
	        	System.out.println("pronoun -> "+pro);
	        	
	        	//System.out.println(candidate);
	        	
	        	String candidateObj = candidate.getLeaves().toString(); 
	        	//Gives the candidate in string form as [Roy]
	        	
	        	String candidateName = candidateObj.substring(1, (candidateObj.length()-1)); 
	        	// Have to do this bullshit to remove those square brackets.
	        	
	        	//System.out.println("Sending "+candidateObj+" for animacy test...");
	        	
	        	String animacyLookup = AnimacyLookup(candidateObj,persons,things);
	        	//String FirstChildLabel = candi.firstChild().label().value(); 	//for singular plural
	        	if (animacyLookup.equals("Singular Person"))
	            {
	            	//So, if the pronoun is HE,SHE,HIM or HER this should be the antecedent
	        		//System.out.println("Candidate is a singular person");        		
	        		if(!(pro.equalsIgnoreCase("[He]") || pro.equalsIgnoreCase("[She]") || pro.equalsIgnoreCase("[Him]") || pro.equalsIgnoreCase("[Her]")))
	        		{	//candidate is a singular person but pronouns are not HE,SHE,etc    	
		        			candidates.remove(m);
		            		//System.out.println(candidateName+" removed from list as it failed animacy test and number test for He,She");
		            		m--;
	        		}
	            }
	        	
	        	if (animacyLookup.equals("Plural Person"))
	        	{
	        		//System.out.println("Candidate is a plural person");
	        		if(!(pro.equalsIgnoreCase("[They]")))
	        		{
	        			// candidates is plural person but pronoun is not THEY		
	            			candidates.remove(m);
	                		//System.out.println(candidateName+" removed from list as it failed animacy test and number test for They in person");
	                		m--;		
	        		}
	                
	        	}
	        	
	        	if (animacyLookup.equals("Singular Thing"))
	            {
	            	//So, if the pronoun is IT or ITS this should be the antecedent
	        		//System.out.println("Candidate is a singular thing");
	              		
	        		if(!(pro.equalsIgnoreCase("[It]")|| pro.equalsIgnoreCase("[Its]")))
	        		{
	        			//candidate is a singular thing but pronouns are not IT,ITS,etc
	        			    candidates.remove(m);
		            		//System.out.println(candidateName+" removed from list as it failed animacy test");
		            		m--;
	        		
	        		}
	            	
	            } 
	        	
	        	if (animacyLookup.equals("Plural Thing"))
	            {
	            	//So, if the pronoun is IT or ITS this should be the antecedent
	        		//System.out.println("Candidate is a plural thing");
	              		
	        		if(!(pro.equalsIgnoreCase("[They]")))
	        		{
	        			//candidate is a singular thing but pronouns are not IT,ITS,etc
	        			    candidates.remove(m);
		            		//System.out.println(candidateName+" removed from list as it failed animacy test");
		            		m--;
	        		
	        		}
	            	
	            } 
	        	
	        	if (animacyLookup.equalsIgnoreCase("Error in Animacy"))
	        	{	
	        		System.out.println("Some error occured in animacy"); 
	        		System.out.println("Not in persons[] or things[]");
	        		candidates.remove(m);
	        		System.out.println(candidateName+" removed from list as it failed animacy test");		
	        		m--;
	        	
	        	}
	             
	        	//System.out.println("Printing filtered Candidates...");
	            for (int k=0; k < candidates.size(); ++k) 
	            {
	       
	             	//System.out.println("Candidate #"+k+": "+candidates.get(k));
	            }
	             
	             System.out.println("...Done Printing filtered Candidates...");
	        	
	        } 
        
  }    
        
  
        ArrayList<Integer> scores = new ArrayList<Integer>();
        
        for (int j=0; j < candidates.size(); ++j) 
        {
            Tree candidate = candidates.get(j);
            
            Tree parent = null;
            int parent_index = 0;
            /*for (Tree tree : forest) 
            {
            	//System.out.println(".........PRINTING TREE FROM FOREST........");
            	//System.out.println(tree);
                if (tree.contains(candidate)) 
                { 
                    parent = tree;
                    break;
                }
                ++parent_index;
            }*/
            
            for (int k = 0; k < forest.size(); k++)
            {
            	Tree tree = forest.get(k);
            	if(tree.contains(candidate))
            	{
            		parent = tree;
            		parent_index = k;
            	}
            }
            
            //System.out.println("Parent of "+candidate+"is: "+parent);
            scores.add(0);
            
            if (parent_index == 0)
            {	
                scores.set(j, scores.get(j)+100); 
                // If in the last sentence, +100 points
                //System.out.println("It is last sentence, added 100");
            }    
           
          
            
            
            //Further Scoring continues...
            int syntacticScore = syntacticScore(candidate, parent);
            System.out.println("SYNTACTIC SCORE= "+syntacticScore);
            scores.set(j, scores.get(j) + syntacticScore(candidate, parent));

            if (existentialEmphasis(candidate))
            { //Example: "There was a dog standing outside"
            	System.out.println("EXISTENTIAL= 70");
                scores.set(j, scores.get(j)+70);
            } 
            if (!adverbialEmphasis(candidate, parent))
            {	System.out.println("NOT ADVERBIAL= 50");
                scores.set(j, scores.get(j)+50);
            } 
            if (headNounEmphasis(candidate, parent))
            {	System.out.println("HEAD NOUN= 80");
                scores.set(j, scores.get(j)+80);
            } 

            
                        
            // Decision
            // Looks for recency factor
            // Looks for distance of the candidate from the pronoun in text
            int sz = forest.size()-1;
      //System.out.println("pronoun in sentence " + sz + "(sz). Candidate in sentence "+parent_index+" (parent_index)");
            int dividend = 1;
            for (int u=0; u < sz - parent_index; ++u)
                dividend *= 2;
            //System.out.println("\t"+dividend);
            try 
            {
            	scores.set(j, scores.get(j)/dividend);
            }
            catch (ArithmeticException ae)
            {
            	//System.out.println(ae);
            	scores.set(j, 0);
            }
            //System.out.println(candidate + " -> " + scores.get(j) );
        }
        
        int max = -1;
        int max_index = -1;
        
        for (int i=0; i < scores.size(); ++i) {
            if (scores.get(i) > max) {
                max_index = i;
                max = scores.get(i);
            }
        }
        Tree final_candidate = candidates.get(max_index);
        System.out.println("My decision for " + pronoun + " is: " + final_candidate); 
        // Decide what candidate, with both gender resolution and Lappin and Leass ranking.

        Tree pronounparent = pronoun.parent(wholetree).parent(wholetree); // 1 parent gives me the NP of the pronoun
        int pos = 0;
        for (Tree sibling : pronounparent.children()) 
        {
            //System.out.println("Sibling "+pos+": " + sibling);
            if (sibling.contains(pronoun)) break;
            ++pos;
        }
        //System.out.println("Before setchild: " + pronoun);
        @SuppressWarnings("unused")
        Tree returnval = pronounparent.setChild(pos, final_candidate);
        //System.out.println("After setchild: " + pronoun);
        //System.out.println();
        //System.out.println("-----------------------------------------------------------------------------");
        //System.out.println();
        return wholetree; // wholetree is already modified, since it contains pronounparent
    } // End of function HobbsResolve
    
    
    private static String SimpleAnimacyCheck(String candidateName,String[] PERSONS, String [] THINGS)
    {
    	for (int i = 0; i < PERSONS.length; i++)
    	{
    		if (candidateName.equals(PERSONS[i]))
    			return "Person";
    	}
    	
    	for (int i = 0; i < THINGS.length; i++)
    	{
    		if (candidateName.equals(THINGS[i]))
    			return "Thing";
    	}
    		
    	return "Error";
    }
    
    private static String AnimacyLookup(String candidate, ArrayList<String> persons, ArrayList<String> things)
    {
    	for(int i = 0; i < persons.size(); i++)
    	{
    		String person = persons.get(i);
    		//System.out.println(person);
    		String splitPerson[] = person.split("-");
    		
    		if (candidate.equals(splitPerson[0]))
    		{
    			//System.out.println("I am hereeeee");
    			if (splitPerson[1].equals("p") && splitPerson[2].equals("1")){
        			//System.out.println("This is candidate: "+candidate+"-p-1");
        			return "Singular Person";
        		}
    		}
    		
    		if (candidate.equals(splitPerson[0]))
    		{
    			//System.out.println("I am p2p2p2p2");
    			if (splitPerson[1].equals("p") && splitPerson[2].equals("2")){
        			//System.out.println("This is candidate: "+candidate+"-p-2");
        			return "Plural Person";
        		}
    		}
    	}
    	
    	for(int i = 0; i < things.size(); i++)
    	{	
    		String thing = things.get(i);
    		//System.out.println(thing);
    		String splitThing[] = thing.split("-");
    		if (candidate.equals(splitThing[0]))
    		{
    			if (splitThing[1].equals("t") && splitThing[2].equals("1")){
        			//System.out.println("This is candidate: "+candidate+"-t-1");
        			return "Singular Thing";
        		}
    		}
    		if (candidate.equals(splitThing[0]))
    		{
    			if (splitThing[1].equals("t") && splitThing[2].equals("2")){
        			//System.out.println("This is candidate: "+candidate+"-t-2");
        			return "Plural Thing";
        		}
    		}
    	}	
    	return "Error in Animacy";
    			
    }
         
  //syntacticScore(candidate, parent))
    private static int syntacticScore(Tree candidate, Tree root) 
    {
        // We will check whether the NP is inside an S (hence it would be a subject)
        // a VP (direct object)
        // a PP inside a VP (an indirect obj)
        Tree parent = candidate;
        //System.out.println("-------parent.label().value()-------");
        //System.out.println(parent.label().value());
        //System.out.println(root.label().value());
        
        //Tree test = candidate;
        //Tree test2 = test.parent(root);
        //System.out.println("((((((( .parent )))))))");
        //System.out.println(test2);
        try{
        while (! parent.label().value().equals("S")) 
        {
            if (parent.label().value().equals("VP")){ 
            	//System.out.println("Here in VP.....");
            	return 50; // direct obj
            }
            if (parent.label().value().equals("PP")) 
            {
            	//System.out.println("Here in PP.....");
                Tree grandparent = parent.parent(root);
                while (! grandparent.label().value().equals("S")) 
                {
                    if (parent.label().value().equals("VP")) //indirect obj is a PP inside a VP
     
                    	return 40;
                    parent = grandparent;
                    grandparent = grandparent.parent(root);
                } 
            }
            
            parent = parent.parent(root); 
            
            //System.out.println("||||||||||||parent.parent(root)|||||||||");
            //System.out.println(parent);
        }
        return 80; // If nothing remains, it must be the subject
        }
        catch(NullPointerException npe)
        {
        	System.out.println("NullPointerException occured");
        	return 0;
        }
    }

    private static boolean existentialEmphasis(Tree candidate) 
    {	//Example: "There was a dog standing outside"
        // We want to check whether our NP's Dets are "a" or "an".
    	try {
        for (Tree child : candidate) 
        {
            if (child.label().value().equals("DT")) 
            {
                for (Tree leaf : child) 
                {
                    if (leaf.value().equals("a")||leaf.value().equals("an")
                            ||leaf.value().equals("A")||leaf.value().equals("An") ) 
                    {
                        //System.out.println("Existential emphasis!");
                        return true;
                    }
                }
            }
        }
        return false;
    	}
    	
    	catch(NullPointerException npe)
        {
        	System.out.println("NullPointerException occured");
        	return false;
        }
    	
    }

    private static boolean headNounEmphasis(Tree candidate, Tree root) 
    {
    	try{
        Tree parent = candidate.parent(root);
        while (! parent.label().value().equals("S")) 
        { // If it is the head NP, it is not contained in another NP 
        	//(that's exactly how the original algorithm does it)
            if (parent.label().value().equals("NP")) 
            	return false;
            parent = parent.parent(root);
        }
        return true;
    	}
    	catch(NullPointerException npe)
        {
        	System.out.println("NullPointerException occured");
        	return false;
        }
    }

    private static boolean adverbialEmphasis(Tree candidate, Tree root) 
    { // Like in "Inside the castle, King Arthur was invincible". "Castle" has the adv emph.
        try{
    	Tree parent = candidate;
        while (! parent.label().value().equals("S")) 
        {
            if (parent.label().value().equals("PP")) 
            {
                for (Tree sibling : parent.siblings(root)) 
                {
                    if ( (sibling.label().value().equals(","))) 
                    {
                        //System.out.println("adv Emph!");
                        return true;
                    }
                }
            }
            parent = parent.parent(root);
        }
        return false;
        }
        catch(NullPointerException npe)
        {
        	System.out.println("NullPointerException occured");
        	return false;
        }
    }


    public static ArrayList<Tree> findPronouns(Tree t) 
    {
        ArrayList<Tree> pronouns = new ArrayList<Tree>();
        if ((t.label().value().equals("PRP") || t.label().value().equals("PRP$"))  
        		&& !t.children()[0].label().value().equals("I") 
        		&& !t.children()[0].label().value().equals("you") 
        		&& !t.children()[0].label().value().equals("You")) 
        {
            pronouns.add(t);
        }
        else
            for (Tree child : t.children())
                pronouns.addAll(findPronouns(child));
                    return pronouns;
    }

    @SuppressWarnings("rawtypes")
    public static ArrayList<List> preprocess(DocumentPreprocessor strarray) 
    {
        ArrayList<List> Result = new ArrayList<List>();
        for (List<HasWord> sentence : strarray) 
        {
            if (!StringUtils.isAsciiPrintable(sentence.toString())) 
            {
                continue; // Removing non ASCII printable sentences
            }
            //string = StringEscapeUtils.escapeJava(string);
            //string = string.replaceAll("([^A-Za-z0-9])", "\\s$1");
            int nonwords_chars = 0;
            int words_chars = 0;
            for (HasWord hasword : sentence ) 
            {
                String next = hasword.toString();
                if ((next.length() > 30)||(next.matches("[^A-Za-z]"))) 
                {	
                	nonwords_chars += next.length(); // Words too long or non alphabetical will be junk
                }
                	else words_chars += next.length();
            }
            if ( (nonwords_chars / (nonwords_chars+words_chars)) > 0.5) // If more than 50% of the string is non-alphabetical, it is going to be junk
                continue;   // Working on a character-basis because some sentences may contain a single, very long word
            if (sentence.size() > 1000) 
            {
                //System.out.println("\tString longer than 1000 words!\t" + sentence.toString());
                continue;
            }
            Result.add(sentence);
        }
        return Result;
    }
    
    
}