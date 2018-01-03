package fyp;


import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

public class Speech    
{    
	String speaktext; 
	public void dospeak(String speak,String  voicename)    
	{    
	    speaktext = speak;    
	    String voiceName =voicename;    
	    try    
	    {    
	        SynthesizerModeDesc desc = new SynthesizerModeDesc(null,"general",  Locale.US,null,null);    
	        Synthesizer synthesizer =  Central.createSynthesizer(desc);    
	        synthesizer.allocate();    
	        synthesizer.resume();     
	        desc = (SynthesizerModeDesc)  synthesizer.getEngineModeDesc();     
	        Voice[] voices = desc.getVoices();      
	        Voice voice = null;   
	        for (int i = 0; i < voices.length; i++)    
	        {    
	            if (voices[i].getName().equals(voiceName))    
	            {    
	                voice = voices[i];    
	                break;     
	            }     
	        }    
	        
	        synthesizer.getSynthesizerProperties().setVoice(voice);    
	        System.out.println("Speaking : "+speaktext);    
	        synthesizer.speakPlainText(speaktext, null);    
	        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);    
	        //synthesizer.deallocate();    
	    }    
	    catch (Exception e)   
	    {    
	        String message = " missing speech.properties in " + System.getProperty("user.home") + "\n";    
	        System.out.println(""+e);    
	        System.out.println(message);    
	    }    
	}    

	public static void callSpeech()    
	{   
		
		//System.setProperty("mbrola.base", "F:/Eclipse Projects/STAR Speech/mbrola");
		
		
		String speakThis = "Hello.I am STAR."
				+ "I am a discourse based Question Answering System. "
				+ "You can give me a text and ask me a question about it."
				+ "I can decompose long sentences into simpler ones."
				+ "I can also understand pronouns present in the text."
				+ "I can represent the information from your text in a knowledge base."
				+ "Then, I can understand your question and answer it.";
		
		StringTokenizer st  = new StringTokenizer(speakThis,".");
		Speech speechObj = new Speech();
		ArrayList<String> sentences = new ArrayList<String>();
		int i = 0;
		while (st.hasMoreTokens())
		{
			String sentence = st.nextToken();
			sentences.add(sentence);
			System.out.println("Next token: "+sentences.get(i));
			i++;
			//speechObj.dospeak(sentence, "kevin16");
		}
	    System.out.println("ArrayList created....");
		
	    for( int j = 0; j < sentences.size(); j++)
	    {
	    	speechObj.dospeak(sentences.get(j), "kevin16");
	    }
	    
	    
		//Speech obj=new Speech(); obj.dospeak(speakThis,"kevin16");    
	
	}    
}
