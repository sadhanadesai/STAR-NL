package fyp;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
public class Star extends JFrame {
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
					Star frame = new Star();
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
	public Star() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(30, 144, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Hello");
		lblNewLabel.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);		
		lblNewLabel.setFont(new Font("Calibri", Font.PLAIN, 70));
		lblNewLabel.setBounds(450, 130, 500, 75);
		contentPane.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("About me");
		
        btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent a) {
				Speech sp = new Speech();
				sp.callSpeech();
				
				TestSwing ts = new TestSwing();
				 ts.setVisible(true);
			     ts.setExtendedState(6);
			        //System.out.println("I am here also...");
			     ts.setTitle("S.T.A.R.");
			     dispose();
			}
		});
		btnNewButton.setBackground(new Color(50, 205, 50));
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setFont(new Font("Calibri", Font.PLAIN, 30));
		btnNewButton.setBounds(600, 610, 200, 35);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("I am S.T.A.R.");
		lblNewLabel_1.setIcon(new ImageIcon("C:\\Users\\Sadhana\\workspace\\System\\ST2.png"));
		lblNewLabel_1.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel_1.setFont(new Font("Calibri", Font.PLAIN, 60));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(450, 150, 600, 520);
		contentPane.add(lblNewLabel_1);
		
	/*	JLabel lblNewLabel_2 = new JLabel("I am STAR");
		lblNewLabel_2.setForeground(UIManager.getColor("EditorPane.background"));
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);		
		lblNewLabel_2.setFont(new Font("Calibri", Font.PLAIN, 50));
		lblNewLabel_2.setBounds(450, 150, 600, 520);

		contentPane.add(lblNewLabel_2);  */
	}
}
