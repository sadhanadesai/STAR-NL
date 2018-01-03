package fyp;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class TestSwing extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public String wholetext;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestSwing frame = new TestSwing();
					//frame.setSize(1000, 700);
					frame.setExtendedState(6);
					frame.setResizable(true);
					frame.setVisible(true);
					frame.setTitle("S.T.A.R.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TestSwing() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(30, 144, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel label1 = new JLabel("What shall I learn today?");
		label1.setForeground(UIManager.getColor("EditorPane.background"));
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		label1.setFont(new Font("Calibri", Font.PLAIN, 45));
		label1.setBounds(450, 100, 500, 75);
		contentPane.add(label1);
		
		JLabel label2 = new JLabel("(Give me a Text)");
		label2.setForeground(UIManager.getColor("EditorPane.background"));
		label2.setFont(new Font("Calibri", Font.PLAIN, 26));
		label2.setHorizontalAlignment(SwingConstants.CENTER);
		label2.setBounds(600, 188, 180, 20);
		contentPane.add(label2);
		
		/*final JTextPane textPane = new JTextPane();
		textPane.setBackground(UIManager.getColor("Button.highlight"));
		textPane.setFont(new Font("Lucida Bright", Font.PLAIN, 14));
		textPane.setBounds(500, 275, 400, 150);
		contentPane.add(textPane);*/
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(400, 250, 600, 120);
		contentPane.add(scrollPane);
		
		final JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBackground(UIManager.getColor("Button.highlight"));
		textArea.setFont(new Font("Lucida Bright", Font.PLAIN, 14));
		textArea.setBounds(400, 250, 600, 120);
		
		String Story[] = {"Boyandthewolf" , "Belle"};
		final JComboBox comboBox = new JComboBox(Story);
		comboBox.setEditable(true);
		comboBox.setBounds(600, 540, 200, 25);
		contentPane.add(comboBox);
		
		final JButton btnNewButton = new JButton("LEARN");
		
		btnNewButton.setBorderPainted(false);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				//System.out.println("I am here...");
				FileWriter writeToFile=null;				
				try{
			        writeToFile = new FileWriter ("C:/Users/Sadhana/workspace/System/src/fyp/inputFile.txt");
			   // SwingWorker worker = new SwingWork();
			
			        //textPane.write(write); 
			        textArea.write(writeToFile); 
			        
			      //nextframe			       
					
			    	}
			    catch (Exception e)
				{
			        e.printStackTrace();
			    }
				
				finally
				{
					if(writeToFile != null)
						try {
								//trigger to start executing the main modules.
								//trigger -> text simplification module.
								RunSystem rs = new RunSystem("inputFile.txt", false);
								rs.RunTextSimplifier();
								
								writeToFile.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SAXException e) {
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
					
					 	Question q = new Question();
				        q.setVisible(true);
				        q.setExtendedState(6);
				        //System.out.println("I am here also...");
				        q.setTitle("S.T.A.R.");
				        dispose();
				}
			} 			
			
		});
							
		//btnNewButton.setForeground(new Color(0, 0, 0));
		
		btnNewButton.setBackground(new Color(50, 205, 50));
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setFont(new Font("Calibri", Font.PLAIN, 20));
		btnNewButton.setBounds(652, 390, 100, 35);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("(Give me a file)");
		lblNewLabel.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel.setFont(new Font("Calibri", Font.PLAIN, 26));
		lblNewLabel.setBounds(615, 460, 300, 40);
		contentPane.add(lblNewLabel);
		
		JButton fileButton = new JButton("LEARN FILE");
		
		fileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent a) {
				// TODO Auto-generated method stub
				String selectStory = (String) comboBox.getItemAt(comboBox.getSelectedIndex());
				//label.setText(selectStory);
				try {
			          FileReader fr=new FileReader("C:/Users/Sadhana/workspace/System/src/fyp/"+selectStory+".txt");
			          FileWriter fw=new FileWriter("C:/Users/Sadhana/workspace/System/src/fyp/inputFile.txt");			          
			          int c = fr.read();
			            while(c!=-1) {
			                fw.write(c);
			                c = fr.read();
			            }
			            fr.close();
					    fw.close();
				}
				catch(IOException e) {
			          System.out.println(e);
				}
				//send String selectStory to runSystem
				
				try {
					RunSystem rs = new RunSystem(selectStory+".txt",true);
					rs.RunTextSimplifier();
				} catch (FileNotFoundException | JWNLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Question q = new Question();
		        q.setVisible(true);
		        q.setExtendedState(6);
		        //System.out.println("I am here also...");
		        q.setTitle("S.T.A.R.");
		        dispose();
				}
			});

		fileButton.setBackground(new Color(50, 205, 50));
		fileButton.setForeground(new Color(255, 255, 255));
		fileButton.setFont(new Font("Calibri", Font.PLAIN, 20));
		fileButton.setBounds(652, 610, 130, 35);
		contentPane.add(fileButton);				
	}
}
