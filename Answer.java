package fyp;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Answer extends JFrame {

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
					Answer frame = new Answer();
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
	public Answer() {
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
		try{
			
	        FileReader fn = new FileReader("C:/Users/Sadhana/workspace/System/src/fyp/questionFile.txt");
	        //String path = "C:/Users/Sadhana/workspace/FYP/src/main/java/finalYearProject/answerFile.txt";		        
	        BufferedReader br = new BufferedReader(fn);
	        //String test = br.readLine();
	        //System.out.println("TESTING............"+test);
	        textArea.read(br,null); 
	        
	        //String wholetext = new String(Files.readAllBytes(FileSystems.getDefault().getPath(path))); 
	        //textField.setText(wholetext);
	        //fn.reset();
	        
	        fn.close();			        
		}
		catch(Exception e)
		{
		}			
		textArea.setBackground(UIManager.getColor("Button.highlight"));
		textArea.setFont(new Font("Lucida Bright", Font.PLAIN, 16));
		textArea.setBounds(103,	200 , 450, 100);
		contentPane.add(textArea);
		
		JButton btnNewButton = new JButton("Answer");
		
		btnNewButton.setBackground(new Color(50, 205, 50));
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setFont(new Font("Calibri", Font.PLAIN, 20));
		btnNewButton.setBounds(283, 350, 110, 35);
		contentPane.add(btnNewButton);
		
		
		JLabel lblNewLabel1 = new JLabel("Answer");
		lblNewLabel1.setBackground(UIManager.getColor("TextField.selectionForeground"));
		lblNewLabel1.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel1.setFont(new Font("Calibri", Font.PLAIN, 40));
		lblNewLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel1.setBounds(190, 470, 271, 50);
		contentPane.add(lblNewLabel1);
		
		final JTextArea textArea1 = new JTextArea();
		textArea1.setBackground(UIManager.getColor("Button.highlight"));
		textArea1.setEditable(false);
				try{
									
			        FileReader fn = new FileReader("C:/Users/Sadhana/workspace/System/src/fyp/answerFile.txt");
			        //String path = "C:/Users/Sadhana/workspace/FYP/src/main/java/finalYearProject/answerFile.txt";		        
			        BufferedReader br = new BufferedReader(fn);
			        //String test = br.readLine();
			        //System.out.println("TESTING............"+test);
			        textArea1.read(br,null); 
			        
			        //String wholetext = new String(Files.readAllBytes(FileSystems.getDefault().getPath(path))); 
			        //textField.setText(wholetext);
			        //fn.reset();
			        
			        fn.close();			        
				}
				catch(Exception e)
				{
				}				
		textArea1.setFont(new Font("Lucida Bright", Font.PLAIN, 16));
		textArea1.setBounds(103, 520, 450, 100);
		textArea1.setLineWrap(true);
		textArea1.setWrapStyleWord(true);
		contentPane.add(textArea1);
		textArea1.setColumns(10);
	
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(750, 180, 450, 450);
		contentPane.add(scrollPane);
		
		JTextArea textArea2 = new JTextArea();
		textArea2.setEditable(false);
		scrollPane.setViewportView(textArea2);
		textArea2.setFont(new Font("Lucida Bright", Font.PLAIN, 16));
		textArea2.setBounds(750, 180, 450, 450);
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
		
		JButton btnNewButton1 = new JButton("Ask another Question");
		btnNewButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent n) {												
				Question q = new Question();
		        q.setVisible(true);
		        q.setExtendedState(6);
		        q.setTitle("S.T.A.R.");
		        dispose();				
			    } 						
		});
		btnNewButton1.setBackground(new Color(50, 205, 50));
		btnNewButton1.setForeground(new Color(255, 255, 255));
		btnNewButton1.setFont(new Font("Calibri", Font.PLAIN, 20));
		btnNewButton1.setBounds(220, 400, 240, 35);
		contentPane.add(btnNewButton1);
	}
}

