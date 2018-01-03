package fyp;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import net.sf.extjwnl.JWNLException;

import java.awt.Color;

public class Question extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Question frame = new Question();
					frame.setExtendedState(6);
					frame.setResizable(true);
					frame.setVisible(true);
					
					//frame.setUndecorated(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Question() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(30, 144, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Give me a Question");
		lblNewLabel.setFont(new Font("Calibri", Font.PLAIN, 42));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel.setBounds(93, 121, 495, 66);
		contentPane.add(lblNewLabel);
		
		final JTextArea textArea = new JTextArea();
		textArea.setBackground(UIManager.getColor("Button.highlight"));
		textArea.setFont(new Font("Lucida Bright", Font.PLAIN, 16));
		textArea.setBounds(103,	200 , 450, 100);
		contentPane.add(textArea);
		
		JButton btnNewButton = new JButton("Answer");
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent n) {				
				FileWriter write=null;
				try{
			        write = new FileWriter ("C:/Users/Sadhana/workspace/System/src/fyp/questionFile.txt");
			        textArea.write(write);
			        //nextframe
			        
			      			    }
			    catch (Exception e){
			        e.printStackTrace();
			    }
			    finally{
			      if(write != null)
					try {
						
						//trigger to start executing the main modules.
						//trigger -> qts module.
						
						RunSystem rs = new RunSystem("inputFile.txt", true);
						rs.RunQuestionModule();
						write.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} /*catch (JWNLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/ catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JWNLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			      Answer a = new Answer();
			       a.setVisible(true);
			       a.setExtendedState(6);
			       a.setTitle("S.T.A.R.");
			       dispose();
			     
					
			    } 			
			}
		});
		
		btnNewButton.setBackground(new Color(50, 205, 50));
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setFont(new Font("Calibri", Font.PLAIN, 20));
		btnNewButton.setBounds(283, 350, 110, 35);
		contentPane.add(btnNewButton);
		
		
		
	/*	
		JLabel lblNewLabel1 = new JLabel("Answer");
		lblNewLabel1.setBackground(UIManager.getColor("TextField.selectionForeground"));
		lblNewLabel1.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel1.setFont(new Font("Calibri", Font.PLAIN, 40));
		lblNewLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel1.setBounds(190, 420, 271, 50);
		contentPane.add(lblNewLabel1);
		
		final JTextArea textArea1 = new JTextArea();
		textArea1.setBackground(UIManager.getColor("Button.highlight"));
		textArea1.setEditable(false);
		textArea1.setFont(new Font("Lucida Bright", Font.PLAIN, 16));
		textArea1.setBounds(103, 480, 450, 100);
		textArea1.setLineWrap(true);
		textArea1.setWrapStyleWord(true);
		contentPane.add(textArea1);
		textArea1.setColumns(10);
	*/	
	    JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(750, 180, 450, 450);
		contentPane.add(scrollPane);
		
		JTextArea textArea2 = new JTextArea();
		textArea2.setEditable(false);
		scrollPane.setViewportView(textArea2);
		textArea2.setFont(new Font("Lucida Bright", Font.PLAIN, 16));
		textArea2.setBounds(750, 180, 450, 450);
		//contentPane.add(textArea2);
		try{
	        FileReader fr = new FileReader("C:/Users/Sadhana/workspace/System/src/fyp/inputFile.txt");
	        BufferedReader br = new BufferedReader(fr);
	        textArea2.read(br, null);
	        //fr.reset();
	        fr.close();			        
		}
		catch(Exception e)
		{			
		}			
				
		JLabel lblNewLabel2 = new JLabel("Input Text");
		lblNewLabel2.setFont(new Font("Calibri", Font.PLAIN, 42));
		lblNewLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel2.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel2.setBounds(830, 120, 250, 70);
		contentPane.add(lblNewLabel2);
		
		JLabel lblNewLabel3 = new JLabel("STAR accepts form based questions.");
		lblNewLabel3.setFont(new Font("Calibri", Font.PLAIN, 28));		
		lblNewLabel3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel3.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel3.setBounds(80, 350, 535, 200);
		contentPane.add(lblNewLabel3);
		
		JLabel lblNewLabel4 = new JLabel("For example, if your question is");
		lblNewLabel4.setFont(new Font("Calibri", Font.PLAIN, 28));		
		lblNewLabel4.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel4.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel4.setBounds(70, 380, 550, 200);
		contentPane.add(lblNewLabel4);
		
		JLabel lblNewLabel5 = new JLabel("'Who caught the thief?'  ask");
		lblNewLabel5.setFont(new Font("Calibri", Font.PLAIN, 28));		
		lblNewLabel5.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel5.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel5.setBounds(70, 410, 500, 200);
		contentPane.add(lblNewLabel5);
		
		JLabel lblNewLabel6 = new JLabel("'X caught the thief'");
		lblNewLabel6.setFont(new Font("Calibri", Font.PLAIN, 28));		
		lblNewLabel6.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel6.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel6.setBounds(70, 440, 500, 200);
		contentPane.add(lblNewLabel6);

	}
}

